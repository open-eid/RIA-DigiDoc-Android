@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File

interface FileOpeningRepository {
    suspend fun isFileSizeValid(file: File): Boolean

    suspend fun uriToFile(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri,
    ): File

    suspend fun showFileChooser(
        fileChooser: ManagedActivityResultLauncher<String, List<Uri>>,
        contractType: String,
    )

    suspend fun openOrCreateContainer(
        context: Context,
        contentResolver: ContentResolver,
        uris: List<Uri>,
    ): SignedContainer

    suspend fun checkForValidFiles(files: List<File>)
}
