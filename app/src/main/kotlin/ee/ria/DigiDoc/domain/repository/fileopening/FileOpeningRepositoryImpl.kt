@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository.fileopening

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningService
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.exceptions.EmptyFileException
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
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
        private val sivaService: SivaService,
        private val cdoc2Settings: CDOC2Settings,
    ) : FileOpeningRepository {
        override suspend fun isFileSizeValid(file: File): Boolean = fileOpeningService.isFileSizeValid(file)

        @Throws(FileNotFoundException::class, SecurityException::class)
        override suspend fun uriToFile(
            context: Context,
            contentResolver: ContentResolver,
            uri: Uri,
        ): File = fileOpeningService.uriToFile(context, contentResolver, uri)

        override suspend fun showFileChooser(
            fileChooser: ActivityResultLauncher<String>,
            contractType: String,
        ) = fileChooser.launch(contractType)

        @Throws(Exception::class)
        override suspend fun addFilesToContainer(
            context: Context,
            signedContainer: SignedContainer,
            documents: List<File>,
        ): Unit =
            SignedContainer
                .addDataFiles(
                    context,
                    signedContainer,
                    cacheFiles(
                        context,
                        getContainerFiles(signedContainer, documents),
                    ),
                )

        @Throws(Exception::class)
        override suspend fun addFilesToContainer(
            context: Context,
            cryptoContainer: CryptoContainer,
            documents: List<File>,
        ): Unit =
            cryptoContainer
                .addDataFiles(
                    cacheFiles(
                        context,
                        getContainerFiles(cryptoContainer, documents),
                    ),
                )

        @Throws(
            EmptyFileException::class,
            NoSuchElementException::class,
            NoInternetConnectionException::class,
            SecurityException::class,
            Exception::class,
        )
        override suspend fun openOrCreateContainer(
            context: Context,
            contentResolver: ContentResolver,
            uris: List<Uri>,
            isSivaConfirmed: Boolean,
            forceFirstDataFileContainer: Boolean,
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
            return SignedContainer.openOrCreate(
                context,
                containerPath,
                files,
                isSivaConfirmed,
                forceFirstDataFileContainer,
            )
        }

        @Throws(
            EmptyFileException::class,
            NoSuchElementException::class,
            NoInternetConnectionException::class,
            SecurityException::class,
            Exception::class,
        )
        override suspend fun openOrCreateCryptoContainer(
            context: Context,
            contentResolver: ContentResolver,
            uris: List<Uri>,
            forceCreate: Boolean,
        ): CryptoContainer {
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

            val containerPath = ContainerUtil.addCryptoContainer(context, files.first())
            return CryptoContainer.openOrCreate(
                context,
                containerPath,
                files,
                cdoc2Settings,
                forceCreate,
            )
        }

        override suspend fun checkForValidFiles(files: List<File>) {
            if (files.isEmpty()) {
                throw NoSuchElementException()
            }
        }

        override fun isEmptyFileInList(files: List<File>): Boolean = ContainerUtil.isEmptyFileInList(files)

        override fun getValidFiles(
            files: List<File>,
            container: SignedContainer?,
        ): List<File> =
            ContainerUtil.getFilesWithValidSize(files).filter { file ->
                container == null || !isFileAlreadyInContainer(file, container)
            }

        override fun getValidFiles(
            files: List<File>,
            container: CryptoContainer?,
        ): List<File> =
            ContainerUtil.getFilesWithValidSize(files).filter { file ->
                container == null || !isFileAlreadyInContainer(file, container)
            }

        override fun getFilesWithValidSize(files: List<File>): List<File> = ContainerUtil.getFilesWithValidSize(files)

        override fun isFileAlreadyInContainer(
            file: File,
            container: SignedContainer,
        ): Boolean =
            container.rawContainer()?.dataFiles()?.any { it.fileName() == file.name }
                ?: false

        override fun isFileAlreadyInContainer(
            file: File,
            container: CryptoContainer,
        ): Boolean =
            container.dataFiles?.any { it?.name == file.name }
                ?: false

        override fun isSivaConfirmationNeeded(
            context: Context,
            files: List<File>,
        ): Boolean = sivaService.isSivaConfirmationNeeded(context, files)

        @Throws(Exception::class)
        private fun getContainerFiles(
            signedContainer: SignedContainer,
            documents: List<File>,
        ): List<File> {
            val fileList: ArrayList<File> = ArrayList()
            val fileNamesInContainer: List<String> = getFileNamesInContainer(signedContainer)
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
        private fun getContainerFiles(
            cryptoContainer: CryptoContainer,
            documents: List<File>,
        ): List<File> {
            val fileList: ArrayList<File> = ArrayList()
            val fileNamesInContainer: List<String> = getFileNamesInContainer(cryptoContainer)
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
        private fun getFileNamesInContainer(signedContainer: SignedContainer): List<String> {
            val containerFileNames: MutableList<String> = java.util.ArrayList()
            val dataFiles =
                signedContainer.rawContainer()?.dataFiles()

            if (dataFiles != null) {
                for (i in dataFiles.indices) {
                    containerFileNames.add(dataFiles[i].fileName())
                }
            }

            return containerFileNames
        }

        @Throws(Exception::class)
        private fun getFileNamesInContainer(cryptoContainer: CryptoContainer): List<String> {
            val containerFileNames: MutableList<String> = java.util.ArrayList()
            val dataFiles =
                cryptoContainer.dataFiles

            if (dataFiles != null) {
                for (i in dataFiles.indices) {
                    dataFiles[i]?.name?.let { containerFileNames.add(it) }
                }
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
                ContainerUtil.getCacheFile(context, file.name).let { fileBuilder.add(it) }
            }
            return fileBuilder
        }
    }
