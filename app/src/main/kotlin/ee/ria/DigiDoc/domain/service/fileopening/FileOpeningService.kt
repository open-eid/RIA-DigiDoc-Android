@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service.fileopening

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException

interface FileOpeningService {
    suspend fun isFileSizeValid(file: File): Boolean

    @Throws(FileNotFoundException::class, SecurityException::class)
    suspend fun uriToFile(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri,
    ): File
}
