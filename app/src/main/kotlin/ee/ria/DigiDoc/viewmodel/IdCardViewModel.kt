@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx3.asFlow
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Base64
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class IdCardViewModel
    @Inject
    constructor(
        private val smartCardReaderManager: SmartCardReaderManager,
        private val idCardService: IdCardService,
        private val cdoc2Settings: CDOC2Settings,
        private val configurationRepository: ConfigurationRepository,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _idCardStatus = MutableLiveData(SmartCardReaderStatus.IDLE)
        val idCardStatus: LiveData<SmartCardReaderStatus?> = _idCardStatus

        private val _userData = MutableLiveData<IdCardData?>(null)
        val userData: LiveData<IdCardData?> = _userData

        private val _signStatus = MutableLiveData<Boolean?>(null)
        val signStatus: LiveData<Boolean?> = _signStatus

        private val _decryptStatus = MutableLiveData<Boolean?>(null)
        val decryptStatus: LiveData<Boolean?> = _decryptStatus

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer

        private val _cryptoContainer = MutableLiveData<CryptoContainer?>(null)
        val cryptoContainer: LiveData<CryptoContainer?> = _cryptoContainer

        private val _errorState = MutableLiveData<Triple<Int, String?, Int?>?>(null)
        val errorState: LiveData<Triple<Int, String?, Int?>?> = _errorState

        private val _pinErrorState = MutableLiveData<Triple<Int, String?, Int?>?>(null)
        val pinErrorState: LiveData<Triple<Int, String?, Int?>?> = _pinErrorState

        private val _shouldHandleError = MutableStateFlow(false)
        val shouldHandleError: StateFlow<Boolean> = _shouldHandleError

        private val _dialogError = MutableLiveData<String?>(null)
        val dialogError: LiveData<String?> = _dialogError

        init {
            CoroutineScope(Main).launch {
                smartCardReaderManager.status().asFlow().distinctUntilChanged().collect { status ->
                    _idCardStatus.postValue(status)
                }
            }
        }

        suspend fun loadPersonalData() =
            withContext(IO) {
                try {
                    val token =
                        withContext(IO) {
                            Token.create(smartCardReaderManager.connectedReader())
                        }

                    val data = idCardService.data(token)

                    _userData.postValue(data)
                } catch (e: Exception) {
                    _signStatus.postValue(false)
                    _decryptStatus.postValue(false)

                    showGeneralError(e)
                    errorLog(logTag, "Unable to get ID-card personal data: ${e.message}", e)

                    resetValues()
                }
            }

        suspend fun sign(
            activity: Activity,
            signedContainer: SignedContainer,
            pin2Code: ByteArray,
            roleData: RoleData?,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation

            try {
                val token: Token =
                    withContext(Main) {
                        Token.create(smartCardReaderManager.connectedReader())
                    }

                val signedContainerResult: SignedContainer =
                    idCardService.signContainer(token, signedContainer, pin2Code, roleData)

                if (pin2Code.isNotEmpty()) {
                    Arrays.fill(pin2Code, 0.toByte())
                }

                withContext(Main) {
                    _signStatus.postValue(true)
                    _signedContainer.postValue(signedContainerResult)
                }
            } catch (e: Exception) {
                handleSigningError(e, signedContainer)
            } finally {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        suspend fun decrypt(
            activity: Activity,
            context: Context,
            container: CryptoContainer?,
            pin1Code: ByteArray,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation
            if (container != null) {
                try {
                    val token: Token =
                        withContext(Main) {
                            Token.create(smartCardReaderManager.connectedReader())
                        }

                    val authCert = idCardService.data(token).authCertificate.data

                    debugLog(
                        logTag,
                        "Auth certificate: " + Base64.toBase64String(authCert),
                    )
                    val decryptedContainer =
                        CryptoContainer.decrypt(
                            context,
                            container.file,
                            container.recipients,
                            authCert,
                            pin1Code,
                            token,
                            cdoc2Settings,
                            configurationRepository,
                        )
                    if (pin1Code.isNotEmpty()) {
                        Arrays.fill(pin1Code, 0.toByte())
                    }

                    withContext(Main) {
                        _decryptStatus.postValue(true)
                        _cryptoContainer.postValue(decryptedContainer)
                    }
                } catch (e: Exception) {
                    handleDecryptError(e)
                } finally {
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            } else {
                withContext(Main) {
                    _decryptStatus.postValue(false)
                    _errorState.postValue(Triple(R.string.error_general_client, null, null))
                    errorLog(logTag, "Unable to get container value. Container is 'null'")
                }
            }
        }

        suspend fun removePendingSignature(signedContainer: SignedContainer) {
            val signatures = signedContainer.getSignatures(Main)
            if (signatures.isNotEmpty()) {
                val lastSignatureStatus = signatures.last().validator.status
                if (lastSignatureStatus == ValidatorInterface.Status.Invalid ||
                    lastSignatureStatus == ValidatorInterface.Status.Unknown
                ) {
                    signedContainer.removeSignature(signatures.last())
                }
            }
        }

        private fun handleIdentityError(e: Exception) {
            val message = e.message ?: ""

            when {
                e is CodeVerificationException -> handlePinError(e)
                message.contains("Too Many Requests") ->
                    showErrorDialog(
                        e,
                        "Unable to sign with ID-card - Too Many Requests",
                    )
                message.contains("OCSP response not in valid time slot") ->
                    showErrorDialog(
                        e,
                        "Unable to sign with ID-card - OCSP response not in valid time slot",
                    )
                message.contains("Certificate status: revoked") -> showRevokedCertificateError(e)
                message.contains("Failed to connect") ||
                    message.contains("Failed to create connection with host") ->
                    showNetworkError(e)
                message.contains("Failed to create proxy connection with host") -> showProxyError(e)
                message.contains("No lock found with certificate key") -> showNoLockFoundError(e)
                else -> showGeneralError(e)
            }
        }

        private fun handleDecryptError(e: Exception) {
            _decryptStatus.postValue(false)

            handleIdentityError(e)
        }

        private suspend fun handleSigningError(
            e: Exception,
            signedContainer: SignedContainer,
        ) {
            removePendingSignature(signedContainer)
            _signStatus.postValue(false)

            handleIdentityError(e)
        }

        private fun handlePinError(e: CodeVerificationException) {
            val pinRetryCount = e.retries
            val pinErrorMessage =
                when (pinRetryCount) {
                    2 -> Triple(R.string.id_card_sign_pin_invalid, e.type.name, pinRetryCount)
                    1 -> Triple(R.string.id_card_sign_pin_invalid_final, e.type.name, null)
                    0 -> Triple(R.string.id_card_sign_pin_locked, e.type.name, null)
                    else -> Triple(R.string.id_card_sign_pin_wrong, e.type.name, null)
                }
            setShouldHandleError(pinRetryCount == 0)
            _pinErrorState.postValue(pinErrorMessage)
            errorLog(logTag, "Unable to sign / decrypt with ID-card: ${e.message}", e)
        }

        private fun showErrorDialog(
            e: Exception,
            logMessage: String,
        ) {
            _dialogError.postValue(e.message)
            errorLog(logTag, logMessage, e)
        }

        private fun showRevokedCertificateError(e: Exception) {
            _errorState.postValue(
                Triple(
                    R.string.signature_update_signature_error_message_certificate_revoked,
                    null,
                    null,
                ),
            )
            errorLog(logTag, "Unable to sign with ID-card - Certificate status: revoked", e)
        }

        private fun showNetworkError(e: Exception) {
            _errorState.postValue(Triple(R.string.no_internet_connection, null, null))
            errorLog(logTag, "Unable to sign with ID-card - Unable to connect to Internet", e)
        }

        private fun showProxyError(e: Exception) {
            _errorState.postValue(Triple(R.string.main_settings_proxy_invalid_settings, null, null))
            errorLog(logTag, "Unable to sign with ID-card - Unable to create proxy connection with host", e)
        }

        private fun showGeneralError(e: Exception) {
            _errorState.postValue(Triple(R.string.error_general_client, null, null))
            errorLog(logTag, "Unable to sign with ID-card: ${e.message}", e)
        }

        fun setShouldHandleError(value: Boolean) {
            _shouldHandleError.value = value
        }

        private fun showNoLockFoundError(e: Exception) {
            _errorState.postValue(Triple(R.string.no_lock_found, null, null))
            errorLog(logTag, "Unable to decrypt with ID-card - No lock found with certificate key", e)
        }

        fun resetSignStatus() {
            _signStatus.postValue(null)
        }

        fun resetDecryptStatus() {
            _decryptStatus.postValue(null)
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetDialogErrorState() {
            _dialogError.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun resetCryptoContainer() {
            _cryptoContainer.postValue(null)
        }

        fun resetPersonalUserData() {
            _userData.postValue(null)
        }

        private fun resetIdCardStatus() {
            _idCardStatus.postValue(null)
        }

        fun resetPINErrorState() {
            _pinErrorState.postValue(null)
        }

        fun resetShouldHandleError() {
            _shouldHandleError.value = false
        }

        private fun resetValues() {
            resetDialogErrorState()
            resetIdCardStatus()
            resetPersonalUserData()
            resetErrorState()
            resetPINErrorState()
            resetSignStatus()
            resetSignedContainer()
            resetShouldHandleError()
        }
    }
