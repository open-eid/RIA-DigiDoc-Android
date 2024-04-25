@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.container

import android.content.Context
import ee.ria.DigiDoc.common.Constant.DIR_SIGNATURE_CONTAINERS
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.util.Locale

object ContainerUtil {
    private val LOG_TAG = javaClass.simpleName

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
        val fileInDirectory: File = FileUtil.getFileInDirectory(file, signatureContainersDir(context))
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

    private suspend fun signatureContainersDir(context: Context): File {
        return withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, DIR_SIGNATURE_CONTAINERS)
            val isDirsCreated = dir.mkdirs()
            if (isDirsCreated) {
                debugLog(LOG_TAG, "Directories created for ${dir.path}")
            }
            dir
        }
    }
}
