@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.webkit.URLUtil
import ee.ria.DigiDoc.common.Constant.ALLOWED_URL_CHARACTERS
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.common.Constant.RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.infoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.w3c.dom.Document
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.text.Normalizer
import java.util.Objects
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object FileUtil {
    private val LOG_TAG = javaClass.simpleName

    /**
     * Check if file path is in cache directory
     *
     * @param file File to check
     * @return Boolean indicating if file is in the cache directory.
     */
    @Throws(IOException::class)
    suspend fun getFileInDirectory(
        file: File,
        directory: File,
    ): File {
        if (!file.toPath().normalize().startsWith(directory.toPath())) {
            throw IOException(
                "Invalid path: " +
                    withContext(Dispatchers.IO) {
                        file.getCanonicalPath()
                    },
            )
        }
        return file
    }

    /**
     * Get Smart-ID V2 file name
     *
     * @param file File to get name from
     * @return String with updated file name
     */
    fun getSignDocumentFileName(file: File): String {
        val fullFileName = file.getName()
        val fileName: String = FilenameUtils.getBaseName(fullFileName)
        val fileExtension: String = FilenameUtils.getExtension(fullFileName)
        return if (fileName.length <= 6) {
            "$fileName.$fileExtension"
        } else {
            (
                StringUtils.left(fileName, 3) +
                    "..." +
                    StringUtils.right(fileName, 3)
            ) +
                "." +
                fileExtension
        }
    }

    /**
     * Replace invalid string characters
     *
     * @param input Input to replace invalid characters
     * @param replacement Replacement to replace invalid characters with
     * @return String with valid characters
     */
    fun sanitizeString(
        input: String?,
        replacement: String?,
    ): String? {
        if (input == null) {
            return null
        }
        var trimmed = input.trim { it <= ' ' }
        if (trimmed.startsWith(".")) {
            trimmed = DEFAULT_FILENAME + trimmed
        }
        val sb = StringBuilder(trimmed.length)
        if (!URLUtil.isValidUrl(trimmed) && !isRawUrl(trimmed)) {
            for (element in trimmed) {
                if (RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING.indexOf(element) != -1) {
                    sb.append(replacement)
                } else {
                    sb.append(element)
                }
            }
        } else if (!isRawUrl(trimmed)) {
            return normalizeUri(Uri.parse(trimmed)).toString()
        }
        return if (sb.toString().isNotEmpty()) {
            FilenameUtils.getName(
                FilenameUtils.normalize(
                    sb.toString(),
                ),
            )
        } else {
            FilenameUtils.normalize(trimmed)
        }
    }

    fun normalizeUri(uri: Uri?): Uri? {
        return if (uri == null) {
            null
        } else {
            Uri.parse(normalizeText(uri.toString()))
        }
    }

    fun normalizeText(text: String): String {
        val sb = StringBuilder(text.length)
        for (element in text) {
            val i = ALLOWED_URL_CHARACTERS.indexOf(element)
            if (i == -1) {
                sb.append("")
            } else {
                // Coverity does not want to see usages of the original string
                sb.append(ALLOWED_URL_CHARACTERS[i])
            }
        }
        return sb.toString()
    }

    fun normalizeString(text: String?): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
    }

    fun normalizePath(filePath: String?): Uri {
        return Uri.parse(FilenameUtils.normalize(filePath))
    }

    fun isPDF(file: File?): Boolean {
        try {
            ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY,
            ).use { parcelFileDescriptor ->
                // Try to render as PDF. Throws exception if not a PDF file.
                PdfRenderer(parcelFileDescriptor)
                return true
            }
        } catch (e: IOException) {
            return false
        }
    }

    fun renameFile(
        path: Path,
        fileNameWithExtension: String?,
    ): Path {
        return try {
            Files.deleteIfExists(path.resolveSibling(fileNameWithExtension))
            val newFilePath = path.resolveSibling(fileNameWithExtension)
            Files.move(path, newFilePath)
            newFilePath
        } catch (e: IOException) {
            path
        }
    }

    fun logsExist(logsDirectory: File): Boolean {
        if (logsDirectory.exists()) {
            val files = logsDirectory.listFiles()
            return files != null && files.isNotEmpty()
        }
        return false
    }

    @Throws(IOException::class)
    fun combineLogFiles(
        logsDirectory: File,
        diagnosticsLogsFileName: String,
    ): File {
        if (logsExist(logsDirectory)) {
            val files =
                if (logsDirectory.listFiles() != null) logsDirectory.listFiles() else arrayOf()
            val combinedLogFile =
                File(logsDirectory, diagnosticsLogsFileName)
            if (combinedLogFile.exists()) {
                Files.delete(combinedLogFile.toPath())
            }
            if (files != null) {
                for (file in files) {
                    val header = """

===== File: ${file.getName()} =====

"""
                    val fileString =
                        header +
                            FileUtils.readFileToString(
                                file,
                                Charset.defaultCharset(),
                            )
                    FileUtils.write(
                        combinedLogFile,
                        fileString,
                        Charset.defaultCharset(),
                        true,
                    )
                }
            }
            return combinedLogFile
        }
        throw FileNotFoundException("Could not combine log files. Cannot find logs.")
    }

    fun getLogsDirectory(context: Context): File {
        return File(context.filesDir.toString() + "/logs")
    }

    fun getCertFile(
        context: Context,
        certName: String,
        certFolder: String?,
    ): File? {
        val savedCertFolder = certFolder?.let { File(context.filesDir, it) }
        val files = savedCertFolder?.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isFile() && file.getName() == certName) {
                    return file
                }
            }
        }
        return null
    }

    private fun isRawUrl(url: String?): Boolean {
        return if (url.isNullOrEmpty()) {
            false
        } else {
            url.startsWith("raw:")
        }
    }

    fun readFileContent(filePath: String): String {
        try {
            FileInputStream(filePath).use { fileInputStream ->
                return readFileContent(
                    fileInputStream,
                )
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read content of cached file '$filePath'", e)
        }
    }

    fun readFileContent(inputStream: InputStream?): String {
        try {
            BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                val sb = java.lang.StringBuilder()
                var i: Int
                while (reader.read().also { i = it } != -1) {
                    sb.append(i.toChar())
                }
                return sb.toString().trim { it <= ' ' }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read content of cached file", e)
        }
    }

    fun readFileContentBytes(filePath: String): ByteArray {
        try {
            FileInputStream(filePath).use { fileInputStream ->
                return readFileContentBytes(
                    fileInputStream,
                )
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read content of cached file '$filePath'", e)
        }
    }

    fun readFileContentBytes(inputStream: InputStream): ByteArray {
        try {
            ByteArrayOutputStream().use { buffer ->
                var nRead: Int
                val data = ByteArray(16384)
                while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                    buffer.write(data, 0, nRead)
                }
                return buffer.toByteArray()
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to read content of cached file", e)
        }
    }

    fun storeFile(
        filePath: String,
        content: String?,
    ) {
        val file = File(filePath)
        val isDirsCreated = Objects.requireNonNull(file.getParentFile()).mkdirs()
        if (isDirsCreated) {
            infoLog(LOG_TAG, "Directories created for $filePath")
        }
        try {
            FileOutputStream(file.getAbsoluteFile()).use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
                    .use { writer ->
                        writer.write(content)
                    }
            }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to store file '$filePath'!", e)
        }
    }

    fun storeFile(
        filePath: String,
        content: ByteArray?,
    ) {
        val file = File(filePath)
        val isDirsCreated = Objects.requireNonNull(file.getParentFile()).mkdirs()
        if (isDirsCreated) {
            infoLog(LOG_TAG, "Directories created for $filePath")
        }
        try {
            FileOutputStream(file).use { os -> os.write(content) }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to store file '$filePath'!", e)
        }
    }

    fun createDirectoryIfNotExist(directory: String) {
        val destinationDirectory = File(directory)
        if (!destinationDirectory.exists()) {
            val isDirsCreated = destinationDirectory.mkdirs()
            if (isDirsCreated) {
                infoLog(LOG_TAG, "Directories created for $directory")
            }
        }
    }

    fun fileExists(filePath: String?): Boolean {
        return filePath?.let { File(it).exists() } ?: false
    }

    fun removeFile(filePath: String) {
        val fileToDelete = File(FilenameUtils.normalize(filePath))
        if (fileToDelete.exists()) {
            val isFileDeleted = fileToDelete.delete()
            if (isFileDeleted) {
                infoLog(LOG_TAG, "File deleted: $filePath")
            }
        }
    }

    @Throws(IOException::class)
    fun getFileInContainerZip(
        containerFile: File,
        fileNameToFind: String?,
        outputFolder: File,
    ): File? {
        createDirectoryIfNotExist(outputFolder.path)

        ZipInputStream(containerFile.inputStream()).use { zipInputStream ->
            var zipEntry: ZipEntry?

            while (zipInputStream.nextEntry.also { zipEntry = it } != null) {
                val entryName = zipEntry?.name
                if (entryName != null && entryName == fileNameToFind) {
                    val outputFile = File(outputFolder, entryName)
                    FileOutputStream(outputFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                    return outputFile
                }
            }
        }

        return null
    }

    fun readFileAsString(file: File): String = file.readText()

    fun deleteFilesInFolder(directory: File) {
        if (directory.exists()) {
            directory.deleteRecursively()
        }

        directory.delete()
    }

    fun writeToFile(
        reader: BufferedReader,
        destinationPath: String,
        fileName: String,
    ) {
        val file = File(destinationPath, fileName)

        try {
            file.outputStream().use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    reader.forEachLine { line ->
                        writer.write(line)
                        writer.write(System.lineSeparator())
                    }
                }
            }
        } catch (e: IOException) {
            errorLog(LOG_TAG, "Failed to write to file: $fileName", e)
        }
    }

    fun getNameFromFileName(fileName: String): String? {
        return FilenameUtils.getName(fileName)
    }

    fun parseXMLFile(file: File): Document? {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            return builder.parse(file)
        } catch (ex: Exception) {
            return null
        }
    }
}
