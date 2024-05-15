@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.file.FileStream
import java.io.File

interface FileOpeningRepository {
    suspend fun isFileSizeValid(file: File): Boolean

    suspend fun uriToFile(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri,
    ): File

    suspend fun showFileChooser(
        fileChooser: ActivityResultLauncher<String>,
        contractType: String,
    )

    @Throws(
        EmptyFileException::class,
        NoSuchElementException::class,
        NoInternetConnectionException::class,
        Exception::class,
    )
    suspend fun openOrCreateContainer(
        context: Context,
        contentResolver: ContentResolver,
        uris: List<Uri>,
    ): SignedContainer

    @Throws(
        Exception::class,
    )
    suspend fun addFilesToContainer(
        context: Context,
        existingSignedContainer: SignedContainer?,
        documentStreams: List<FileStream>,
    ): SignedContainer

    suspend fun checkForValidFiles(files: List<File>)

    fun isEmptyFileInList(fileStreams: List<FileStream>): Boolean

    fun parseUris(
        contentResolver: ContentResolver?,
        uris: List<Uri>,
    ): List<FileStream>

    fun getFilesWithValidSize(fileStreams: List<FileStream>): List<FileStream>
}
