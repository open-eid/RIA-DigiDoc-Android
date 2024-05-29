@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import java.io.File
import java.io.FileNotFoundException

interface FileOpeningRepository {
    suspend fun isFileSizeValid(file: File): Boolean

    @Throws(FileNotFoundException::class)
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
        documents: List<File>,
    ): SignedContainer

    suspend fun removeSignature(signature: SignatureInterface): SignedContainer

    suspend fun checkForValidFiles(files: List<File>)

    fun isEmptyFileInList(files: List<File>): Boolean

    fun getValidFiles(
        files: List<File>,
        container: SignedContainer?,
    ): List<File>

    fun getFilesWithValidSize(files: List<File>): List<File>

    fun isFileAlreadyInContainer(
        file: File,
        container: SignedContainer,
    ): Boolean
}
