/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.common.io.ByteStreams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.PDF_MIMETYPE
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.common.container.Container
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.ContainerFileOpeningResult
import ee.ria.DigiDoc.domain.model.notifications.ContainerNotificationType
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.isContainer
import ee.ria.DigiDoc.utilsLib.extensions.isCryptoContainer
import ee.ria.DigiDoc.utilsLib.extensions.isSignedPDF
import ee.ria.DigiDoc.utilsLib.extensions.isXades
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedContainerViewModel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val contentResolver: ContentResolver,
    ) : ViewModel() {
        private val _signedContainer = MutableStateFlow<SignedContainer?>(null)
        val signedContainer: StateFlow<SignedContainer?> = _signedContainer.asStateFlow()

        private val _cryptoContainer = MutableStateFlow<CryptoContainer?>(null)
        val cryptoContainer: StateFlow<CryptoContainer?> = _cryptoContainer.asStateFlow()

        private val _nestedContainers = mutableStateListOf<Container?>()
        val nestedContainers: List<Container?> get() = _nestedContainers

        private val _signedMidStatus = MutableStateFlow<MobileCreateSignatureProcessStatus?>(null)
        val signedMidStatus: StateFlow<MobileCreateSignatureProcessStatus?> = _signedMidStatus.asStateFlow()

        private val _signedSidStatus = MutableStateFlow<SessionStatusResponseProcessStatus?>(null)
        val signedSidStatus: StateFlow<SessionStatusResponseProcessStatus?> = _signedSidStatus.asStateFlow()

        private val _signedNFCStatus = MutableStateFlow<Boolean?>(null)
        val signedNFCStatus: StateFlow<Boolean?> = _signedNFCStatus.asStateFlow()

        private val _signedIDCardStatus = MutableStateFlow<Boolean?>(null)
        val signedIDCardStatus: StateFlow<Boolean?> = _signedIDCardStatus.asStateFlow()

        private val _decryptNFCStatus = MutableStateFlow<Boolean?>(null)
        val decryptNFCStatus: StateFlow<Boolean?> = _decryptNFCStatus.asStateFlow()

        private val _decryptIDCardStatus = MutableStateFlow<Boolean?>(null)
        val decryptIDCardStatus: StateFlow<Boolean?> = _decryptIDCardStatus.asStateFlow()

        private val _containerEncrypted = MutableStateFlow<Boolean?>(null)
        val containerEncrypted: StateFlow<Boolean?> = _containerEncrypted.asStateFlow()

        private val _externalFileUris = MutableStateFlow<List<Uri>>(listOf())
        val externalFileUris: StateFlow<List<Uri>> = _externalFileUris.asStateFlow()

        private val _containerNotifications = MutableStateFlow<List<ContainerNotificationType>>(listOf())
        val containerNotifications: StateFlow<List<ContainerNotificationType>> = _containerNotifications.asStateFlow()

        private val _isSivaConfirmed = MutableStateFlow<Boolean>(true)
        val isSivaConfirmed: StateFlow<Boolean> = _isSivaConfirmed.asStateFlow()

        private val _addedFilesCount = MutableStateFlow<Int>(0)
        val addedFilesCount: StateFlow<Int> = _addedFilesCount.asStateFlow()

        fun setSignedSidStatus(signedStatus: SessionStatusResponseProcessStatus?) {
            _signedSidStatus.value = signedStatus
        }

        fun setSignedMidStatus(signedStatus: MobileCreateSignatureProcessStatus?) {
            _signedMidStatus.value = signedStatus
        }

        fun setSignedNFCStatus(signedStatus: Boolean?) {
            _signedNFCStatus.value = signedStatus
        }

        fun setDecryptNFCStatus(decryptStatus: Boolean?) {
            _decryptNFCStatus.value = decryptStatus
        }

        fun setSignedIDCardStatus(signedStatus: Boolean?) {
            _signedIDCardStatus.value = signedStatus
        }

        fun setDecryptIDCardStatus(decryptStatus: Boolean?) {
            _decryptIDCardStatus.value = decryptStatus
        }

        fun setSignedContainer(signedContainer: SignedContainer?) {
            _signedContainer.value = signedContainer
            addNestedContainer(signedContainer)
        }

        fun setCryptoContainer(
            cryptoContainer: CryptoContainer?,
            overwriteContainer: Boolean = false,
            containerEncrypted: Boolean = false,
        ) {
            _cryptoContainer.value = cryptoContainer
            if (overwriteContainer) {
                removeLastContainer()
            }
            if (containerEncrypted) {
                _containerEncrypted.value = true
            }
            addNestedContainer(cryptoContainer)
        }

        fun resetContainerEncrypted() {
            _containerEncrypted.value = null
        }

        fun setExternalFileUris(uris: List<Uri>) {
            _externalFileUris.value = uris
        }

        fun resetExternalFileUris() {
            _externalFileUris.value = listOf<Uri>()
        }

        fun resetSignedContainer() {
            _signedContainer.value = null
        }

        fun resetCryptoContainer() {
            _cryptoContainer.value = null
        }

        fun removeLastContainer() {
            _nestedContainers.takeIf { it.isNotEmpty() }?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    it.removeLast()
                } else {
                    it.removeAt(it.size - 1)
                }
            }
        }

        fun clearContainers() {
            _nestedContainers.clear()
        }

        fun currentContainer(): Container? = if (_nestedContainers.isNotEmpty()) _nestedContainers.last() else null

        fun isNestedContainer(container: Container?): Boolean =
            nestedContainers.size > 1 && container == currentContainer()

        fun setIsSivaConfirmed(isConfirmed: Boolean) {
            _isSivaConfirmed.value = isConfirmed
        }

        fun resetIsSivaConfirmed() {
            _isSivaConfirmed.value = true
        }

        fun setAddedFilesCount(files: Int) {
            _addedFilesCount.value = files
        }

        fun resetAddedFilesCount() {
            _addedFilesCount.value = 0
        }

        fun openContainerDataFile(
            signedContainer: SignedContainer?,
            clickedDataFile: DataFileInterface?,
            context: Context,
        ): ContainerFileOpeningResult {
            return try {
                if (clickedDataFile == null) {
                    return ContainerFileOpeningResult.Error(IllegalArgumentException("Clicked data file is null"))
                }

                val containerDataFile =
                    getContainerDataFile(
                        signedContainer,
                        clickedDataFile,
                    ) ?: return ContainerFileOpeningResult.Error(IllegalStateException("Container data file is null"))

                val isSignedPDF = containerDataFile.isSignedPDF(context)
                val mainContainerIsSignedPDF = signedContainer?.isSignedPDF() == true

                if (
                    containerDataFile.isContainer(context) ||
                    containerDataFile.isCryptoContainer() ||
                    (!mainContainerIsSignedPDF && isSignedPDF)
                ) {
                    ContainerFileOpeningResult.OpenNestedFile(
                        file = containerDataFile,
                        needsSivaDialog = isSivaDialogNeeded(containerDataFile, context, isSignedPDF),
                    )
                } else {
                    ContainerFileOpeningResult.OpenWithFile(containerDataFile)
                }
            } catch (ex: Exception) {
                errorLog("SharedContainerViewModel", "Unable to open container. Unable to get datafiles", ex)
                ContainerFileOpeningResult.Error(ex)
            }
        }

        fun openCryptoContainerDataFile(
            cryptoContainer: CryptoContainer?,
            dataFile: File?,
        ): ContainerFileOpeningResult {
            return try {
                if (dataFile == null) {
                    return ContainerFileOpeningResult.Error(IllegalArgumentException("Clicked data file is null"))
                }

                val containerDataFile =
                    getCryptoContainerDataFile(
                        cryptoContainer,
                        dataFile,
                    )
                        ?: return ContainerFileOpeningResult.Error(IllegalStateException("Container data file is null"))

                val isSignedPDF = containerDataFile.isSignedPDF(context)

                if (
                    containerDataFile.isContainer(context) ||
                    containerDataFile.isCryptoContainer() ||
                    isSignedPDF
                ) {
                    ContainerFileOpeningResult.OpenNestedFile(
                        file = containerDataFile,
                        needsSivaDialog = isSivaDialogNeeded(containerDataFile, context, isSignedPDF),
                    )
                } else {
                    ContainerFileOpeningResult.OpenWithFile(containerDataFile)
                }
            } catch (ex: Exception) {
                errorLog("SharedContainerViewModel", "Unable to open container. Unable to get datafiles", ex)
                ContainerFileOpeningResult.Error(ex)
            }
        }

        private fun isSivaDialogNeeded(
            file: File,
            context: Context,
            isSignedPDF: Boolean,
        ): Boolean {
            val mimetype = file.mimeType(context)
            val isXades = file.isXades(context)
            val isCades = file.isCades(context)
            val isSivaCandidate = SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(mimetype) && !isXades
            val isSignedPdfFile = PDF_MIMETYPE == mimetype && isSignedPDF
            return isSivaCandidate || isSignedPdfFile || isCades
        }

        @Throws(Exception::class)
        fun getCryptoContainerDataFile(
            cryptoContainer: CryptoContainer?,
            dataFile: File,
        ): File? =
            cryptoContainer?.getDataFile(
                dataFile,
                cryptoContainer.file.let {
                    ContainerUtil.getContainerDataFilesDir(
                        context,
                        it,
                    )
                },
            )

        @Throws(Exception::class)
        suspend fun removeCryptoContainerDataFile(
            cryptoContainer: CryptoContainer?,
            dataFile: File?,
        ) {
            dataFile?.let { cryptoContainer?.removeDataFile(it) }
            _cryptoContainer.value = null
            delay(100L)
            _cryptoContainer.value = cryptoContainer
        }

        @Throws(Exception::class)
        suspend fun removeRecipient(
            cryptoContainer: CryptoContainer?,
            recipient: Addressee?,
        ) {
            recipient?.let { cryptoContainer?.removeRecipient(it) }
            _cryptoContainer.value = null
            delay(100L)
            _cryptoContainer.value = cryptoContainer
        }

        @Throws(Exception::class)
        fun getContainerDataFile(
            signedContainer: SignedContainer?,
            dataFile: DataFileInterface,
        ): File? =
            signedContainer?.getDataFile(
                dataFile,
                signedContainer.getContainerFile()?.let {
                    ContainerUtil.getContainerDataFilesDir(
                        context,
                        it,
                    )
                },
            )

        @Throws(Exception::class)
        suspend fun removeContainerDataFile(
            signedContainer: SignedContainer?,
            dataFile: DataFileInterface?,
        ) {
            dataFile?.let { signedContainer?.removeDataFile(it) }
            _signedContainer.value = null
            delay(100L)
            _signedContainer.value = signedContainer
        }

        @Throws(FileNotFoundException::class, IOException::class)
        fun saveContainerFile(
            documentFile: File,
            activityResult: ActivityResult,
        ) {
            FileInputStream(documentFile).use { inputStream ->
                activityResult.data?.data?.let {
                    contentResolver
                        .openOutputStream(it)
                        .use { outputStream ->
                            outputStream ?: throw FileNotFoundException("Unable to open output stream for URI: $it")
                            ByteStreams.copy(inputStream, outputStream)
                        }
                }
            }
        }

        suspend fun removeSignature(
            signedContainer: SignedContainer?,
            signature: SignatureInterface?,
        ) {
            signature?.let { signedContainer?.removeSignature(it) }
            _signedContainer.value = null
            delay(100L)
            _signedContainer.value = signedContainer
        }

        fun setContainerNotifications(notifications: List<ContainerNotificationType>) {
            _containerNotifications.value = notifications
        }

        fun resetContainerNotifications() {
            _containerNotifications.value = listOf()
        }

        private fun addNestedContainer(container: Container?) {
            if (container != null && !nestedContainers.contains(container)) {
                _nestedContainers.add(container)
            }
        }
    }
