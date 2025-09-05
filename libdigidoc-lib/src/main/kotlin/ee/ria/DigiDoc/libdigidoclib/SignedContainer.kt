@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.common.Constant.NON_LEGACY_CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.exceptions.SSLHandshakeException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.isPDF
import ee.ria.DigiDoc.utilsLib.extensions.isSignedPDF
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.libdigidocpp.Container
import ee.ria.libdigidocpp.ContainerOpenCB
import ee.ria.libdigidocpp.Signature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

private const val LOG_TAG = "SignedContainer"

@Singleton
class SignedContainer
    @Inject
    constructor(
        private val context: Context,
        private val container: Container? = null,
        private var containerFile: File? = null,
        private val isExistingContainer: Boolean = false,
        private val timestamps: List<SignatureInterface>? = emptyList(),
    ) : ee.ria.DigiDoc.common.container.Container {
        suspend fun getDataFiles(): List<DataFileInterface> {
            return CoroutineScope(IO)
                .async {
                    val wrappedDataFile =
                        container
                            ?.dataFiles()
                            ?.mapNotNull { DataFileWrapper(it) } ?: emptyList()
                    return@async wrappedDataFile
                }.await()
        }

        @Throws(Exception::class)
        suspend fun getNestedTimestampedContainer(): SignedContainer? {
            if ((containerMimetype().equals(ASICS_MIMETYPE, ignoreCase = true) && getDataFiles().size == 1) ||
                isCades() &&
                !isXades()
            ) {
                val dataFile = container?.dataFiles()?.firstOrNull()
                val containerRawFile = containerFile

                if (containerRawFile != null && dataFile != null) {
                    val containerDataFilesDir = ContainerUtil.getContainerDataFilesDir(context, containerRawFile)
                    val nestedTimestampedFile = getDataFile(DataFileWrapper(dataFile), containerDataFilesDir)

                    return nestedTimestampedFile?.let {
                        SignedContainer(
                            context = context,
                            container = open(context, it, true).rawContainer(),
                            containerFile = it,
                            isExistingContainer = true,
                        )
                    }
                }
            }

            return null
        }

        fun getTimestamps(): List<SignatureInterface>? = timestamps

        suspend fun getSignatures(thread: CoroutineContext = IO): List<SignatureInterface> =
            withContext(thread) {
                try {
                    container
                        ?.signatures()
                        ?.filterNotNull()
                        ?.mapNotNull { signature ->
                            try {
                                SignatureWrapper(signature)
                            } catch (e: Exception) {
                                errorLog(LOG_TAG, "Failed to wrap signature", e)
                                null
                            }
                        } ?: emptyList()
                } catch (e: Exception) {
                    errorLog(LOG_TAG, "Unable to get container signatures", e)
                    emptyList()
                }
            }

        fun isSigned(): Boolean = container?.signatures()?.isNotEmpty() ?: false

        fun getName(): String = containerFile?.name ?: DEFAULT_FILENAME

        suspend fun setName(filename: String) {
            val name = sanitizeString(filename, "")
            val containerName = name?.let { ContainerUtil.addExtensionToContainerFilename(it) }
            val newFile = containerName?.let { File(containerFile?.parent, it) }

            if (newFile != null) {
                withContext(IO) {
                    containerFile?.renameTo(newFile)
                    containerFile = newFile

                    containerFile?.let {
                        container?.save(newFile.path)
                    }
                }
            }
        }

        fun isExistingContainer(): Boolean = isExistingContainer

        fun getContainerFile(): File? = containerFile

        @Throws(Exception::class)
        fun removeSignature(signature: SignatureInterface) {
            val signatures = container?.signatures()
            if (!signatures.isNullOrEmpty()) {
                for (i in signatures.indices) {
                    if (signature.id == signatures[i].id()) {
                        container.removeSignature(i.toLong())
                        break
                    }
                }
                container.save()
            }
        }

        @Throws(Exception::class)
        fun getDataFile(
            dataFile: DataFileInterface,
            directory: File?,
        ): File? {
            val file = sanitizeString(dataFile.fileName, "")?.let { File(directory, it) }
            val dataFiles = container?.dataFiles()
            if (dataFiles != null) {
                for (i in dataFiles.indices) {
                    val containerDataFile = dataFiles[i]
                    if (dataFile.id == containerDataFile.id()) {
                        if (file != null) {
                            containerDataFile.saveAs(file.absolutePath)
                        }
                        return file
                    }
                }
            }
            throw IllegalArgumentException("Could not find file ${dataFile.id} in container ${containerFile?.name}")
        }

        @Throws(Exception::class)
        suspend fun removeDataFile(dataFile: DataFileInterface) {
            if ((container?.dataFiles()?.size ?: 0) == 1) {
                throw ContainerDataFilesEmptyException()
            }

            val dataFiles = container?.dataFiles()
            if (dataFiles != null) {
                withContext(IO) {
                    for (i in dataFiles.indices) {
                        if (dataFile.id == dataFiles[i].id()) {
                            container.removeDataFile(i.toLong())
                            break
                        }
                    }

                    container.save()
                }
            }
        }

        fun rawContainer(): Container? = container

        fun containerMimetype(): String? = container?.mediaType()

        fun isLegacy(): Boolean =
            containerFile?.let {
                !NON_LEGACY_CONTAINER_EXTENSIONS.contains(it.extension)
            } ?: false

        fun isXades(): Boolean =
            containerMimetype().equals(ASICS_MIMETYPE, true) &&
                container
                    ?.signatures()
                    ?.stream()
                    ?.anyMatch { signature: Signature? ->
                        signature?.profile()?.lowercase()?.contains("bes") ?: false
                    } ?: false

        fun isCades(): Boolean =
            container
                ?.signatures()
                ?.stream()
                ?.anyMatch { signature: Signature? ->
                    signature?.profile()?.lowercase()?.contains("cades") ?: false
                } ?: false

        suspend fun getSignaturesStatusCount(): Map<ValidatorInterface.Status, Int> {
            val counts =
                mutableMapOf(
                    ValidatorInterface.Status.Valid to 0,
                    ValidatorInterface.Status.Unknown to 0,
                    ValidatorInterface.Status.Invalid to 0,
                )

            val signatures = getSignatures(Main)

            for (signature in signatures) {
                signature.validator.status.let { status ->
                    counts[status] = counts[status]?.plus(1) ?: 1
                }
            }

            return counts.toMap()
        }

        fun isSignedPDF(): Boolean = containerFile?.isSignedPDF(context) ?: false

        companion object {
            @Throws(Exception::class)
            fun addDataFiles(
                context: Context,
                signedContainer: SignedContainer,
                dataFiles: List<File?>,
            ) {
                for (dataFile in dataFiles) {
                    if (dataFile != null) {
                        signedContainer.rawContainer()?.addDataFile(dataFile.absolutePath, dataFile.mimeType(context))
                    }
                }
                signedContainer.rawContainer()?.save()
            }

            @Throws(Exception::class)
            suspend fun openOrCreate(
                @ApplicationContext context: Context,
                file: File,
                dataFiles: List<File?>?,
                isSivaConfirmed: Boolean,
                forceFirstDataFileContainer: Boolean = false,
            ): SignedContainer {
                val isFirstDataFileContainer =
                    if (forceFirstDataFileContainer) {
                        false
                    } else {
                        dataFiles?.firstOrNull()?.run {
                            isContainer(context) || (isPDF(context) && isSignedPDF(context))
                        } ?: false
                    }

                var containerFileWithExtension = file

                if ((!isFirstDataFileContainer || (dataFiles?.size ?: 0) > 1) &&
                    !file.path.endsWith(".$DEFAULT_CONTAINER_EXTENSION")
                ) {
                    containerFileWithExtension =
                        File(
                            FilenameUtils.removeExtension(file.path) + ".$DEFAULT_CONTAINER_EXTENSION",
                        )
                }

                return if (dataFiles != null && dataFiles.size == 1 && isFirstDataFileContainer) {
                    open(context, containerFileWithExtension, isSivaConfirmed)
                } else {
                    create(context, containerFileWithExtension, dataFiles)
                }
            }

            @Throws(Exception::class)
            private suspend fun create(
                context: Context,
                file: File,
                dataFiles: List<File?>?,
            ): SignedContainer {
                if (dataFiles.isNullOrEmpty()) {
                    throw NoSuchElementException("Cannot create an empty container")
                }

                val container =
                    try {
                        withContext(IO) {
                            Container.create(file.path)
                        }
                    } catch (e: Exception) {
                        handleContainerException(context, e)
                    } ?: throw IOException("Container creation failed")

                dataFiles.forEachIndexed { index, dataFile ->
                    dataFile?.let {
                        debugLog(
                            LOG_TAG,
                            "Adding datafile '${dataFile.name}'. File ${index + 1} / ${dataFiles.size}",
                        )
                        try {
                            container.addDataFile(it.absolutePath, it.mimeType(context))
                        } catch (e: Exception) {
                            errorLog(LOG_TAG, "Unable to add file to container. ${e.localizedMessage}")
                        }
                    } ?: run {
                        errorLog(LOG_TAG, "Unable to add file to container")
                    }
                }

                if (container.dataFiles().isEmpty()) {
                    throw NoSuchElementException("No valid data files in the container")
                }

                try {
                    container.save()
                } catch (e: Exception) {
                    handleContainerException(context, e)
                }

                return SignedContainer(context, container, file, false)
            }

            @Throws(Exception::class)
            private suspend fun open(
                context: Context,
                file: File?,
                isSivaConfirmed: Boolean,
            ): SignedContainer =
                try {
                    val openedContainer =
                        withContext(IO) {
                            Container.open(file?.path ?: "", DigidocContainerOpenCB(isSivaConfirmed))
                        }
                    SignedContainer(context, openedContainer, file, true)
                } catch (e: Exception) {
                    handleContainerException(context, e)
                }

            private fun handleContainerException(
                context: Context,
                e: Exception,
            ): Nothing {
                val message = e.message ?: ""

                when {
                    message.startsWith("Failed to connect to host") ||
                        message.startsWith("Failed to create proxy connection with host") ||
                        message.startsWith("Failed to create connection with host") -> {
                        throw NoInternetConnectionException(context)
                    }

                    message.startsWith("Failed to create ssl connection with host") -> {
                        throw SSLHandshakeException(context)
                    }

                    else -> {
                        throw IOException(e.message)
                    }
                }
            }
        }

        class DigidocContainerOpenCB(
            private val validate: Boolean,
        ) : ContainerOpenCB() {
            override fun validateOnline(): Boolean = validate
        }
    }
