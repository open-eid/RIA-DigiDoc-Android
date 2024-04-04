package ee.ria.DigiDoc.libdigidoclib.update

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.tools.JavaCompiler
import javax.tools.StandardJavaFileManager
import javax.tools.ToolProvider

open class UpdateLibdigidocppTask : DefaultTask() {
    companion object {
        private const val PREFIX = "libdigidocpp."
        private const val SUFFIX = ".zip"
        private const val JAR = PREFIX + "jar"
        private const val SCHEMA = "schema.zip"

        private val ABIS = listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        private val ABI_FILES =
            mapOf(
                "arm64-v8a" to "androidarm64",
                "armeabi-v7a" to "androidarm",
                "x86_64" to "androidx86_64"
            )
    }

    private var dir: String = "."

    @Option(option = "dir", description = "Directory where the libdigidocpp ZIP files are")
    fun setDir(dir: String) {
        this.dir = dir
    }

    @Input
    fun getDir(): String {
        return dir
    }

    @TaskAction
    fun run() {
        val inputDir = File(project.rootDir, dir)
        val outputDir = temporaryDir
        outputDir.deleteRecursively()

        ABIS.forEach { abi ->
            val inputFile = File(inputDir, "$PREFIX${ABI_FILES[abi]}$SUFFIX")
            val outputUnzippedDir = File(outputDir, "unzipped")
            update(inputFile, outputUnzippedDir, abi)
        }
    }

    private fun update(
        zipFile: File,
        outputDir: File,
        abi: String
    ) {
        if (!zipFile.exists()) {
            log("Could not find file $zipFile")
            return
        }

        log("Updating from $zipFile")
        unzip(zipFile, outputDir)
        val cacheDir = File(outputDir, PREFIX + ABI_FILES[abi])

        generateAndCopyJar(cacheDir)
        generateAndCopySchema(cacheDir)
        copyNativeLibraries(cacheDir, abi)
    }

    private fun generateAndCopyJar(cacheDir: File) {
        val sourceDir = File(cacheDir, "include")
        val jarFile = File(cacheDir, JAR)

        log("Generating $JAR from ${cacheDir.parentFile}")
        keepFolder(sourceDir, "ee")
        compile(sourceDir)
        jar(sourceDir, jarFile)

        val destinationDir = File(project.projectDir, "libs")
        jarFile.copyTo(destinationDir.resolve(JAR), overwrite = true)
    }

    private fun generateAndCopySchema(cacheDir: File) {
        log("Generating $SCHEMA from ${cacheDir.parentFile}")
        val schemaCacheDir = File(cacheDir, "etc")
        val schemaZipFile = File(cacheDir, SCHEMA)

        FileOutputStream(schemaZipFile).use { fileOutputStream ->
            ZipOutputStream(fileOutputStream).use { schemaOutputStream ->
                files(schemaCacheDir).forEach { schemaFile ->
                    val entry = ZipEntry(schemaFile.name)
                    entry.time = schemaFile.lastModified()
                    schemaOutputStream.putNextEntry(entry)
                    schemaFile.inputStream().use { inputStream ->
                        inputStream.copyTo(schemaOutputStream)
                    }
                    schemaOutputStream.closeEntry()
                }
            }
        }
        val schemaDir = File(project.projectDir, "src/main/res/raw")
        schemaZipFile.copyTo(File(schemaDir, SCHEMA), true)
    }

    private fun copyNativeLibraries(
        cacheDir: File,
        abi: String
    ) {
        val nativeLib = File(cacheDir, "lib/libdigidoc_java.so")
        val destDirDebug = File(project.projectDir, "src/debug/jniLibs/$abi")
        val destDirMain = File(project.projectDir, "src/main/jniLibs/$abi")

        nativeLib.copyTo(File(destDirDebug, "libdigidoc_java.so"), true)
        nativeLib.copyTo(File(destDirMain, "libdigidoc_java.so"), true)
    }

    private fun log(
        message: String,
        vararg parameters: Any
    ) {
        logger.lifecycle(String.format(message, *parameters))
    }

    private fun keepFolder(
        folder: File,
        keepFolderName: String
    ) {
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory && file.name != keepFolderName) {
                delete(file)
            }
        }
    }

    private fun delete(file: File?) {
        file?.let { currentFile ->
            currentFile.listFiles()?.forEach { subFile ->
                delete(subFile)
            }
            if (!currentFile.delete()) {
                throw IOException("Failed to delete file ${currentFile.name}")
            }
        }
    }

    private fun unzip(
        zip: File,
        destination: File
    ) {
        FileInputStream(zip).use { fileInputStream ->
            unzip(fileInputStream, destination)
        }
    }

    private fun unzip(
        stream: FileInputStream,
        destination: File
    ) {
        ZipInputStream(stream).use { zipInputStream ->
            var entry: ZipEntry?
            while (zipInputStream.nextEntry.also { entry = it } != null) {
                val entryName = entry?.name ?: throw ZipException("Invalid zip entry")
                val entryFile = File(destination, entryName)
                if (entry?.isDirectory == true) continue

                if (!entryFile.toPath().normalize().startsWith(destination.toPath())) {
                    throw ZipException("Bad zip entry: $entryName")
                }

                entryFile.parentFile?.apply {
                    mkdirs()
                    entryFile.outputStream().use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                } ?: throw FileNotFoundException("Unable to get file to make directories")
            }
        }
    }

    private fun compile(path: File) {
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        val fileManager: StandardJavaFileManager = compiler.getStandardFileManager(null, null, null)
        compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjectsFromFiles(files(path))).call()
        fileManager.close()
    }

    private fun jar(
        path: File,
        jar: File
    ) {
        val manifest =
            Manifest().apply {
                mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
            }

        FileOutputStream(jar).use { fileOutputStream ->
            JarOutputStream(fileOutputStream, manifest).use { outputStream ->
                Files.walk(Paths.get(path.toURI())).filter { Files.isRegularFile(it) }.forEach { file ->
                    if (file.toString().endsWith(".class")) {
                        val entryName = path.toPath().relativize(file).toString()
                        val entry =
                            JarEntry(entryName).apply {
                                time = Files.getLastModifiedTime(file).toMillis()
                            }
                        outputStream.putNextEntry(entry)
                        Files.copy(file, outputStream)
                        outputStream.closeEntry()
                    }
                }
            }
        }
    }

    private fun files(dir: File?): List<File> {
        val files = mutableListOf<File>()
        dir?.listFiles()?.forEach { file ->
            files.addAll(if (file.isDirectory) files(file) else listOf(file))
        }
        return files
    }
}
