@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
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
            fileChooser: ActivityResultLauncher<String>,
            contractType: String,
        ) {
            return fileChooser.launch(contractType)
        }

        override suspend fun removeSignature(signature: SignatureInterface): SignedContainer {
            return SignedContainer.container().removeSignature(signature)
        }

        @Throws(Exception::class)
        override suspend fun addFilesToContainer(
            context: Context,
            documentStreams: List<FileStream>,
        ): SignedContainer {
            return SignedContainer.container()
                .addDataFiles(
                    cacheFileStreams(
                        context,
                        getContainerFiles(documentStreams),
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
            val files = mutableListOf<File>()

            uris.forEachIndexed { _, uri ->
                val file = uriToFile(context, contentResolver, uri)
                if (isFileSizeValid(file)) {
                    files.add(file)
                } else if (uris.size == 1) {
                    throw EmptyFileException(context)
                }
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

        override fun isEmptyFileInList(fileStreams: List<FileStream>): Boolean {
            return ContainerUtil.isEmptyFileInList(fileStreams)
        }

        override fun parseUris(
            contentResolver: ContentResolver?,
            uris: List<Uri>,
        ): List<FileStream> {
            return ContainerUtil.parseUris(contentResolver, uris)
        }

        override fun getFilesWithValidSize(fileStreams: List<FileStream>): List<FileStream> {
            return ContainerUtil.getFilesWithValidSize(fileStreams)
        }

        @Throws(Exception::class)
        private fun getContainerFiles(documentStreams: List<FileStream>): List<FileStream> {
            val fileStreamList: ArrayList<FileStream> = ArrayList()
            val fileNamesInContainer: List<String> = getFileNamesInContainer()
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
        private fun getFileNamesInContainer(): List<String> {
            val containerFileNames: MutableList<String> = java.util.ArrayList()
            val dataFiles: List<DataFileInterface> =
                SignedContainer.container().getDataFiles()

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
                ContainerUtil.cache(context, fileStream)?.let { fileBuilder.add(it) }
            }
            return fileBuilder
        }
    }
