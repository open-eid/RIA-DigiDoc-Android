@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileOpeningRepositoryImpl
    @Inject
    constructor(
        private val fileOpeningService: FileOpeningService,
    ) : FileOpeningRepository {
        override suspend fun isFileSizeValid(file: File): Boolean {
            return fileOpeningService.isFileSizeValid(file)
        }

        override suspend fun uriToFile(
            context: Context,
            contentResolver: ContentResolver,
            uri: Uri,
        ): File {
            return fileOpeningService.uriToFile(context, contentResolver, uri)
        }

        override suspend fun showFileChooser(
            fileChooser: ManagedActivityResultLauncher<String, List<Uri>>,
            contractType: String,
        ) {
            return fileChooser.launch(contractType)
        }

        @Throws(
            EmptyFileException::class,
            NoSuchElementException::class,
            NoInternetConnectionException::class,
            Exception::class,
        )
        override suspend fun openOrCreateContainer(
            context: Context,
            contentResolver: ContentResolver,
            uris: List<Uri>,
        ): SignedContainer {
            val files =
                uris
                    .map { uri ->
                        val file = uriToFile(context, contentResolver, uri)
                        if (!isFileSizeValid(file)) {
                            throw EmptyFileException(context)
                        }
                        file
                    }

            checkForValidFiles(files)

            val containerPath = ContainerUtil.addSignatureContainer(context, files.first())
            return SignedContainer.openOrCreate(context, containerPath, files)
        }

        override suspend fun checkForValidFiles(files: List<File>) {
            if (files.isEmpty()) {
                throw NoSuchElementException()
            }
        }
    }
