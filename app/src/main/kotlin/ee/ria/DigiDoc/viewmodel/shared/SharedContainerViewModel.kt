@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel.shared

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.io.ByteStreams
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.container.Container
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.notifications.ContainerNotificationType
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.DataFileInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        private val _signedContainer = MutableLiveData<SignedContainer?>()
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _cryptoContainer = MutableLiveData<CryptoContainer?>()
        val cryptoContainer: LiveData<CryptoContainer?> = _cryptoContainer

        private val _nestedContainers = mutableStateListOf<Container?>()
        val nestedContainers: List<Container?> get() = _nestedContainers

        private val _signedMidStatus = MutableLiveData<MobileCreateSignatureProcessStatus?>(null)
        val signedMidStatus: LiveData<MobileCreateSignatureProcessStatus?> = _signedMidStatus

        private val _signedSidStatus = MutableLiveData<SessionStatusResponseProcessStatus?>(null)
        val signedSidStatus: LiveData<SessionStatusResponseProcessStatus?> = _signedSidStatus

        private val _signedNFCStatus = MutableLiveData<Boolean?>(null)
        val signedNFCStatus: LiveData<Boolean?> = _signedNFCStatus

        private val _signedIDCardStatus = MutableLiveData<Boolean?>(null)
        val signedIDCardStatus: LiveData<Boolean?> = _signedIDCardStatus

        private val _decryptNFCStatus = MutableLiveData<Boolean?>(null)
        val decryptNFCStatus: LiveData<Boolean?> = _decryptNFCStatus

        private val _decryptIDCardStatus = MutableLiveData<Boolean?>(null)
        val decryptIDCardStatus: LiveData<Boolean?> = _decryptIDCardStatus

        private val _externalFileUris = MutableStateFlow<List<Uri>>(listOf())
        val externalFileUris: StateFlow<List<Uri>> = _externalFileUris

        private val _containerNotifications = MutableStateFlow<List<ContainerNotificationType>>(listOf())
        val containerNotifications: StateFlow<List<ContainerNotificationType>> = _containerNotifications

        private val _isSivaConfirmed = MutableLiveData<Boolean>(true)
        val isSivaConfirmed: LiveData<Boolean> = _isSivaConfirmed

        private val _addedFilesCount = MutableStateFlow<Int>(0)
        val addedFilesCount: StateFlow<Int> = _addedFilesCount

        fun setSignedSidStatus(signedStatus: SessionStatusResponseProcessStatus?) {
            _signedSidStatus.postValue(signedStatus)
        }

        fun setSignedMidStatus(signedStatus: MobileCreateSignatureProcessStatus?) {
            _signedMidStatus.postValue(signedStatus)
        }

        fun setSignedNFCStatus(signedStatus: Boolean?) {
            _signedNFCStatus.postValue(signedStatus)
        }

        fun setDecryptNFCStatus(decryptStatus: Boolean?) {
            _decryptNFCStatus.postValue(decryptStatus)
        }

        fun setSignedIDCardStatus(signedStatus: Boolean?) {
            _signedIDCardStatus.postValue(signedStatus)
        }

        fun setDecryptIDCardStatus(decryptStatus: Boolean?) {
            _decryptIDCardStatus.postValue(decryptStatus)
        }

        fun setSignedContainer(signedContainer: SignedContainer?) {
            _signedContainer.postValue(signedContainer)
            addNestedContainer(signedContainer)
        }

        fun setCryptoContainer(
            cryptoContainer: CryptoContainer?,
            overwriteContainer: Boolean = false,
        ) {
            _cryptoContainer.postValue(cryptoContainer)
            if (overwriteContainer) {
                removeLastContainer()
            }
            addNestedContainer(cryptoContainer)
        }

        fun setExternalFileUris(uris: List<Uri>) {
            _externalFileUris.value = uris
        }

        fun resetExternalFileUris() {
            _externalFileUris.value = listOf<Uri>()
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun resetCryptoContainer() {
            _cryptoContainer.postValue(null)
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

        @Throws(Exception::class)
        fun getCryptoContainerDataFile(
            cryptoContainer: CryptoContainer?,
            dataFile: File,
        ): File? =
            cryptoContainer?.getDataFile(
                dataFile,
                cryptoContainer.file?.let {
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
            _cryptoContainer.postValue(null)
            delay(100L)
            _cryptoContainer.postValue(cryptoContainer)
        }

        @Throws(Exception::class)
        suspend fun removeRecipient(
            cryptoContainer: CryptoContainer?,
            recipient: Addressee?,
        ) {
            recipient?.let { cryptoContainer?.removeRecipient(it) }
            _cryptoContainer.postValue(null)
            delay(100L)
            _cryptoContainer.postValue(cryptoContainer)
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
            _signedContainer.postValue(null)
            delay(100L)
            _signedContainer.postValue(signedContainer)
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
                            if (outputStream != null) {
                                ByteStreams.copy(inputStream, outputStream)
                            }
                        }
                }
            }
        }

        suspend fun removeSignature(
            signedContainer: SignedContainer?,
            signature: SignatureInterface?,
        ) {
            signature?.let { signedContainer?.removeSignature(it) }
            _signedContainer.postValue(null)
            delay(100L)
            _signedContainer.postValue(signedContainer)
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
