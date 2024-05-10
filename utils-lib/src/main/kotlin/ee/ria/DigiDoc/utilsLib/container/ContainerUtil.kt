@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.common.io.ByteStreams
import ee.ria.DigiDoc.common.Constant.DATA_FILE_DIR
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.common.Constant.SIGNATURE_CONTAINER_EXT
import ee.ria.DigiDoc.utilsLib.file.FileStream
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getFileInDirectory
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

object ContainerUtil {
    private val LOG_TAG = javaClass.simpleName

    @Throws(IOException::class)
    suspend fun addSignatureContainer(
        context: Context,
        file: File,
    ): File {
        val containerName = addExtensionToContainerFilename(file.name)
        val containerFile = generateSignatureContainerFile(context, containerName)
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
    suspend fun cache(
        context: Context,
        fileStream: FileStream,
    ): File? {
        val file: File? = fileStream.displayName?.let { getCacheFile(context, it) }
        fileStream.source?.openStream().use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                if (inputStream != null) {
                    ByteStreams.copy(inputStream, outputStream)
                }
            }
        }
        return file
    }

    @Throws(IOException::class)
    private suspend fun getCacheFile(
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

    fun isEmptyFileInList(fileStreams: List<FileStream>): Boolean {
        for (fileStream in fileStreams) {
            if (fileStream.fileSize?.toInt() == 0) {
                return true
            }
        }

        return false
    }

    @Throws(IOException::class)
    fun getFilesWithValidSize(fileStreams: List<FileStream>): List<FileStream> {
        val validFileStreams: MutableList<FileStream> = ArrayList()
        for (fileStream in fileStreams) {
            if (fileStream.fileSize?.toInt() != 0) {
                validFileStreams.add(fileStream)
            }
        }

        return validFileStreams
    }

    fun parseUris(
        contentResolver: ContentResolver?,
        uris: List<Uri>,
    ): List<FileStream> {
        val list: ArrayList<FileStream> = ArrayList()
        for (i in uris.indices) {
            val uri = uris[i]
            list.add(
                FileStream.create(
                    contentResolver,
                    uri,
                    contentResolver?.let {
                        FileUtil.normalizeUri(uri)
                            ?.let { uri -> getFileSize(it, uri) }
                    },
                ),
            )
        }

        return list
    }

    private fun getFileSize(
        contentResolver: ContentResolver?,
        uri: Uri,
    ): Long {
        val cursor =
            FileUtil.normalizeUri(uri)?.let { contentResolver?.query(it, null, null, null, null) }
        var fileSize: Long = 0
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && !cursor.isNull(columnIndex)) {
                fileSize = cursor.getLong(columnIndex)
            }
            cursor.close()
            return fileSize
        }
        @Suppress("KotlinConstantConditions")
        return fileSize
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
        val normalizedDisplayName =
            FilenameUtils.getName(
                FileUtil.sanitizeString(
                    FileUtil.normalizePath(
                        filename,
                    ).path,
                    "",
                ),
            )
        val containerName =
            java.lang.String.format(
                Locale.US,
                "%s.%s",
                FilenameUtils.removeExtension(normalizedDisplayName),
                SIGNATURE_CONTAINER_EXT,
            )

        return containerName
    }
}
