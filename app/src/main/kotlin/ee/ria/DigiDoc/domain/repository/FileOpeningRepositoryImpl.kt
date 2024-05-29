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
import java.io.File
import java.io.FileNotFoundException
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

        @Throws(FileNotFoundException::class)
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
            documents: List<File>,
        ): SignedContainer {
            return SignedContainer.container()
                .addDataFiles(
                    cacheFiles(
                        context,
                        getContainerFiles(documents),
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

        override fun isEmptyFileInList(files: List<File>): Boolean {
            return ContainerUtil.isEmptyFileInList(files)
        }

        override fun getValidFiles(
            files: List<File>,
            container: SignedContainer?,
        ): List<File> {
            return ContainerUtil.getFilesWithValidSize(files).filter { file ->
                container == null || !isFileAlreadyInContainer(file, container)
            }
        }

        override fun getFilesWithValidSize(files: List<File>): List<File> {
            return ContainerUtil.getFilesWithValidSize(files)
        }

        override fun isFileAlreadyInContainer(
            file: File,
            container: SignedContainer,
        ): Boolean {
            return container.getDataFiles().any { it.fileName == file.name }
        }

        @Throws(Exception::class)
        private fun getContainerFiles(documents: List<File>): List<File> {
            val fileList: ArrayList<File> = ArrayList()
            val fileNamesInContainer: List<String> = getFileNamesInContainer()
            val fileNamesToAdd: List<String> = getFileNamesToAddToContainer(documents)
            for (i in fileNamesToAdd.indices) {
                if (!fileNamesInContainer.contains(fileNamesToAdd[i])) {
                    fileList.add(documents[i])
                }
            }

            if (fileList.isEmpty()) {
                return documents
            }

            return fileList
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

        private fun getFileNamesToAddToContainer(documents: List<File>): List<String> {
            val documentNamesToAdd: MutableList<String> = ArrayList()
            for (file in documents) {
                file.name.let { documentNamesToAdd.add(it) }
            }

            return documentNamesToAdd
        }

        @Throws(IOException::class)
        private suspend fun cacheFiles(
            context: Context,
            files: List<File>,
        ): List<File> {
            val fileBuilder: ArrayList<File> = ArrayList()
            for (file in files) {
                ContainerUtil.cache(context, file).let { fileBuilder.add(it) }
            }
            return fileBuilder
        }
    }
