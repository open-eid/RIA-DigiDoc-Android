@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil.cache
import ee.ria.DigiDoc.utilsLib.file.FileStream
import java.io.File
import java.io.IOException
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

        @Throws(Exception::class)
        override suspend fun addFilesToContainer(
            context: Context,
            existingSignedContainer: SignedContainer?,
            documentStreams: List<FileStream>,
        ): SignedContainer {
            return SignedContainer.open(context, existingSignedContainer?.getContainerFile())
                .addDataFiles(
                    context,
                    cacheFileStreams(
                        context,
                        getContainerFiles(context, existingSignedContainer?.getContainerFile(), documentStreams),
                    ),
                )
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

        @Throws(Exception::class)
        private suspend fun getContainerFiles(
            context: Context,
            containerFile: File?,
            documentStreams: List<FileStream>,
        ): List<FileStream> {
            val fileStreamList: ArrayList<FileStream> = ArrayList()
            val fileNamesInContainer: List<String> = getFileNamesInContainer(context, containerFile)
            val fileNamesToAdd: List<String> = getFileNamesToAddToContainer(documentStreams)
            for (i in fileNamesToAdd.indices) {
                if (!fileNamesInContainer.contains(fileNamesToAdd[i])) {
                    fileStreamList.add(documentStreams[i])
                }
            }

            if (fileStreamList.isEmpty()) {
                return documentStreams
            }

            return fileStreamList
        }

        @Throws(Exception::class)
        private suspend fun getFileNamesInContainer(
            context: Context,
            containerFile: File?,
        ): List<String> {
            val containerFileNames: MutableList<String> = java.util.ArrayList()
            val dataFiles: List<DataFileInterface> =
                SignedContainer.open(context, containerFile).getDataFiles()

            for (i in dataFiles.indices) {
                containerFileNames.add(dataFiles[i].fileName)
            }

            return containerFileNames
        }

        private fun getFileNamesToAddToContainer(documentStreams: List<FileStream>): List<String> {
            val documentNamesToAdd: MutableList<String> = ArrayList()
            for (fileStream in documentStreams) {
                fileStream.displayName?.let { documentNamesToAdd.add(it) }
            }

            return documentNamesToAdd
        }

        @Throws(IOException::class)
        private suspend fun cacheFileStreams(
            context: Context,
            fileStreams: List<FileStream>,
        ): List<File> {
            val fileBuilder: ArrayList<File> = ArrayList()
            for (fileStream in fileStreams) {
                cache(context, fileStream)?.let { fileBuilder.add(it) }
            }
            return fileBuilder
        }
    }
