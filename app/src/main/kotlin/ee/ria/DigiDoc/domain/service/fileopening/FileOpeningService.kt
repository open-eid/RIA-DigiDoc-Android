@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service.fileopening

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import java.io.File

interface FileOpeningService {
    suspend fun isFileSizeValid(file: File): Boolean

    suspend fun uriToFile(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri,
    ): File
}
