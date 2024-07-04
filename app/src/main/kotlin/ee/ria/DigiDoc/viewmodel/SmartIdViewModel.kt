@file:Suppress("PackageName", "MaxLineLength")

package ee.ria.DigiDoc.viewmodel

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.ImmutableMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.MAXIMUM_PERSONAL_CODE_LENGTH
import ee.ria.DigiDoc.common.Constant.SmartIdConstants.NOTIFICATION_CHANNEL
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.sid.dto.request.SmartCreateSignatureRequest
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.smartId.SmartSignService
import ee.ria.DigiDoc.smartId.utils.SmartCreateSignatureRequestHelper
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import ee.ria.DigiDoc.utilsLib.signing.NotificationUtil
import ee.ria.DigiDoc.utilsLib.signing.PowerUtil
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator.validatePersonalCode
import ee.ria.libdigidocpp.Conf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class SmartIdViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStore,
        private val smartSignService: SmartSignService,
        private val configurationRepository: ConfigurationRepository,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName
        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer
        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState
        private val _challenge = MutableLiveData<String?>(null)
        val challenge: LiveData<String?> = _challenge
        private val _status = MutableLiveData<SessionStatusResponseProcessStatus?>(null)
        val status: LiveData<SessionStatusResponseProcessStatus?> = _status
        private val _selectDevice = MutableLiveData(false)
        val selectDevice: LiveData<Boolean?> = _selectDevice

        private val countries: ImmutableMap<Int, String> =
            ImmutableMap.builder<Int, String>()
                .put(0, "EE")
                .put(1, "LT")
                .put(2, "LV")
                .build()

        private val messages: ImmutableMap<SessionStatusResponseProcessStatus, Int> =
            ImmutableMap.builder<SessionStatusResponseProcessStatus, Int>()
                .put(
                    SessionStatusResponseProcessStatus.OK,
                    R.string.signature_update_mobile_id_status_request_ok,
                )
                .put(
                    SessionStatusResponseProcessStatus.TIMEOUT,
                    R.string.signature_update_smart_id_error_message_account_not_found_or_timeout,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.DOCUMENT_UNUSABLE,
                    R.string.signature_update_smart_id_status_document_unusable,
                )
                .put(
                    SessionStatusResponseProcessStatus.WRONG_VC,
                    R.string.signature_update_smart_id_status_wrong_vc,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED_DISPLAYTEXTANDPIN,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED_VC_CHOICE,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED_CONFIRMATIONMESSAGE,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED_CONFIRMATIONMESSAGE_WITH_VC_CHOICE,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.USER_REFUSED_CERT_CHOICE,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    SessionStatusResponseProcessStatus.ACCOUNT_NOT_FOUND,
                    R.string.signature_update_smart_id_error_message_account_not_found_or_timeout,
                )
                .put(
                    SessionStatusResponseProcessStatus.SESSION_NOT_FOUND,
                    R.string.signature_update_smart_id_error_message_session_not_found,
                )
                .put(
                    SessionStatusResponseProcessStatus.MISSING_SESSIONID,
                    R.string.signature_update_mobile_id_error_general_client,
                )
                .put(
                    SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS,
                    R.string.signature_update_signature_error_message_too_many_requests,
                )
                .put(
                    SessionStatusResponseProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS,
                    R.string.signature_update_signature_error_message_exceeded_unsuccessful_requests,
                )
                .put(
                    SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT,
                    R.string.signature_update_signature_error_message_invalid_time_slot,
                )
                .put(
                    SessionStatusResponseProcessStatus.CERTIFICATE_REVOKED,
                    R.string.signature_update_signature_error_message_certificate_revoked,
                )
                .put(
                    SessionStatusResponseProcessStatus.NOT_QUALIFIED,
                    R.string.signature_update_smart_id_error_message_not_qualified,
                )
                .put(
                    SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS,
                    R.string.signature_update_smart_id_error_message_access_rights,
                )
                .put(
                    SessionStatusResponseProcessStatus.OLD_API,
                    R.string.signature_update_smart_id_error_message_old_api,
                )
                .put(
                    SessionStatusResponseProcessStatus.UNDER_MAINTENANCE,
                    R.string.signature_update_smart_id_error_message_under_maintenance,
                )
                .put(
                    SessionStatusResponseProcessStatus.GENERAL_ERROR,
                    R.string.signature_update_mobile_id_error_general_client,
                )
                .put(
                    SessionStatusResponseProcessStatus.NO_RESPONSE,
                    R.string.no_internet_connection,
                )
                .put(
                    SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE,
                    R.string.invalid_ssl_handshake,
                )
                .put(
                    SessionStatusResponseProcessStatus.TECHNICAL_ERROR,
                    R.string.signature_update_smart_id_error_technical_error,
                )
                .build()

        fun resetStatus() {
            _status.postValue(null)
        }

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun cancelSmartIdWorkRequest() {
            smartSignService.setCancelled(true)
        }

        private fun resetValues() {
            _errorState.postValue(null)
            _challenge.postValue(null)
            _status.postValue(null)
            _selectDevice.postValue(false)
            smartSignService.resetValues()
        }

        suspend fun performSmartIdWorkRequest(
            container: SignedContainer?,
            personalCode: String,
            country: Int,
            roleData: RoleData?,
        ) {
            resetValues()
            val configurationProvider = configurationRepository.getConfiguration()
            val uuid = dataStore.getSettingsUUID()
            val proxySetting: ProxySetting = dataStore.getProxySetting()
            val manualProxySettings: ManualProxy = dataStore.getManualProxySettings(context)

            val displayMessage: String =
                context
                    .getString(R.string.signature_update_mobile_id_display_message)
            val request: SmartCreateSignatureRequest =
                SmartCreateSignatureRequestHelper
                    .create(
                        container,
                        uuid,
                        configurationProvider?.sidV2RestUrl,
                        configurationProvider?.sidV2SkRestUrl,
                        countries[country],
                        personalCode,
                        displayMessage,
                    )
            val certBundle = ArrayList(configurationProvider?.certBundle ?: emptyList())
            withContext(Dispatchers.Main) {
                smartSignService.errorState.observeForever {
                    if (it != null) {
                        _errorState.postValue(it)
                    }
                }
                smartSignService.challenge.observeForever { challenge ->
                    if (challenge != null) {
                        _challenge.postValue(challenge)

                        if (!PowerUtil.isPowerSavingMode(context)) {
                            debugLog(logTag, "Creating notification channel")

                            NotificationUtil.createNotificationChannel(
                                context,
                                NOTIFICATION_CHANNEL,
                                context.getString(R.string.signature_update_signature_add_method_smart_id),
                            )
                        }
                        val challengeTitle: String = context.getString(R.string.smart_id_challenge)
                        val notification: Notification? =
                            NotificationUtil.createNotification(
                                context,
                                NOTIFICATION_CHANNEL,
                                R.mipmap.ic_launcher,
                                challengeTitle,
                                challenge,
                                NotificationCompat.PRIORITY_HIGH,
                                false,
                            )
                        try {
                            if (notification != null) {
                                sendNotification(context, challenge, notification)
                            }
                        } catch (nfe: NumberFormatException) {
                            errorLog(logTag, "Unable to send notification", nfe)
                        }
                    }
                }
                smartSignService.selectDevice.observeForever {
                    if (it != null) {
                        _selectDevice.postValue(it)
                    }
                }
                smartSignService.status.observeForever {
                    if (it != null) {
                        _status.postValue(it)
                        if (it != SessionStatusResponseProcessStatus.OK) {
                            if (it != SessionStatusResponseProcessStatus.USER_CANCELLED) {
                                _errorState.postValue(
                                    messages[it]?.let { res ->
                                        context.getString(
                                            res,
                                        )
                                    },
                                )
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    _signedContainer.postValue(SignedContainer.container())
                                }
                            }
                        }
                    }
                }
                smartSignService.response.observeForever {
                    when (it?.status) {
                        SessionStatusResponseProcessStatus.OK -> {
                            CoroutineScope(Dispatchers.Main).launch {
                                _status.postValue(it.status)
                                _signedContainer.postValue(SignedContainer.container())
                            }
                        }

                        else -> {
                            if (it != null) {
                                _status.postValue(it.status)
                                _errorState.postValue(
                                    messages[it.status]?.let { res ->
                                        context.getString(
                                            res,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
            smartSignService.resetValues()
            smartSignService.processSmartIdRequest(
                context,
                request,
                roleData,
                proxySetting,
                manualProxySettings,
                certBundle,
                Objects.requireNonNull(Conf.instance()).PKCS12Cert(),
                Objects.requireNonNull(Conf.instance()).PKCS12Pass(),
            )

            withContext(Dispatchers.Main) {
                smartSignService.errorState.removeObserver {}
                smartSignService.challenge.removeObserver {}
                smartSignService.status.removeObserver {}
                smartSignService.response.removeObserver {}
            }
        }

        fun positiveButtonEnabled(
            country: Int,
            personalCode: String?,
        ): Boolean {
            if (personalCode != null) {
                validatePersonalCode(personalCode)
                return country != 0 || isPersonalCodeCorrect(personalCode)
            }
            return false
        }

        private fun isPersonalCodeCorrect(personalCode: String): Boolean {
            return personalCode.length == MAXIMUM_PERSONAL_CODE_LENGTH
        }

        @Throws(NumberFormatException::class)
        private fun sendNotification(
            context: Context,
            challenge: String,
            notification: Notification,
        ) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2 ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(challenge.toInt(), notification)
            }
        }
    }
