@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.file

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.google.common.io.ByteSource
import com.google.common.io.Files
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import org.apache.commons.io.FilenameUtils
import java.io.File

data class FileStream(val displayName: String?, val source: ByteSource?, val fileSize: Long?) {
    companion object {
        fun create(
            contentResolver: ContentResolver?,
            uri: Uri?,
            fileSize: Long?,
        ): FileStream {
            var displayName =
                if (uri?.lastPathSegment == null) {
                    DEFAULT_FILENAME
                } else {
                    FilenameUtils.getName(uri.lastPathSegment)
                }
            val sanitizedUri = FileUtil.normalizeUri(uri)
            val cursor: Cursor? =
                sanitizedUri?.let {
                    contentResolver?.query(
                        it,
                        arrayOf(OpenableColumns.DISPLAY_NAME),
                        null,
                        null,
                        null,
                    )
                }
            if (cursor != null) {
                if (cursor.moveToFirst() && !cursor.isNull(0)) {
                    displayName =
                        FilenameUtils.getName(FileUtil.sanitizeString(cursor.getString(0), ""))
                }
                cursor.close()
            }

            return FileStream(displayName, ContentResolverUriSource(contentResolver, sanitizedUri), fileSize)
        }

        fun create(file: File): FileStream {
            return FileStream(file.name, Files.asByteSource(file), file.length())
        }
    }
}
