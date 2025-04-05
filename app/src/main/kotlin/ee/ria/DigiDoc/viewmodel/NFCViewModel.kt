@file:Suppress("PackageName", "MaxLineLength")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.ImmutableMap
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.CAN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.PaceTunnelException
import ee.ria.DigiDoc.idcard.TokenWithPace
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.smartcardreader.ApduResponseException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import java.util.Arrays
import javax.inject.Inject

@HiltViewModel
class NFCViewModel
    @Inject
    constructor(
        private val nfcSmartCardReaderManager: NfcSmartCardReaderManager,
        private val containerWrapper: ContainerWrapper,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer
        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState
        private val _message = MutableLiveData<Int?>(null)
        val message: LiveData<Int?> = _message
        private val _nfcStatus = MutableLiveData<NfcStatus?>(null)
        val nfcStatus: LiveData<NfcStatus?> = _nfcStatus
        private val _signStatus = MutableLiveData<Boolean?>(null)
        val signStatus: LiveData<Boolean?> = _signStatus
        private val _shouldResetPIN2 = MutableLiveData(false)
        val shouldResetPIN2: LiveData<Boolean> = _shouldResetPIN2
        private val _roleDataRequested = MutableLiveData(false)
        val roleDataRequested: LiveData<Boolean?> = _roleDataRequested
        private val _dialogError = MutableLiveData(0)
        val dialogError: LiveData<Int> = _dialogError

        private val dialogMessages: ImmutableMap<SessionStatusResponseProcessStatus, Int> =
            ImmutableMap.builder<SessionStatusResponseProcessStatus, Int>()
                .put(
                    SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS,
                    R.string.too_many_requests_message,
                )
                .put(
                    SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT,
                    R.string.invalid_time_slot_message,
                )
                .build()

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetSignStatus() {
            _signStatus.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun resetShouldResetPIN2() {
            _shouldResetPIN2.postValue(null)
        }

        fun resetRoleDataRequested() {
            _roleDataRequested.postValue(null)
        }

        fun setRoleDataRequested(roleDataRequested: Boolean) {
            _roleDataRequested.postValue(roleDataRequested)
        }

        fun shouldShowCANNumberError(canNumber: String?): Boolean {
            return (
                !canNumber.isNullOrEmpty() &&
                    !isCANLengthValid(canNumber)
            )
        }

        fun shouldShowPIN2CodeError(pin2Code: ByteArray?): Boolean {
            return (pin2Code != null && pin2Code.isNotEmpty() && !isPIN2LengthValid(pin2Code))
        }

        private fun isPIN2LengthValid(pin2Code: ByteArray): Boolean {
            return pin2Code.size in PIN2_MIN_LENGTH..PIN_MAX_LENGTH
        }

        private fun isCANLengthValid(canNumber: String): Boolean {
            return canNumber.length == CAN_LENGTH
        }

        fun positiveButtonEnabled(
            canNumber: String?,
            pin2Code: ByteArray?,
        ): Boolean {
            if (canNumber != null && pin2Code != null) {
                return isCANLengthValid(canNumber.toString()) &&
                    isPIN2LengthValid(pin2Code)
            }
            return false
        }

        fun getNFCStatus(activity: Activity): NfcStatus {
            return nfcSmartCardReaderManager.detectNfcStatus(activity)
        }

        private fun resetValues() {
            _errorState.postValue(null)
            _message.postValue(null)
            _signStatus.postValue(null)
            _nfcStatus.postValue(null)
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

        suspend fun cancelNFCWorkRequest(signedContainer: SignedContainer) {
            removePendingSignature(signedContainer)

            nfcSmartCardReaderManager.disableNfcReaderMode()
        }

        suspend fun checkNFCStatus(nfcStatus: NfcStatus) {
            withContext(Main) {
                _nfcStatus.postValue(nfcStatus)
                when (nfcStatus) {
                    NfcStatus.NFC_NOT_SUPPORTED -> _message.postValue(R.string.signature_update_nfc_adapter_missing)
                    NfcStatus.NFC_NOT_ACTIVE -> _message.postValue(R.string.signature_update_nfc_turned_off)
                    NfcStatus.NFC_ACTIVE -> _message.postValue(R.string.signature_update_nfc_hold)
                }
            }
        }

        suspend fun performNFCWorkRequest(
            activity: Activity,
            context: Context,
            container: SignedContainer?,
            pin2Code: ByteArray?,
            canNumber: String,
            roleData: RoleData?,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetValues()

            if (container != null) {
                withContext(Main) {
                    _message.postValue(R.string.signature_update_nfc_hold)
                }

                checkNFCStatus(
                    nfcSmartCardReaderManager.startDiscovery(activity) { nfcReader, exc ->
                        if ((nfcReader != null) && (exc == null)) {
                            try {
                                CoroutineScope(Main).launch {
                                    _message.postValue(R.string.signature_update_nfc_detected)
                                }
                                val card = TokenWithPace.create(nfcReader)
                                card.tunnel(canNumber)
                                val signerCert = card.certificate(CertificateType.SIGNING)
                                debugLog(logTag, "Signer certificate: " + Base64.toBase64String(signerCert))

                                val dataToSignBytes = containerWrapper.prepareSignature(container, signerCert, roleData)

                                val signatureArray = card.calculateSignature(pin2Code, dataToSignBytes, true)
                                if (null != pin2Code && pin2Code.isNotEmpty()) {
                                    Arrays.fill(pin2Code, 0.toByte())
                                }
                                debugLog(logTag, "Signature: " + Hex.toHexString(signatureArray))

                                containerWrapper.finalizeSignature(container, signatureArray)

                                CoroutineScope(Main).launch {
                                    _shouldResetPIN2.postValue(true)
                                    _signStatus.postValue(true)
                                    _signedContainer.postValue(container)
                                }
                            } catch (ex: SmartCardReaderException) {
                                _signStatus.postValue(false)
                                CoroutineScope(IO).launch {
                                    removePendingSignature(container)
                                }

                                if (ex.message?.contains("TagLostException") == true) {
                                    _errorState.postValue(context.getString(R.string.signature_update_nfc_tag_lost))
                                } else if (ex.message?.contains("PIN2 verification failed") == true &&
                                    ex.message?.contains("Retries left: 2") == true
                                ) {
                                    _shouldResetPIN2.postValue(true)
                                    _errorState.postValue(
                                        context.getString(R.string.signature_update_id_card_sign_pin2_invalid, 2),
                                    )
                                } else if (ex.message?.contains("PIN2 verification failed") == true &&
                                    ex.message?.contains("Retries left: 1") == true
                                ) {
                                    _shouldResetPIN2.postValue(true)
                                    _errorState.postValue(
                                        context.getString(R.string.signature_update_id_card_sign_pin2_invalid_final),
                                    )
                                } else if (ex.message?.contains("PIN2 verification failed") == true &&
                                    ex.message?.contains("Retries left: 0") == true
                                ) {
                                    _shouldResetPIN2.postValue(true)
                                    _errorState.postValue(
                                        context.getString(R.string.signature_update_id_card_sign_pin2_locked),
                                    )
                                } else if (ex is ApduResponseException) {
                                    _errorState.postValue(
                                        context.getString(R.string.signature_update_nfc_technical_error),
                                    )
                                } else if (ex is PaceTunnelException) {
                                    _errorState.postValue(
                                        context.getString(R.string.signature_update_nfc_wrong_can),
                                    )
                                } else {
                                    _errorState.postValue(
                                        ex.message ?: context.getString(R.string.signature_update_nfc_technical_error),
                                    )
                                }

                                errorLog(logTag, "Exception: " + ex.message, ex)
                            } catch (ex: Exception) {
                                _signStatus.postValue(false)
                                _shouldResetPIN2.postValue(true)

                                val message = ex.message ?: ""

                                when {
                                    message.contains("Failed to connect") ||
                                        message.contains("Failed to create connection with host") ->
                                        showNetworkError(
                                            context,
                                            ex,
                                        )
                                    message.contains(
                                        "Failed to create proxy connection with host",
                                    ) -> showProxyError(context, ex)
                                    message.contains("Too Many Requests") ->
                                        setErrorState(
                                            context,
                                            SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS,
                                        )
                                    message.contains("OCSP response not in valid time slot") ->
                                        setErrorState(
                                            context,
                                            SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT,
                                        )
                                    else -> showTechnicalError(context, ex)
                                }

                                errorLog(logTag, "Exception: " + ex.message, ex)
                            } finally {
                                nfcSmartCardReaderManager.disableNfcReaderMode()
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    },
                )
            } else {
                withContext(Main) {
                    _nfcStatus.postValue(nfcSmartCardReaderManager.detectNfcStatus(activity))
                    _signStatus.postValue(false)
                    _errorState.postValue(context.getString(R.string.error_general_client))
                    errorLog(logTag, "Unable to get container value. Container is 'null'")
                }
            }
        }

        fun handleBackButton() {
            _shouldResetPIN2.postValue(true)
            resetValues()
        }

        fun resetDialogErrorState() {
            _dialogError.postValue(0)
        }

        private fun setErrorState(
            context: Context,
            status: SessionStatusResponseProcessStatus,
        ) {
            val res = dialogMessages[status]

            if (res == R.string.too_many_requests_message ||
                res == R.string.invalid_time_slot_message
            ) {
                _dialogError.postValue(res)
            } else {
                _errorState.postValue(
                    res?.let {
                        context.getString(
                            it,
                        )
                    },
                )
            }
        }

        private fun showNetworkError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.no_internet_connection))
            errorLog(logTag, "Unable to sign with NFC - Unable to connect to Internet", e)
        }

        private fun showProxyError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.main_settings_proxy_invalid_settings))
            errorLog(logTag, "Unable to sign with NFC - Unable to create proxy connection with host", e)
        }

        private fun showTechnicalError(
            context: Context,
            e: Exception,
        ) {
            _errorState.postValue(context.getString(R.string.signature_update_nfc_technical_error))
            errorLog(logTag, "Unable to sign with NFC: ${e.message}", e)
        }
    }
