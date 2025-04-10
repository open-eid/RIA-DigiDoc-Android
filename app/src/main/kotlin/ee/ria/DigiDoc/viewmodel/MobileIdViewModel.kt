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
import ee.ria.DigiDoc.common.Constant.ALLOWED_PHONE_NUMBER_COUNTRY_CODES
import ee.ria.DigiDoc.common.Constant.MAXIMUM_PERSONAL_CODE_LENGTH
import ee.ria.DigiDoc.common.Constant.MINIMUM_PHONE_NUMBER_LENGTH
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.mobileId.utils.MobileCreateSignatureRequestHelper
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator.validatePersonalCode
import ee.ria.libdigidocpp.Conf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class MobileIdViewModel
    @Inject
    constructor(
        private val dataStore: DataStore,
        private val mobileSignService: MobileSignService,
        private val configurationRepository: ConfigurationRepository,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer
        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState
        private val _challenge = MutableLiveData<String?>(null)
        val challenge: LiveData<String?> = _challenge
        private val _status = MutableLiveData<MobileCreateSignatureProcessStatus?>(null)
        val status: LiveData<MobileCreateSignatureProcessStatus?> = _status

        private val _dialogError = MutableLiveData(0)
        val dialogError: LiveData<Int> = _dialogError

        private val faults: ImmutableMap<MobileCertificateResultType, Int> =
            ImmutableMap.builder<MobileCertificateResultType, Int>()
                .put(
                    MobileCertificateResultType.NOT_FOUND,
                    R.string.signature_update_mobile_id_error_not_mobile_id_user,
                )
                .put(
                    MobileCertificateResultType.NOT_ACTIVE,
                    R.string.signature_update_mobile_id_error_not_mobile_id_user,
                )
                .build()

        private val messages: ImmutableMap<MobileCreateSignatureProcessStatus, Int> =
            ImmutableMap.builder<MobileCreateSignatureProcessStatus, Int>()
                .put(
                    MobileCreateSignatureProcessStatus.OK,
                    R.string.signature_update_mobile_id_status_request_ok,
                )
                .put(
                    MobileCreateSignatureProcessStatus.TIMEOUT,
                    R.string.signature_update_mobile_id_status_expired_transaction,
                )
                .put(
                    MobileCreateSignatureProcessStatus.NOT_MID_CLIENT,
                    R.string.signature_update_mobile_id_status_expired_transaction,
                )
                .put(
                    MobileCreateSignatureProcessStatus.USER_CANCELLED,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    MobileCreateSignatureProcessStatus.SIGNATURE_HASH_MISMATCH,
                    R.string.signature_update_mobile_id_status_signature_hash_mismatch,
                )
                .put(
                    MobileCreateSignatureProcessStatus.DELIVERY_ERROR,
                    R.string.signature_update_mobile_id_status_delivery_error,
                )
                .put(
                    MobileCreateSignatureProcessStatus.PHONE_ABSENT,
                    R.string.signature_update_mobile_id_status_phone_absent,
                )
                .put(
                    MobileCreateSignatureProcessStatus.SIM_ERROR,
                    R.string.signature_update_mobile_id_status_sim_error,
                )
                .put(
                    MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS,
                    R.string.too_many_requests_message,
                )
                .put(
                    MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS,
                    R.string.signature_update_signature_error_message_exceeded_unsuccessful_requests,
                )
                .put(
                    MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS,
                    R.string.signature_update_mobile_id_error_message_access_rights,
                )
                .put(
                    MobileCreateSignatureProcessStatus.OCSP_INVALID_TIME_SLOT,
                    R.string.invalid_time_slot_message,
                )
                .put(
                    MobileCreateSignatureProcessStatus.CERTIFICATE_REVOKED,
                    R.string.signature_update_signature_error_message_certificate_revoked,
                )
                .put(
                    MobileCreateSignatureProcessStatus.GENERAL_ERROR,
                    R.string.error_general_client,
                )
                .put(MobileCreateSignatureProcessStatus.NO_RESPONSE, R.string.no_internet_connection)
                .put(
                    MobileCreateSignatureProcessStatus.INVALID_COUNTRY_CODE,
                    R.string.signature_update_mobile_id_status_no_country_code,
                )
                .put(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE, R.string.invalid_ssl_handshake)
                .put(
                    MobileCreateSignatureProcessStatus.TECHNICAL_ERROR,
                    R.string.signature_update_mobile_id_error_technical_error,
                )
                .put(
                    MobileCreateSignatureProcessStatus.INVALID_PROXY_SETTINGS,
                    R.string.main_settings_proxy_invalid_settings,
                )
                .build()

        fun resetDialogErrorState() {
            _dialogError.postValue(0)
        }

        fun resetErrorState() {
            _errorState.postValue(null)
        }

        fun resetStatus() {
            _status.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun cancelMobileIdWorkRequest(signedContainer: SignedContainer?) {
            if (signedContainer != null) {
                mobileSignService.setCancelled(signedContainer, true)
            }
        }

        private fun setErrorState(
            context: Context,
            status: MobileCreateSignatureProcessStatus?,
        ) {
            val res = messages[status]

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

        private fun resetValues() {
            _errorState.postValue(null)
            _challenge.postValue(null)
            _status.postValue(null)
            mobileSignService.resetValues()
        }

        suspend fun performMobileIdWorkRequest(
            activity: Activity,
            context: Context,
            displayMessage: String,
            container: SignedContainer?,
            personalCode: String,
            phoneNumber: String,
            roleData: RoleData?,
        ) {
            activity.requestedOrientation = activity.resources.configuration.orientation
            resetValues()
            val configurationProvider = configurationRepository.getConfiguration()
            val uuid = dataStore.getSettingsUUID()
            val proxySetting: ProxySetting = dataStore.getProxySetting()
            val manualProxySettings: ManualProxy = dataStore.getManualProxySettings()

            val request: MobileCreateSignatureRequest =
                MobileCreateSignatureRequestHelper
                    .create(
                        container,
                        uuid,
                        configurationProvider?.midRestUrl,
                        configurationProvider?.midSkRestUrl,
                        Locale.getDefault(),
                        personalCode,
                        phoneNumber,
                        displayMessage,
                    )
            val certBundle = ArrayList(configurationProvider?.certBundle ?: emptyList())
            withContext(Main) {
                mobileSignService.errorState.observeForever {
                    if (it != null) {
                        _errorState.postValue(it)
                    }
                }
                mobileSignService.challenge.observeForever { challenge ->
                    if (challenge != null) {
                        _challenge.postValue(challenge)
                    }
                }
                mobileSignService.result.observeForever {
                    if (it != null) {
                        _errorState.postValue(
                            faults[it]?.let { res ->
                                context.getString(
                                    res,
                                )
                            },
                        )
                    }
                }
                mobileSignService.status.observeForever { status ->
                    if (status != null) {
                        _status.postValue(status)
                        if (status != MobileCreateSignatureProcessStatus.OK) {
                            if (status != MobileCreateSignatureProcessStatus.USER_CANCELLED) {
                                setErrorState(context, status)
                            } else {
                                CoroutineScope(Main).launch {
                                    val containerSignatures = container?.getSignatures(Main)
                                    val signatureInterface =
                                        if (containerSignatures?.isEmpty() == true) {
                                            null
                                        } else {
                                            containerSignatures
                                                ?.lastOrNull {
                                                    it.validator.status == ValidatorInterface.Status.Invalid ||
                                                        it.validator.status == ValidatorInterface.Status.Unknown
                                                }
                                        }
                                    signatureInterface?.let {
                                        try {
                                            container?.removeSignature(it)
                                        } catch (e: Exception) {
                                            debugLog(
                                                logTag,
                                                "Unable to remove Mobile-ID signature after " +
                                                    "cancelling from device: ${e.localizedMessage}",
                                                e,
                                            )
                                        }
                                    }
                                    _signedContainer.postValue(container)
                                    setErrorState(context, status)
                                }
                            }
                        }
                    }
                }
                mobileSignService.response.observeForever {
                    when (it?.status) {
                        MobileCreateSignatureProcessStatus.OK -> {
                            CoroutineScope(Main).launch {
                                _status.postValue(it.status)
                                _signedContainer.postValue(container)
                            }
                        }
                        else -> {
                            if (it != null) {
                                _status.postValue(it.status)
                                setErrorState(context, it.status)
                            }
                        }
                    }
                }
            }
            mobileSignService.resetValues()
            if (container != null) {
                mobileSignService.processMobileIdRequest(
                    context = context,
                    signedContainer = container,
                    request = request,
                    roleDataRequest = roleData,
                    proxySetting = proxySetting,
                    manualProxySettings = manualProxySettings,
                    certificateBundle = certBundle,
                    accessTokenPath = Objects.requireNonNull(Conf.instance()).PKCS12Cert(),
                    accessTokenPass = Objects.requireNonNull(Conf.instance()).PKCS12Pass(),
                )
            } else {
                CoroutineScope(Main).launch {
                    _status.postValue(MobileCreateSignatureProcessStatus.GENERAL_ERROR)
                    _errorState.postValue(context.getString(R.string.error_general_client))
                    errorLog(logTag, "Unable to get container value. Container is 'null'")
                }
            }

            withContext(Main) {
                mobileSignService.errorState.removeObserver {}
                mobileSignService.challenge.removeObserver {}
                mobileSignService.status.removeObserver {}
                mobileSignService.result.removeObserver {}
                mobileSignService.response.removeObserver {}
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }

        fun isPersonalCodeValid(personalCode: String?): Boolean {
            return !(
                !personalCode.isNullOrEmpty() &&
                    !isPersonalCodeCorrect(personalCode)
            )
        }

        fun isPhoneNumberValid(phoneNumber: String?): Boolean {
            if (!phoneNumber.isNullOrEmpty()) {
                if (isCountryCodeMissing(phoneNumber)) {
                    return false
                } else if (!isCountryCodeCorrect(phoneNumber)) {
                    return false
                } else if (!isPhoneNumberCorrect(phoneNumber)) {
                    return false
                }
            }
            return true
        }

        fun positiveButtonEnabled(
            phoneNumber: String?,
            personalCode: String?,
        ): Boolean {
            if (phoneNumber != null && personalCode != null) {
                return isCountryCodeCorrect(phoneNumber.toString()) &&
                    isPhoneNumberCorrect(phoneNumber.toString()) &&
                    isPersonalCodeCorrect(personalCode.toString())
            }
            return false
        }

        fun isCountryCodeMissing(phoneNumber: String): Boolean {
            return phoneNumber.length in 4..<MINIMUM_PHONE_NUMBER_LENGTH &&
                !isCountryCodeCorrect(phoneNumber)
        }

        fun isCountryCodeCorrect(phoneNumber: String): Boolean {
            for (allowedCountryCode in ALLOWED_PHONE_NUMBER_COUNTRY_CODES) {
                if (phoneNumber.startsWith(allowedCountryCode)) {
                    return true
                }
            }
            return false
        }

        fun isPhoneNumberCorrect(phoneNumber: String): Boolean {
            return phoneNumber.length >= MINIMUM_PHONE_NUMBER_LENGTH
        }

        fun isPersonalCodeCorrect(personalCode: String): Boolean {
            return validatePersonalCode(personalCode) && personalCode.length == MAXIMUM_PERSONAL_CODE_LENGTH
        }
    }
