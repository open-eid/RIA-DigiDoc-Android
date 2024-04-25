@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getNameFromFileName
import ee.ria.DigiDoc.utilsLib.file.FileUtil.normalizeUri
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Singleton
class FileOpeningServiceImpl : FileOpeningService {
    private val logTag = javaClass.simpleName

    override suspend fun isFileSizeValid(file: File): Boolean {
        return try {
            file.exists() && file.isFile && file.length() > 0
        } catch (e: Exception) {
            errorLog(logTag, "Unable to check file size", e)
            false
        }
    }

    override suspend fun uriToFile(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri,
    ): File {
        var displayName =
            uri.lastPathSegment?.let { getNameFromFileName(it) } ?: DEFAULT_FILENAME
        val sanitizedUri = normalizeUri(uri)

        val cursor =
            sanitizedUri?.let {
                contentResolver.query(
                    it,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null,
                )
            }

        cursor?.use {
            if (it.moveToFirst() && !it.isNull(0)) {
                displayName = it.getString(0)?.let { name ->
                    sanitizeString(name, "")
                }?.let { sanitized ->
                    getNameFromFileName(sanitized)
                } ?: DEFAULT_FILENAME
            }
        }

        cursor?.close()

        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputFile = File(context.cacheDir, displayName)
        val outputStream: OutputStream = outputFile.outputStream()

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return outputFile
    }
}
