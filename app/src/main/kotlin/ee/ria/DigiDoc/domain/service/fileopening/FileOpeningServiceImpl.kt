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

package ee.ria.DigiDoc.domain.service.fileopening

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getNameFromFileName
import ee.ria.DigiDoc.utilsLib.file.FileUtil.normalizeUri
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import javax.inject.Singleton

@Singleton
class FileOpeningServiceImpl : FileOpeningService {
    private val logTag = javaClass.simpleName

    override suspend fun isFileSizeValid(file: File): Boolean =
        try {
            file.exists() && file.isFile && file.length() > 0
        } catch (e: Exception) {
            errorLog(logTag, "Unable to check file size", e)
            false
        }

    @Throws(FileNotFoundException::class, SecurityException::class)
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
                displayName = it
                    .getString(0)
                    ?.let { name ->
                        sanitizeString(name, "").trim().let { sanitizedString ->
                            if (sanitizedString.isEmpty() || sanitizedString.startsWith(".")) {
                                "$DEFAULT_FILENAME$sanitizedString"
                            } else if (sanitizedString.endsWith(".")) {
                                DEFAULT_FILENAME
                            } else {
                                sanitizedString
                            }
                        }
                    }?.let { sanitized ->
                        getNameFromFileName(sanitized)
                    } ?: DEFAULT_FILENAME
            }
        }

        cursor?.close()

        val uriInfo = "scheme: ${uri.scheme}, authority: ${uri.authority}"
        debugLog(logTag, "Opening file: $uriInfo, name: $displayName")

        val inputStream: InputStream =
            contentResolver.openInputStream(uri)
                ?: run {
                    errorLog(logTag, "openInputStream returned null for $uriInfo, name: $displayName")
                    throw IOException("Cannot open input stream for URI: $uriInfo")
                }
        val outputFile = File(context.cacheDir, displayName)

        try {
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            debugLog(logTag, "File copied successfully - name: $displayName, size: ${outputFile.length()}")
        } catch (e: Exception) {
            errorLog(logTag, "Failed to copy file - $uriInfo name: $displayName", e)
            outputFile.delete()
            throw e
        }

        return outputFile
    }
}
