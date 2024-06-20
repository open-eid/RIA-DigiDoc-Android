@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import android.content.Context
import ee.ria.DigiDoc.common.Constant.CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.DATA_FILE_DIR
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getFileInDirectory
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.util.Locale

object ContainerUtil {
    private val LOG_TAG = javaClass.simpleName

    private val FILE_MODIFIED_DATE_COMPARATOR: Comparator<File> =
        Comparator { o1: File, o2: File ->
            if (o1.lastModified() == o2.lastModified()) {
                return@Comparator 0
            }
            if (o1.lastModified() > o2.lastModified()) -1 else 1
        }

    @Throws(IOException::class)
    suspend fun addSignatureContainer(
        context: Context,
        file: File,
    ): File {
        val containerFile = generateSignatureContainerFile(context, file.name)
        file.inputStream().use { inputStream ->
            containerFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return containerFile
    }

    @Throws(IOException::class)
    suspend fun generateSignatureContainerFile(
        context: Context,
        name: String?,
    ): File {
        val file: File =
            increaseCounterIfExists(
                File(
                    signatureContainersDir(context),
                    FilenameUtils.getName(
                        FileUtil.sanitizeString(name, ""),
                    ),
                ),
            )
        val fileInDirectory: File = getFileInDirectory(file, signatureContainersDir(context))
        fileInDirectory.parentFile?.mkdirs()
        return file
    }

    private suspend fun increaseCounterIfExists(file: File): File {
        return withContext(Dispatchers.IO) {
            var updatedFile = file
            val directory = file.getParentFile()
            val name: String? = FileUtil.sanitizeString(file.nameWithoutExtension, "")
            val ext: String = file.extension.lowercase()
            var i = 1
            while (updatedFile.exists()) {
                updatedFile =
                    File(directory, String.format(Locale.US, "%s (%d).%s", name, i++, ext))
            }
            updatedFile
        }
    }

    @Throws(IOException::class)
    suspend fun getCacheFile(
        context: Context,
        name: String,
    ): File {
        val cacheFile =
            File(
                context.cacheDir,
                String.format(
                    Locale.US,
                    "%s",
                    FilenameUtils.getName(FileUtil.sanitizeString(name, "")),
                ),
            )
        return getFileInDirectory(cacheFile, context.cacheDir)
    }

    fun getContainerDataFilesDir(
        context: Context,
        containerFile: File,
    ): File {
        val directory: File =
            if (containerFile.parentFile == signatureContainersDir(context)) {
                createDataFileDirectory(
                    context.cacheDir,
                    containerFile,
                )
            } else {
                createDataFileDirectory(
                    containerFile.parentFile,
                    containerFile,
                )
            }
        return directory
    }

    private fun createDataFileDirectory(
        directory: File?,
        container: File,
    ): File {
        var dir: File
        var i = 0
        while (true) {
            val name =
                StringBuilder(
                    String.format(
                        Locale.US,
                        DATA_FILE_DIR,
                        container.name,
                    ),
                )
            if (i > 0) {
                name.append(i)
            }
            dir = File(directory, name.toString())
            if (dir.isDirectory || !dir.exists()) {
                break
            }
            i++
        }
        val isDirsCreated = dir.mkdirs()
        val isDirCreated = dir.mkdir()

        if (isDirsCreated || isDirCreated) {
            if (directory != null) {
                debugLog(LOG_TAG, "Directories created for " + directory.path)
            }
        }

        return dir
    }

    fun isEmptyFileInList(files: List<File>): Boolean {
        for (file in files) {
            if (file.length().toInt() == 0) {
                return true
            }
        }

        return false
    }

    @Throws(IOException::class)
    fun getFilesWithValidSize(files: List<File>): List<File> {
        val validFiles: MutableList<File> = ArrayList()
        for (file in files) {
            if (file.length().toInt() != 0) {
                validFiles.add(file)
            }
        }

        return validFiles
    }

    fun findSignatureContainerFiles(context: Context): List<File> {
        val signatureContainerFileList = signatureContainersDir(context).listFiles()

        val fileList = signatureContainerFileList?.sortedWith(FILE_MODIFIED_DATE_COMPARATOR)?.toList()

        if (fileList != null) {
            return fileList.filter { file ->
                FilenameUtils.isExtension(file.name, CONTAINER_EXTENSIONS)
            }
        }

        return emptyList()
    }

    private fun signatureContainersDir(context: Context): File {
        val dir = File(context.filesDir, DIR_SIGNATURE_CONTAINERS)
        val isDirsCreated = dir.mkdirs()
        if (isDirsCreated) {
            debugLog(LOG_TAG, "Directories created for ${dir.path}")
        }
        return dir
    }

    fun removeExtensionFromContainerFilename(filename: String): String {
        return FilenameUtils.removeExtension(filename)
    }

    fun addExtensionToContainerFilename(filename: String): String {
        val normalizedFilename = FileUtil.normalizePath(filename).path
        val sanitizedFilename = FileUtil.sanitizeString(normalizedFilename, "")
        val displayName = FilenameUtils.getName(sanitizedFilename)

        return when {
            displayName.isNotEmpty() && FilenameUtils.isExtension(displayName, CONTAINER_EXTENSIONS) ->
                displayName
            else ->
                if (displayName.isNotEmpty()) {
                    "$displayName.$DEFAULT_CONTAINER_EXTENSION"
                } else {
                    "$DEFAULT_FILENAME.$DEFAULT_CONTAINER_EXTENSION"
                }
        }
    }
}
