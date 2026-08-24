/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.core.net.toUri
import ee.ria.DigiDoc.common.Constant.ALLOWED_URL_CHARACTERS
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.common.Constant.FORBIDDEN_FILENAME_CHARACTERS
import ee.ria.DigiDoc.common.Constant.MAX_FILENAME_BYTES
import ee.ria.DigiDoc.common.Constant.ZERO_WIDTH_JOINER_CODE
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.infoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
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
import java.text.BreakIterator
import java.text.Normalizer
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory

object FileUtil {
    private val LOG_TAG = javaClass.simpleName
    private val ALLOWED_FILE_SCHEMES = setOf("content", "file")

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
                        file.canonicalPath
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
        input: String,
        replacement: String,
    ): String {
        var trimmed = input.trim { it <= ' ' }
        if (trimmed.startsWith("..")) {
            trimmed = trimmed.removePrefix("..")
        } else if (trimmed.startsWith(".")) {
            trimmed = DEFAULT_FILENAME + trimmed
        }
        if (isRawUrl(trimmed)) {
            return FilenameUtils.getName(FilenameUtils.normalize(trimmed)) ?: DEFAULT_FILENAME
        }
        if (URLUtil.isValidUrl(trimmed)) {
            return FilenameUtils.getName(normalizeUri(trimmed.toUri()).toString()) ?: DEFAULT_FILENAME
        }
        val sb = StringBuilder(trimmed.length)
        for (element in trimmed) {
            if (isForbiddenInFileName(element)) {
                sb.append(replacement)
            } else {
                sb.append(element)
            }
        }
        val name: String =
            FilenameUtils.getName(
                FilenameUtils.normalize(
                    sb.toString().trim { it <= ' ' },
                ),
            ) ?: ""
        return if (name.isEmpty() || name.all { it == '.' }) {
            DEFAULT_FILENAME
        } else {
            truncateFileName(Normalizer.normalize(name, Normalizer.Form.NFC), MAX_FILENAME_BYTES)
        }
    }

    private fun isForbiddenInFileName(character: Char): Boolean {
        if (character.code == ZERO_WIDTH_JOINER_CODE) {
            return false
        }
        return FORBIDDEN_FILENAME_CHARACTERS.indexOf(character) != -1 ||
            character.category == CharCategory.CONTROL ||
            character.category == CharCategory.FORMAT
    }

    fun truncateFileName(
        fileName: String,
        maxBytes: Int,
    ): String {
        if (fileName.toByteArray().size <= maxBytes) {
            return fileName
        }
        val extension = FilenameUtils.getExtension(fileName)
        val suffix = if (extension.isEmpty()) "" else ".$extension"
        val baseName = FilenameUtils.getBaseName(fileName)

        val shortenedBase = truncateToBytes(baseName, maxBytes - suffix.toByteArray().size)
        if (shortenedBase.isNotEmpty()) {
            return shortenedBase + suffix
        }

        val shortenedName = truncateToBytes(fileName, maxBytes)
        if (shortenedName.isNotEmpty()) {
            return shortenedName
        }

        return truncateToBytes(DEFAULT_FILENAME + suffix, maxBytes)
    }

    private fun truncateToBytes(
        text: String,
        maxBytes: Int,
    ): String {
        val characters = BreakIterator.getCharacterInstance()
        characters.setText(text)

        var lastFit = characters.first()
        var bytes = 0
        for (next in generateSequence { characters.next().takeIf { it != BreakIterator.DONE } }) {
            bytes += text.substring(lastFit, next).toByteArray().size
            if (bytes > maxBytes) {
                break
            }
            lastFit = next
        }
        return text.substring(0, lastFit)
    }

    fun uniqueFileName(
        fileName: String,
        taken: Set<String>,
    ): String {
        if (!taken.contains(fileName)) {
            return fileName
        }
        val baseName = FilenameUtils.getBaseName(fileName).ifEmpty { DEFAULT_FILENAME }
        val extension = FilenameUtils.getExtension(fileName)
        var counter = 1
        while (true) {
            val candidate = if (extension.isEmpty()) "$baseName ($counter)" else "$baseName ($counter).$extension"
            if (!taken.contains(candidate)) {
                return candidate
            }
            counter++
        }
    }

    fun normalizeUri(uri: Uri?): Uri? =
        if (uri == null) {
            null
        } else {
            normalizeText(uri.toString()).toUri()
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

    fun normalizePath(filePath: String?): Uri = FilenameUtils.normalize(filePath).toUri()

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
                    if (file.extension == "log") {
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
            }
            return combinedLogFile
        }
        throw FileNotFoundException("Could not combine log files. Cannot find logs.")
    }

    fun getLogsDirectory(context: Context): File = File(context.filesDir.toString(), "logs")

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

    private fun isRawUrl(url: String?): Boolean =
        if (url.isNullOrEmpty()) {
            false
        } else {
            url.startsWith("raw:")
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
        try {
            val parentFile = file.parentFile ?: throw IOException("No parent directory for path: $filePath")
            if (!parentFile.exists()) {
                val isDirsCreated = parentFile.mkdirs()
                if (isDirsCreated) {
                    infoLog(LOG_TAG, "Directories created or already exist for $filePath")
                }
            }
            FileOutputStream(file.absoluteFile).use { outputStream ->
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
        try {
            val parentFile = file.parentFile ?: throw IOException("No parent directory for path: $filePath")
            if (!parentFile.exists()) {
                val isDirsCreated = parentFile.mkdirs()
                if (isDirsCreated) {
                    infoLog(LOG_TAG, "Directories created or already exist for $filePath")
                }
            }
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
                infoLog(LOG_TAG, "Directories created or already exist for $directory")
            }
        }
    }

    fun fileExists(filePath: String?): Boolean = filePath?.let { File(it).exists() } ?: false

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
        fileNameToFind: String,
        outputFolder: File,
    ): File? {
        createDirectoryIfNotExist(outputFolder.path)

        val resolvedDestDir = outputFolder.canonicalFile

        val matches = { entryName: String ->
            val file = File(entryName)
            val fileName = file.name
            val nameWithoutExt = file.nameWithoutExtension
            val extension = file.extension

            when {
                fileNameToFind.contains(".") -> {
                    fileName.equals(fileNameToFind, ignoreCase = true)
                }

                else -> {
                    extension.equals(fileNameToFind, ignoreCase = true) ||
                        (
                            extension.isEmpty() &&
                                nameWithoutExt.equals(fileNameToFind, ignoreCase = true)
                        )
                }
            }
        }

        val zip =
            ZipFile
                .Builder()
                .setFile(containerFile)
                .get()

        zip.use {
            val entry =
                it.entries
                    .asSequence()
                    .filterNot { archiveEntry -> archiveEntry.isDirectory }
                    .firstOrNull { archiveEntry -> matches(archiveEntry.name) }
                    ?: return null

            val outputFile = File(outputFolder, File(entry.name).name)
            val resolvedOutFile = outputFile.canonicalFile

            if (!resolvedOutFile.path.startsWith(resolvedDestDir.path + File.separator)) {
                return null
            }

            it.getInputStream(entry).use { input ->
                resolvedOutFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return resolvedOutFile
        }
    }

    fun readFileAsString(file: File): String = file.readText()

    fun deleteFilesInFolder(directory: File) {
        if (directory.exists()) {
            directory.deleteRecursively()
        }

        directory.delete()
    }

    @Throws(IOException::class)
    fun writeToFile(
        reader: BufferedReader,
        destinationPath: String,
        fileName: String,
    ) {
        val file = File(destinationPath, fileName)
        var temporaryFile: File? = null

        try {
            temporaryFile = File.createTempFile(fileName, ".tmp", File(destinationPath))

            temporaryFile.outputStream().use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    reader.forEachLine { line ->
                        writer.write(line)
                        writer.write(System.lineSeparator())
                    }
                }
            }

            if (!temporaryFile.renameTo(file)) {
                throw IOException("Unable to move the completed file into place: $fileName")
            }
            infoLog(LOG_TAG, "Wrote file $fileName (${file.length()} bytes)")
        } catch (e: Exception) {
            errorLog(LOG_TAG, "Failed to write to file: $fileName", e)
            throw e
        } finally {
            temporaryFile?.let {
                if (it.exists() && !it.delete()) {
                    errorLog(LOG_TAG, "Unable to delete the temporary file: ${it.name}")
                }
            }
        }
    }

    fun getNameFromFileName(fileName: String): String? = FilenameUtils.getName(fileName)

    fun parseXMLFile(file: File): Document? {
        try {
            val inputStream = FileInputStream(file)
            val documentFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentFactory.newDocumentBuilder()
            val document = documentBuilder.newDocument()

            val saxFactory = SAXParserFactory.newInstance()
            val saxParser = saxFactory.newSAXParser()

            var currentElement: Element? = null

            val handler =
                object : DefaultHandler() {
                    override fun startElement(
                        uri: String?,
                        localName: String?,
                        qName: String?,
                        attributes: Attributes?,
                    ) {
                        val tagName = qName ?: return
                        val element = document.createElement(tagName)
                        for (i in 0 until (attributes?.length ?: 0)) {
                            if (attributes != null) {
                                element.setAttribute(attributes.getQName(i), attributes.getValue(i))
                            }
                        }
                        currentElement?.appendChild(element) ?: document.appendChild(element)
                        currentElement = element
                    }

                    override fun characters(
                        ch: CharArray?,
                        start: Int,
                        length: Int,
                    ) {
                        val text = ch?.let { String(it, start, length).trim() }.orEmpty()
                        if (text.isNotEmpty()) {
                            currentElement?.appendChild(document.createTextNode(text))
                        }
                    }

                    override fun endElement(
                        uri: String?,
                        localName: String?,
                        qName: String?,
                    ) {
                        currentElement = currentElement?.parentNode as? Element
                    }
                }

            saxParser.parse(InputSource(inputStream), handler)
            inputStream.close()
            return document
        } catch (_: Exception) {
            return null
        }
    }

    fun getExternalFileUris(intent: Intent): List<Uri> {
        val clipData = intent.clipData
        val data = intent.data
        val uris =
            when {
                // The same file can be in both clipData and data, so read clipData only to avoid duplicates.
                clipData != null && clipData.itemCount > 0 ->
                    (0 until clipData.itemCount).mapNotNull { clipData.getItemAt(it)?.uri }
                // If there is no clipData, take the file from data, or from a share (EXTRA_STREAM).
                data != null -> listOf(data)
                intent.action == Intent.ACTION_SEND ->
                    listOfNotNull(intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
                intent.action == Intent.ACTION_SEND_MULTIPLE ->
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
                else -> emptyList()
            }
        // Only file-backed schemes are openable. Drop web (https) and deep-link (e.g. web-eid-mobile) URIs.
        return uris.filter { it.scheme in ALLOWED_FILE_SCHEMES }
    }
}
