@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib

import android.content.Context
import android.webkit.MimeTypeMap
import com.google.common.io.Files.getFileExtension
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import ee.ria.DigiDoc.common.Constant.CONTAINER_EXTENSIONS
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.Constant.DEFAULT_FILENAME
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureWrapper
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerUninitializedException
import ee.ria.DigiDoc.libdigidoclib.exceptions.NoInternetConnectionException
import ee.ria.DigiDoc.libdigidoclib.exceptions.SSLHandshakeException
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.isPDF
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.libdigidocpp.Container
import ee.ria.libdigidocpp.ContainerOpenCB
import ee.ria.libdigidocpp.DataFiles
import ee.ria.libdigidocpp.Signatures
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.IOException
import java.util.Locale

private const val LOG_TAG = "SignedContainer"

class SignedContainer(dataFiles: List<DataFileInterface>?, signatures: List<SignatureInterface>) {
    fun getDataFiles(): List<DataFileInterface> {
        return container?.dataFiles()?.mapNotNull { DataFileWrapper(it) } ?: emptyList()
    }

    fun getSignatures(): List<SignatureInterface> {
        return container?.signatures()?.mapNotNull { SignatureWrapper(it) } ?: emptyList()
    }

    fun getName(): String {
        return containerFile?.name ?: DEFAULT_FILENAME
    }

    suspend fun setName(
        context: Context,
        filename: String,
    ) {
        val name = sanitizeString(filename, "")
        val containerName = name?.let { ContainerUtil.addExtensionToContainerFilename(it) }
        val newFile = containerName?.let { File(containerFile?.parent, it) }

        if (newFile != null) {
            containerFile?.renameTo(newFile)
            containerFile = newFile

            open(context, containerFile)

            containerFile?.let {
                container?.save()
            }
        }
    }

    fun isExistingContainer(): Boolean {
        return isExistingContainer
    }

    fun getContainerFile(): File? {
        return containerFile
    }

    @Throws(Exception::class)
    fun removeSignature(signature: SignatureInterface): SignedContainer {
        val signatures = container?.signatures()
        if (signatures != null) {
            for (i in signatures.indices) {
                if (signature.id == signatures[i].id()) {
                    container?.removeSignature(i.toLong())
                    break
                }
            }
        }
        container?.save()
        return container()
    }

    @Throws(Exception::class)
    fun addDataFiles(dataFiles: List<File?>): SignedContainer {
        for (dataFile in dataFiles) {
            if (dataFile != null) {
                container?.addDataFile(dataFile.absolutePath, mimeType(dataFile))
            }
        }
        container?.save()
        return container()
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
        throw IllegalArgumentException(
            ("Could not find file " + dataFile.id) +
                " in container " + containerFile?.name,
        )
    }

    @Throws(Exception::class)
    suspend fun removeDataFile(dataFile: DataFileInterface): SignedContainer {
        if ((container?.dataFiles()?.size ?: 0) == 1) {
            throw ContainerDataFilesEmptyException()
        }
        return withContext(IO) {
            val dataFiles = container?.dataFiles()
            if (dataFiles != null) {
                for (i in dataFiles.indices) {
                    if (dataFile.id == dataFiles[i].id()) {
                        container?.removeDataFile(i.toLong())
                        break
                    }
                }
            }
            container?.save()
            container()
        }
    }

