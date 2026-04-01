/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
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

@file:Suppress("PackageName", "MaxLineLength")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.ImmutableMap
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.CAN_LENGTH
import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.PaceTunnelException
import ee.ria.DigiDoc.idcard.TokenWithPace
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.utils.SendDiagnostics
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.smartcardreader.ApduResponseException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import ee.ria.DigiDoc.utils.pin.PinCodeUtil.isPINLengthValid
import ee.ria.DigiDoc.utilsLib.extensions.clearSensitive
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Hex
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class NFCViewModel
    @Inject
    constructor(
        private val nfcSmartCardReaderManager: NfcSmartCardReaderManager,
        private val containerWrapper: ContainerWrapper,
        private val cdoc2Settings: CDOC2Settings,
        private val configurationRepository: ConfigurationRepository,
        private val idCardService: IdCardService,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer
        private val _cryptoContainer = MutableLiveData<CryptoContainer?>(null)
        val cryptoContainer: LiveData<CryptoContainer?> = _cryptoContainer

        private val _errorState = MutableLiveData<Triple<Int, String?, Int?>?>(null)
        val errorState: LiveData<Triple<Int, String?, Int?>?> = _errorState

        private val _message = MutableLiveData<Int?>(null)
        val message: LiveData<Int?> = _message
        private val _nfcStatus = MutableLiveData<NfcStatus?>(null)
        val nfcStatus: LiveData<NfcStatus?> = _nfcStatus

        private val _signStatus = MutableLiveData<Boolean?>(null)
        val signStatus: LiveData<Boolean?> = _signStatus
        private val _decryptStatus = MutableLiveData<Boolean?>(null)
        val decryptStatus: LiveData<Boolean?> = _decryptStatus
        private val _shouldResetPIN = MutableLiveData(false)
        val shouldResetPIN: LiveData<Boolean> = _shouldResetPIN
        private val _userData = MutableLiveData<IdCardData?>(null)
        val userData: LiveData<IdCardData?> = _userData
        private val _dialogError = MutableLiveData(0)
        val dialogError: LiveData<Int> = _dialogError
        private val _webEidAuthResult = MutableLiveData<Triple<ByteArray, ByteArray, ByteArray>?>()
        val webEidAuthResult: LiveData<Triple<ByteArray, ByteArray, ByteArray>?> = _webEidAuthResult
        private val _webEidSignResult = MutableLiveData<Triple<String, ByteArray, String>?>()
        val webEidSignResult: LiveData<Triple<String, ByteArray, String>?> = _webEidSignResult
        private val _webEidCertificateResult = MutableLiveData<String?>()
        val webEidCertificateResult: LiveData<String?> = _webEidCertificateResult
        private val timeoutHandler = Handler(getMainLooper())
        private var timeoutRunnable: Runnable? = null
        private var pendingWebEidAuthResult: Triple<ByteArray, ByteArray, ByteArray>? = null
        private val _certMismatch = MutableLiveData(false)
        val certMismatch: LiveData<Boolean> = _certMismatch

        private val dialogMessages: ImmutableMap<SessionStatusResponseProcessStatus, Int> =
            ImmutableMap
                .builder<SessionStatusResponseProcessStatus, Int>()
                .put(
                    SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS,
                    R.string.too_many_requests_message,
                ).put(
                    SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT,
                    R.string.invalid_time_slot_message,
                ).build()

        companion object {
            private const val NFC_CARD_DETECTION_TIMEOUT_MS = 30_000L
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetSignStatus() {
            _signStatus.postValue(null)
        }

        fun resetDecryptStatus() {
            _decryptStatus.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun resetCryptoContainer() {
            _cryptoContainer.postValue(null)
        }

        fun resetShouldResetPIN() {
            _shouldResetPIN.postValue(false)
        }

        fun resetWebEidAuthResult() {
            _webEidAuthResult.postValue(null)
        }

        fun resetWebEidSignResult() {
            _webEidSignResult.postValue(null)
        }

        fun resetWebEidCertificateResult() {
            _webEidCertificateResult.postValue(null)
        }

        fun shouldShowCANNumberError(canNumber: String?): Boolean =
            (
                !canNumber.isNullOrEmpty() &&
                    !isCANLengthValid(canNumber)
            )

        fun isCANLengthValid(canNumber: String): Boolean = canNumber.length == CAN_LENGTH

        fun positiveButtonEnabled(
            canNumber: String?,
            pinCode: ByteArray?,
            codeType: CodeType,
        ): Boolean {
            if (canNumber != null && pinCode != null) {
                return isCANLengthValid(canNumber) &&
                    isPINLengthValid(pinCode, codeType)
            }
            return false
        }

        fun getNFCStatus(activity: Activity): NfcStatus = NfcStatus.NFC_ACTIVE

        private fun resetValues() {
            _errorState.postValue(null)
            _message.postValue(null)
            _signStatus.postValue(null)
            _decryptStatus.postValue(null)
            _nfcStatus.postValue(null)
            pendingWebEidAuthResult = null
        }

        private fun resetNonErrorValues() {
            _message.postValue(null)
            _signStatus.postValue(null)
            _decryptStatus.postValue(null)
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

        suspend fun cancelNFCSignWorkRequest(signedContainer: SignedContainer) {
            removePendingSignature(signedContainer)

            nfcSmartCardReaderManager.disableNfcReaderMode()
        }

        fun cancelNfcOperation() {
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

        suspend fun performNFCSignWorkRequest(
            activity: Activity,
            context: Context,
            container: SignedContainer?,
            pin2Code: ByteArray?,
            canNumber: String,
            roleData: RoleData?,
        ) {
            val pinType = context.getString(R.string.signature_id_card_pin2)
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
                                _message.postValue(R.string.signature_update_nfc_detected)

                                val card = TokenWithPace.create(nfcReader)
                                card.tunnel(canNumber)
                                val signerCert = card.certificate(CertificateType.SIGNING)
                                debugLog(
                                    logTag,
                                    "Signer certificate: " + Base64.getEncoder().encodeToString(signerCert),
                                )

                                val signer = ExternalSigner(signerCert)
                                signer.setProfile(SIGNATURE_PROFILE_TS)
                                signer.setUserAgent(UserAgentUtil.getUserAgent(context, SendDiagnostics.NFC))

                                val dataToSignBytes =
                                    containerWrapper.prepareSignature(signer, container, signerCert, roleData)

                                val signatureArray =
                                    card.calculateSignature(pin2Code, dataToSignBytes, true)
                                pin2Code.clearSensitive()
                                debugLog(logTag, "Signature: " + Hex.toHexString(signatureArray))

                                containerWrapper.finalizeSignature(
                                    signer,
                                    container,
                                    signatureArray,
                                )

                                _shouldResetPIN.postValue(true)
                                _signStatus.postValue(true)
                                _signedContainer.postValue(container)
                            } catch (ex: SmartCardReaderException) {
                                handleSmartCardReaderException(ex, CodeType.PIN2, pinType)
                            } catch (ex: Exception) {
                                _signStatus.postValue(false)
                                _shouldResetPIN.postValue(true)

                                val message = ex.message.orEmpty()

                                when {
                                    message.contains("Certificate status: revoked") ->
                                        showRevokedCertificateError(ex)

                                    message.contains("Certificate status: unknown") ->
                                        showUnknownCertificateError(ex)

                                    handleGeneralException(ex) ->
                                        Unit

                                    else ->
                                        showTechnicalError(ex)
                                }
                            } finally {
                                pin2Code.clearSensitive()
                                nfcSmartCardReaderManager.disableNfcReaderMode()
                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    },
                )
            } else {
                withContext(Main) {
                    _nfcStatus.postValue(nfcSmartCardReaderManager.detectNfcStatus(activity))
                    _signStatus.postValue(false)
                    _errorState.postValue(Triple(R.string.error_general_client, null, null))
                    errorLog(logTag, "Unable to get container value. Container is 'null'")
                }
            }
        }

        suspend fun performNFCDecryptWorkRequest(
            activity: Activity,
            context: Context,
            container: CryptoContainer?,
            pin1Code: ByteArray,
            canNumber: String,
        ) {
            val pinType = context.getString(R.string.signature_id_card_pin1)
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
                                _message.postValue(R.string.signature_update_nfc_detected)

                                val card = TokenWithPace.create(nfcReader)
                                card.tunnel(canNumber)

                                val authCert =
                                    card.certificate(CertificateType.AUTHENTICATION)
                                debugLog(
                                    logTag,
                                    "Auth certificate: " + Base64.getEncoder().encodeToString(authCert),
                                )
                                val decryptedContainer =
                                    CryptoContainer.decrypt(
                                        context,
                                        container.file,
                                        container.recipients,
                                        authCert,
                                        pin1Code,
                                        card,
                                        cdoc2Settings,
                                        configurationRepository,
                                    )
                                pin1Code.clearSensitive()

                                _shouldResetPIN.postValue(true)
                                _decryptStatus.postValue(true)
                                _cryptoContainer.postValue(decryptedContainer)
                            } catch (ex: SmartCardReaderException) {
                                _decryptStatus.postValue(false)
                                handleSmartCardReaderException(ex, CodeType.PIN1, pinType)
                            } catch (ex: Exception) {
                                _decryptStatus.postValue(false)
                                _shouldResetPIN.postValue(true)

                                val message = ex.message.orEmpty()

                                when {
                                    message.contains("No lock found with certificate key") ->
                                        showNoLockFoundError(ex)

                                    message.contains("Certificate status: revoked") ->
                                        showRevokedCertificateError(ex)

                                    message.contains("Certificate status: unknown") ->
                                        showUnknownCertificateError(ex)

                                    handleGeneralException(ex) ->
                                        Unit

                                    else ->
                                        showTechnicalError(ex)
                                }
                            } finally {
                                pin1Code.clearSensitive()
                                nfcSmartCardReaderManager.disableNfcReaderMode()
                                activity.requestedOrientation =
                                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            }
                        }
                    },
                )
            } else {
                withContext(Main) {
                    _nfcStatus.postValue(nfcSmartCardReaderManager.detectNfcStatus(activity))
                    _decryptStatus.postValue(false)
                    _errorState.postValue(Triple(R.string.error_general_client, null, null))
                    errorLog(logTag, "Unable to get container value. Container is 'null'")
                    _shouldResetPIN.postValue(true)
                }
            }
        }

        suspend fun loadPersonalData(
            activity: Activity,
            canNumber: String,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation

            checkNFCStatus(
                nfcSmartCardReaderManager.startDiscovery(activity) { nfcReader, exc ->
                    if ((nfcReader != null) && (exc == null)) {
                        _message.postValue(R.string.signature_update_nfc_detected)
                        try {
                            val card = TokenWithPace.create(nfcReader)
                            card.tunnel(canNumber)

                            // NFC operations must run on the same thread as the startDiscovery callback.
                            // Only "runBlocking" works here — coroutines or new threads break the NFC session.
                            val data =
                                runBlocking {
                                    idCardService.data(card)
                                }

                            _userData.postValue(data)
                        } catch (e: Exception) {
                            resetIdCardUserData()

                            if (e.message?.contains("TagLostException") == true) {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_tag_lost, null, null),
                                )
                            } else if (e is ApduResponseException) {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_technical_error, null, null),
                                )
                            } else if (e is PaceTunnelException) {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_wrong_can, null, null),
                                )
                            } else {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_technical_error, null, null),
                                )
                            }

                            errorLog(
                                logTag,
                                "Unable to get ID-card personal data: ${e.message}",
                                e,
                            )

                            resetNonErrorValues()
                        } finally {
                            nfcSmartCardReaderManager.disableNfcReaderMode()
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
            )
        }

        suspend fun performNFCWebEidAuthWorkRequest(
            activity: Activity,
            context: Context,
            canNumber: String,
            pin1Code: ByteArray,
            origin: String,
            challenge: String,
        ) {
            val pinType = context.getString(R.string.signature_id_card_pin1)
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetValues()
            startNFCDetectionTimeout(activity, pin1Code)

            withContext(Main) {
                _message.postValue(R.string.signature_update_nfc_hold)
            }

            checkNFCStatus(
                nfcSmartCardReaderManager.startDiscovery(activity) { nfcReader, exc ->
                    if ((nfcReader != null) && (exc == null)) {
                        stopNFCDetectionTimeout()
                        try {
                            _message.postValue(R.string.signature_update_nfc_detected)

                            val card = TokenWithPace.create(nfcReader)
                            card.tunnel(canNumber)

                            val pin2Changed = card.pinChangedFlag() == 1

                            val (authCert, signingCert, signatureArray) =
                                idCardService.authenticate(
                                    token = card,
                                    pin1 = pin1Code,
                                    origin = origin,
                                    challenge = challenge,
                                )

                            if (!pin2Changed) {
                                handlePin2NotChanged(pin1Code, authCert, signingCert, signatureArray)
                                return@startDiscovery
                            }

                            pin1Code.clearSensitive()

                            _shouldResetPIN.postValue(true)
                            _webEidAuthResult.postValue(Triple(authCert, signingCert, signatureArray))
                        } catch (ex: SmartCardReaderException) {
                            handleSmartCardReaderException(ex, CodeType.PIN1, pinType)
                        } catch (ex: Exception) {
                            _shouldResetPIN.postValue(true)

                            val message = ex.message.orEmpty()

                            when {
                                message.contains("No lock found with certificate key") ->
                                    showNoLockFoundError(ex)

                                handleGeneralException(ex) ->
                                    Unit

                                else ->
                                    showTechnicalError(ex)
                            }
                        } finally {
                            stopNFCDetectionTimeout()
                            pin1Code.clearSensitive()
                            nfcSmartCardReaderManager.disableNfcReaderMode()
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
            )
        }

        suspend fun performNFCWebEidCertificateWorkRequest(
            activity: Activity,
            canNumber: String,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetValues()
            startNFCDetectionTimeout(activity)

            withContext(Main) {
                _message.postValue(R.string.signature_update_nfc_hold)
            }

            checkNFCStatus(
                nfcSmartCardReaderManager.startDiscovery(activity) { nfcReader, exc ->
                    if ((nfcReader != null) && (exc == null)) {
                        stopNFCDetectionTimeout()
                        try {
                            _message.postValue(R.string.signature_update_nfc_detected)

                            val card = TokenWithPace.create(nfcReader)
                            card.tunnel(canNumber)

                            val signingCert = card.certificate(CertificateType.SIGNING)
                            val signingCertB64 = Base64.getEncoder().encodeToString(signingCert)

                            _webEidCertificateResult.postValue(signingCertB64)
                        } catch (ex: SmartCardReaderException) {
                            if (ex.message?.contains("TagLostException") == true) {
                                _errorState.postValue(Triple(R.string.signature_update_nfc_tag_lost, null, null))
                            } else if (ex is ApduResponseException) {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_technical_error, null, null),
                                )
                            } else if (ex is PaceTunnelException) {
                                _errorState.postValue(
                                    Triple(R.string.signature_update_nfc_wrong_can, null, null),
                                )
                            } else {
                                showTechnicalError(ex)
                            }

                            errorLog(logTag, "Exception: " + ex.message, ex)
                        } catch (ex: Exception) {
                            val message = ex.message.orEmpty()

                            when {
                                message.contains("No lock found with certificate key") ->
                                    showNoLockFoundError(ex)

                                handleGeneralException(ex) ->
                                    Unit

                                else ->
                                    showTechnicalError(ex)
                            }
                        } finally {
                            stopNFCDetectionTimeout()
                            nfcSmartCardReaderManager.disableNfcReaderMode()
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
            )
        }

        suspend fun performNFCWebEidSignWorkRequest(
            activity: Activity,
            context: Context,
            canNumber: String,
            pin2Code: ByteArray?,
            responseUri: String,
            hash: String,
            requestSigningCert: String?,
        ) {
            val pinType = context.getString(R.string.signature_id_card_pin2)
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetValues()
            startNFCDetectionTimeout(activity, pin2Code)

            withContext(Main) {
                _message.postValue(R.string.signature_update_nfc_hold)
            }

            checkNFCStatus(
                nfcSmartCardReaderManager.startDiscovery(activity) { nfcReader, exc ->
                    if ((nfcReader != null) && (exc == null)) {
                        stopNFCDetectionTimeout()
                        try {
                            _message.postValue(R.string.signature_update_nfc_detected)

                            val card = TokenWithPace.create(nfcReader)
                            card.tunnel(canNumber)
                            val signerCert = card.certificate(CertificateType.SIGNING)
                            val signerCertB64 = Base64.getEncoder().encodeToString(signerCert)

                            if (requestSigningCert.isNullOrEmpty()) {
                                throw IllegalStateException("Missing signing certificate from AUTH or CERT flow")
                            } else {
                                val expectedCert = Base64.getDecoder().decode(requestSigningCert)

                                if (!expectedCert.contentEquals(signerCert)) {
                                    _certMismatch.postValue(true)
                                    throw IllegalStateException(
                                        "Web eID card does not match the card used for authentication",
                                    )
                                }
                            }

                            val hashBytes = Base64.getDecoder().decode(hash)
                            val (_, signatureArray) = idCardService.sign(card, pin2Code, hashBytes)

                            _shouldResetPIN.postValue(true)
                            _signStatus.postValue(true)
                            _webEidSignResult.postValue(
                                Triple(signerCertB64, signatureArray, responseUri),
                            )
                        } catch (ex: SmartCardReaderException) {
                            handleSmartCardReaderException(ex, CodeType.PIN2, pinType)
                        } catch (ex: Exception) {
                            _signStatus.postValue(false)
                            _shouldResetPIN.postValue(true)

                            val message = ex.message.orEmpty()

                            when {
                                message.contains("Certificate status: revoked") ->
                                    showRevokedCertificateError(ex)

                                message.contains("Certificate status: unknown") ->
                                    showUnknownCertificateError(ex)

                                handleGeneralException(ex) ->
                                    Unit

                                else ->
                                    showTechnicalError(ex)
                            }
                        } finally {
                            stopNFCDetectionTimeout()
                            pin2Code.clearSensitive()
                            nfcSmartCardReaderManager.disableNfcReaderMode()
                            activity.requestedOrientation =
                                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        }
                    }
                },
            )
        }

        fun handleBackButton() {
            _shouldResetPIN.postValue(true)
            resetValues()
        }

        fun resetDialogErrorState() {
            _dialogError.postValue(0)
        }

        fun resetIdCardUserData() {
            _userData.postValue(null)
        }

        private fun setErrorState(status: SessionStatusResponseProcessStatus) {
            val res = dialogMessages[status]

            if (res == R.string.too_many_requests_message ||
                res == R.string.invalid_time_slot_message
            ) {
                _dialogError.postValue(res)
            } else {
                _errorState.postValue(res?.let { Triple(it, null, null) })
            }
        }

        private fun showNetworkError(e: Exception) {
            _errorState.postValue(Triple(R.string.no_internet_connection, null, null))
            errorLog(logTag, "Unable to sign with NFC - Unable to connect to Internet", e)
        }

        private fun showProxyError(e: Exception) {
            _errorState.postValue(Triple(R.string.main_settings_proxy_invalid_settings, null, null))
            errorLog(logTag, "Unable to sign with NFC - Unable to create proxy connection with host", e)
        }

        private fun showNoLockFoundError(e: Exception) {
            _errorState.postValue(Triple(R.string.no_lock_found, null, null))
            errorLog(logTag, "Unable to decrypt with NFC - No lock found with certificate key", e)
        }

        private fun showRevokedCertificateError(e: Exception) {
            _errorState.postValue(
                Triple(
                    R.string.signature_update_signature_error_message_certificate_revoked,
                    null,
                    null,
                ),
            )
            errorLog(logTag, "Unable to sign with NFC - Certificate status: revoked", e)
        }

        private fun showUnknownCertificateError(e: Exception) {
            _errorState.postValue(
                Triple(
                    R.string.signature_update_signature_error_message_certificate_unknown,
                    null,
                    null,
                ),
            )
            errorLog(logTag, "Unable to sign with NFC - Certificate status: unknown", e)
        }

        private fun showWebEidSigningCertificateMismatchError(e: Exception) {
            _errorState.postValue(Triple(R.string.signature_update_nfc_wrong_certificate, null, null))
            errorLog(
                logTag,
                "Web eID signing failed - signing certificate does not match previously used certificate",
                e,
            )
        }

        private fun showWebEidAuthenticationCardMismatchError(e: Exception) {
            _errorState.postValue(Triple(R.string.web_eid_signing_card_mismatch, null, null))
            errorLog(
                logTag,
                "Web eID signing failed - selected ID card does not match the card used for authentication",
                e,
            )
        }

        private fun showTechnicalError(e: Exception) {
            _errorState.postValue(Triple(R.string.signature_update_nfc_technical_error, null, null))
            errorLog(logTag, "Unable to perform with NFC: ${e.message}", e)
        }

        private fun handleSmartCardReaderException(
            ex: SmartCardReaderException,
            codeType: CodeType,
            pinType: String,
        ) {
            val pinName = codeType.name
            val isSigning = codeType == CodeType.PIN2

            if (isSigning) {
                _signStatus.postValue(false)
            }

            when {
                ex.message?.contains("TagLostException") == true -> {
                    _errorState.postValue(Triple(R.string.signature_update_nfc_tag_lost, null, null))
                }

                isSigning && ex.message?.contains("PIN2 has not been changed") == true -> {
                    _dialogError.postValue(R.string.sign_blocked_pin2_unchanged_message)
                }

                ex.message?.contains("$pinName verification failed") == true &&
                    ex.message?.contains("Retries left: 2") == true -> {
                    _shouldResetPIN.postValue(true)
                    _errorState.postValue(Triple(R.string.id_card_sign_pin_invalid, pinType, 2))
                }

                ex.message?.contains("$pinName verification failed") == true &&
                    ex.message?.contains("Retries left: 1") == true -> {
                    _shouldResetPIN.postValue(true)
                    _errorState.postValue(Triple(R.string.id_card_sign_pin_invalid_final, pinType, null))
                }

                ex.message?.contains("$pinName verification failed") == true &&
                    ex.message?.contains("Retries left: 0") == true -> {
                    _shouldResetPIN.postValue(true)
                    _errorState.postValue(Triple(R.string.id_card_sign_pin_locked, pinType, null))
                }

                ex is ApduResponseException -> {
                    _errorState.postValue(Triple(R.string.signature_update_nfc_technical_error, null, null))
                }

                ex is PaceTunnelException -> {
                    _errorState.postValue(Triple(R.string.signature_update_nfc_wrong_can, null, null))
                }

                else -> {
                    showTechnicalError(ex)
                }
            }

            errorLog(logTag, "Exception: ${ex.message}", ex)
        }

        private fun handleGeneralException(ex: Exception): Boolean {
            val message = ex.message.orEmpty()

            return when {
                message.contains("Failed to connect") ||
                    message.contains("Failed to create connection with host") -> {
                    showNetworkError(ex)
                    true
                }

                message.contains("Failed to create proxy connection with host") -> {
                    showProxyError(ex)
                    true
                }

                message.contains("Too Many Requests") -> {
                    setErrorState(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS)
                    true
                }

                message.contains("OCSP response not in valid time slot") -> {
                    setErrorState(SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT)
                    true
                }

                message.contains("Web eID signing certificate mismatch") -> {
                    showWebEidSigningCertificateMismatchError(ex)
                    true
                }

                message.contains("Web eID card does not match the card used for authentication") -> {
                    showWebEidAuthenticationCardMismatchError(ex)
                    true
                }

                else -> false
            }.also {
                errorLog(logTag, "Exception: ${ex.message}", ex)
            }
        }

        override fun onCleared() {
            super.onCleared()
            stopNFCDetectionTimeout()
            nfcSmartCardReaderManager.disableNfcReaderMode()
        }

        private fun startNFCDetectionTimeout(
            activity: Activity,
            pinToClear: ByteArray? = null,
        ) {
            timeoutRunnable =
                Runnable {
                    pinToClear?.clearSensitive()
                    _errorState.postValue(
                        Triple(R.string.signature_update_nfc_detection_timeout, null, null),
                    )

                    nfcSmartCardReaderManager.disableNfcReaderMode()
                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }

            timeoutHandler.postDelayed(timeoutRunnable!!, NFC_CARD_DETECTION_TIMEOUT_MS)
        }

        private fun stopNFCDetectionTimeout() {
            timeoutRunnable?.let {
                timeoutHandler.removeCallbacks(it)
            }
            timeoutRunnable = null
        }

        private fun handlePin2NotChanged(
            pin1Code: ByteArray,
            authCert: ByteArray,
            signingCert: ByteArray,
            signatureArray: ByteArray,
        ) {
            pin1Code.clearSensitive()
            _shouldResetPIN.postValue(true)

            pendingWebEidAuthResult = Triple(authCert, signingCert, signatureArray)
            _dialogError.postValue(R.string.sign_blocked_pin2_unchanged_message)
        }

        fun continuePendingWebEidAuth() {
            pendingWebEidAuthResult?.let {
                _webEidAuthResult.postValue(it)
                pendingWebEidAuthResult = null
            }
        }

        fun checkWebEidSigningCertificateMismatch(
            cachedCert: String?,
            requestSigningCert: String?,
        ): Boolean {
            if (cachedCert.isNullOrEmpty() || requestSigningCert.isNullOrEmpty()) {
                return false
            }

            val isMismatch = cachedCert != requestSigningCert
            if (isMismatch) {
                _certMismatch.postValue(true)
                showWebEidSigningCertificateMismatchError(
                    IllegalStateException("Web eID signing certificate mismatch"),
                )
            }

            return isMismatch
        }

        fun resetCertificateMismatch() {
            _certMismatch.postValue(false)
        }
    }
