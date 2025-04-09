@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository.fileopening

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File
import java.io.FileNotFoundException

interface FileOpeningRepository {
    suspend fun isFileSizeValid(file: File): Boolean

    @Throws(FileNotFoundException::class, SecurityException::class)
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
        isSivaConfirmed: Boolean,
        forceFirstDataFileContainer: Boolean = false,
    ): SignedContainer

    @Throws(
        EmptyFileException::class,
        NoSuchElementException::class,
        NoInternetConnectionException::class,
        Exception::class,
    )
    suspend fun openOrCreateCryptoContainer(
        context: Context,
        contentResolver: ContentResolver,
        uris: List<Uri>,
    ): CryptoContainer

    @Throws(
        Exception::class,
    )
    suspend fun addFilesToContainer(
        context: Context,
        signedContainer: SignedContainer,
        documents: List<File>,
    )

    @Throws(
        Exception::class,
    )
    suspend fun addFilesToContainer(
        context: Context,
        cryptoContainer: CryptoContainer,
        documents: List<File>,
    )

    suspend fun checkForValidFiles(files: List<File>)

    fun isEmptyFileInList(files: List<File>): Boolean

    fun getValidFiles(
        files: List<File>,
        container: SignedContainer?,
    ): List<File>

    fun getValidFiles(
        files: List<File>,
        container: CryptoContainer?,
    ): List<File>

    fun getFilesWithValidSize(files: List<File>): List<File>

    fun isFileAlreadyInContainer(
        file: File,
        container: SignedContainer,
    ): Boolean

    fun isFileAlreadyInContainer(
        file: File,
        container: CryptoContainer,
    ): Boolean

    fun isSivaConfirmationNeeded(
        context: Context,
        files: List<File>,
    ): Boolean
}