    companion object {
        private const val SIGNED_CONTAINER_LOG_TAG = "SignedContainer"
        private var container: Container? = null
        private var containerFile: File? = null
        private var isExistingContainer: Boolean = false

        @Throws(Exception::class)
        suspend fun openOrCreate(
            context: Context,
            file: File,
            dataFiles: List<File?>?,
        ): SignedContainer {
            val isFirstDataFileContainer =
                dataFiles?.firstOrNull()?.run {
                    isContainer || (isPDF && isSignedPDF(context, this))
                } ?: false
            var containerFileWithExtension = file

            if (!isFirstDataFileContainer && !file.path.endsWith(".$DEFAULT_CONTAINER_EXTENSION")) {
                containerFileWithExtension =
                    File(
                        FilenameUtils.removeExtension(file.path) +
                            ".$DEFAULT_CONTAINER_EXTENSION",
                    )
            }

            containerFile = containerFileWithExtension

            return if (dataFiles != null && dataFiles.size == 1 && isFirstDataFileContainer) {
                isExistingContainer = true
                open(context, containerFileWithExtension)
            } else {
                isExistingContainer = false
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

            container =
                try {
                    withContext(IO) {
                        Container.create(file.path)
                    }
                } catch (e: Exception) {
                    container = null
                    handleContainerException(context, e)
                } ?: throw IOException("Container.open returned null")

            dataFiles.forEachIndexed { index, dataFile ->
                dataFile?.let {
                    debugLog(LOG_TAG, "Adding datafile '${dataFile.name}'. File ${index + 1} / ${dataFiles.size}")
                    try {
                        container?.addDataFile(it.absolutePath, it.mimeType)
                    } catch (e: Exception) {
                        errorLog(LOG_TAG, "Unable to add file to container. ${e.localizedMessage}")
                    }
                } ?: run {
                    errorLog(LOG_TAG, "Unable to add file to container")
                }
            }

            if (container?.dataFiles()?.size == 0) {
                container = null
                throw NoSuchElementException("No valid data files")
            }

            container?.save()

            return SignedContainer(
                createDataFilesList(container?.dataFiles()),
                createSignaturesList(container?.signatures()),
            )
        }

        @Throws(Exception::class)
        private suspend fun open(
            context: Context,
            file: File?,
        ): SignedContainer {
            return try {
                val openedContainer =
                    withContext(IO) {
                        Container.open(file?.path ?: "", DigidocContainerOpenCB(true))
                    }
                container = openedContainer
                containerFile = file
                SignedContainer(
                    createDataFilesList(openedContainer.dataFiles()),
                    createSignaturesList(openedContainer.signatures()),
                )
            } catch (e: Exception) {
                container = null
                handleContainerException(context, e)
            }
        }

        @Throws(ContainerUninitializedException::class)
        fun container(): SignedContainer {
            container?.let {
                val dataFiles = it.dataFiles()
                if (dataFiles.isNotEmpty()) {
                    return SignedContainer(createDataFilesList(dataFiles), createSignaturesList(it.signatures()))
                }
            }
            throw ContainerUninitializedException()
        }

        fun rawContainer(): Container? {
            return container
        }

        fun rawContainerFile(): File? {
            return containerFile
        }

        fun isExistingContainer(): Boolean {
            return isExistingContainer
        }

        fun mimeType(file: File): String {
            val extension: String =
                getFileExtension(file.name).lowercase(Locale.getDefault())
            if (CONTAINER_EXTENSIONS.contains(extension)) {
                return CONTAINER_MIME_TYPE
            }
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            return mimeType ?: DEFAULT_MIME_TYPE
        }

        fun containerMimetype(): String? {
            return container?.mediaType()
        }

        /**
         * Reset SignedContainer instance
         */
        fun cleanup() {
            container = null
            containerFile = null
            isExistingContainer = false
        }

        private fun isSignedPDF(
            context: Context,
            file: File,
        ): Boolean {
            PDFBoxResourceLoader.init(context)
            return try {
                PDDocument.load(file).use { document ->
                    document.getSignatureDictionaries().any { signature ->
                        signature.filter == "Adobe.PPKLite" ||
                            signature.subFilter == "ETSI.CAdES.detached" ||
                            signature.subFilter == "adbe.pkcs7.detached"
                    }
                }
            } catch (e: IOException) {
                errorLog(SIGNED_CONTAINER_LOG_TAG, "Unable to check if PDF is signed", e)
                false
            }
        }

        private fun handleContainerException(
            context: Context,
            e: Exception,
        ): Nothing {
            val message = e.message ?: ""

            when {
                message.startsWith("Failed to connect to host") ||
                    message.startsWith("Failed to create proxy connection with host") -> {
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

        private fun createDataFilesList(dataFiles: DataFiles?): List<DataFileInterface> {
            return dataFiles?.map { DataFileWrapper(it) } ?: emptyList()
        }

        private fun createSignaturesList(signatures: Signatures?): List<SignatureInterface> {
            return signatures?.map { SignatureWrapper(it) } ?: emptyList()
        }
    }
}

class DigidocContainerOpenCB(private val validate: Boolean) : ContainerOpenCB() {
    override fun validateOnline(): Boolean {
        return validate
    }
}
