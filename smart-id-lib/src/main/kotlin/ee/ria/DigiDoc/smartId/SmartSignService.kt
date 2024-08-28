@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.SigningCancelledException
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.sid.dto.request.PostCertificateRequest
import ee.ria.DigiDoc.network.sid.dto.request.PostCreateSignatureRequestV2
import ee.ria.DigiDoc.network.sid.dto.request.RequestAllowedInteractionsOrder
import ee.ria.DigiDoc.network.sid.dto.request.SmartCreateSignatureRequest
import ee.ria.DigiDoc.network.sid.dto.response.ServiceFault
import ee.ria.DigiDoc.network.sid.dto.response.SessionResponse
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponse
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessState
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SmartIDServiceResponse
import ee.ria.DigiDoc.network.sid.rest.SIDRestServiceClient
import ee.ria.DigiDoc.network.sid.rest.ServiceGenerator
import ee.ria.DigiDoc.smartId.utils.VerificationCodeUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import ee.ria.DigiDoc.utilsLib.signing.NotificationUtil
import ee.ria.DigiDoc.utilsLib.signing.PowerUtil
import ee.ria.DigiDoc.utilsLib.signing.UUIDUtil
import ee.ria.DigiDoc.utilsLib.text.MessageUtil
import kotlinx.coroutines.delay
import org.bouncycastle.util.encoders.Base64
import retrofit2.Call
import java.io.IOException
import java.net.UnknownHostException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLPeerUnverifiedException

interface SmartSignService {
    val response: LiveData<SmartIDServiceResponse?>
    val challenge: LiveData<String?>
    val status: LiveData<SessionStatusResponseProcessStatus?>
    val errorState: LiveData<String?>
    val cancelled: LiveData<Boolean?>
    val selectDevice: LiveData<Boolean?>

    fun setCancelled(
        signedContainer: SignedContainer,
        cancelled: Boolean?,
    )

    fun resetValues()

    suspend fun processSmartIdRequest(
        context: Context,
        signedContainer: SignedContainer,
        request: SmartCreateSignatureRequest?,
        roleDataRequest: RoleData?,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
        certificateBundle: ArrayList<String>?,
        accessTokenPath: String?,
        accessTokenPass: String?,
    )
}

@Singleton
class SmartSignServiceImpl
    @Inject
    constructor(
        private val serviceGenerator: ServiceGenerator,
        private val containerWrapper: ContainerWrapper,
    ) :
    SmartSignService {
        private val logTag = javaClass.simpleName

        private lateinit var sidRestServiceClient: SIDRestServiceClient

        private val _response = MutableLiveData<SmartIDServiceResponse?>(null)
        override val response: LiveData<SmartIDServiceResponse?> = _response
        private val _errorState = MutableLiveData<String?>(null)
        override val errorState: LiveData<String?> = _errorState
        private val _challenge = MutableLiveData<String?>(null)
        override val challenge: LiveData<String?> = _challenge
        private val _status = MutableLiveData<SessionStatusResponseProcessStatus?>(null)
        override val status: LiveData<SessionStatusResponseProcessStatus?> = _status
        private val _selectDevice = MutableLiveData(false)
        override val selectDevice: LiveData<Boolean?> = _selectDevice

        private val _cancelled = MutableLiveData(false)
        override val cancelled: LiveData<Boolean?> = _cancelled

        private var signatureInterface: SignatureInterface? = null

        override fun resetValues() {
            _response.postValue(null)
            _errorState.postValue(null)
            _challenge.postValue(null)
            _status.postValue(null)
            _selectDevice.postValue(false)
            _cancelled.postValue(false)
        }

        override fun setCancelled(
            signedContainer: SignedContainer,
            cancelled: Boolean?,
        ) {
            signatureInterface?.let {
                signedContainer.removeSignature(it)
            }
            _cancelled.postValue(cancelled)
        }

        private fun setResponse(response: SmartIDServiceResponse?) {
            _response.postValue(response)
        }

        private fun setErrorState(errorState: String?) {
            _errorState.postValue(errorState)
        }

        private fun setChallenge(challenge: String?) {
            _challenge.postValue(challenge)
        }

        private fun setStatus(status: SessionStatusResponseProcessStatus?) {
            _status.postValue(status)
        }

        private fun setSelectDevice(selectDevice: Boolean?) {
            _selectDevice.postValue(selectDevice)
        }

        override suspend fun processSmartIdRequest(
            context: Context,
            signedContainer: SignedContainer,
            request: SmartCreateSignatureRequest?,
            roleDataRequest: RoleData?,
            proxySetting: ProxySetting?,
            manualProxySettings: ManualProxy,
            certificateBundle: ArrayList<String>?,
            accessTokenPath: String?,
            accessTokenPass: String?,
        ) {
            if (PowerUtil.isPowerSavingMode(context)) {
                showEmptyNotification(context)
            }

            LoggingUtil.debugLog(logTag, "Handling smart sign service")
            if (request != null) {
                try {
                    if (certificateBundle != null) {
                        LoggingUtil.debugLog(logTag, request.toString())

                        sidRestServiceClient =
                            serviceGenerator.createService(
                                context,
                                request.url + "/",
                                certificateBundle,
                                proxySetting,
                                manualProxySettings,
                            )
                    }
                } catch (e: CertificateException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE))
                    LoggingUtil.errorLog(
                        logTag,
                        "SSL handshake failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                    )
                    return
                } catch (e: NoSuchAlgorithmException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE))
                    LoggingUtil.errorLog(
                        logTag,
                        "SSL handshake failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                    )
                    return
                }

                if (!UUIDUtil.isValid(request.relyingPartyUUID)) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS))
                    LoggingUtil.debugLog(
                        logTag,
                        "Relying Party UUID not in valid format: ${request.relyingPartyUUID}",
                    )
                    return
                }

                try {
                    var sessionStatusResponse: SessionStatusResponse?
                    val semanticsIdentifier =
                        ("PNO" + request.country) + "-" + request.nationalIdentityNumber
                    sessionStatusResponse =
                        doSessionStatusRequestLoop(
                            signedContainer,
                            sidRestServiceClient.getCertificateV2(
                                semanticsIdentifier, getCertificateRequest(request),
                            ),
                            true,
                        )
                    if (sessionStatusResponse == null) {
                        LoggingUtil.errorLog(logTag, "No session status response")
                        return
                    }

                    LoggingUtil.debugLog(logTag, "Session status response: $sessionStatusResponse")

                    val base64Hash =
                        containerWrapper.prepareSignature(
                            signedContainer,
                            getCertificatePem(
                                sessionStatusResponse.cert?.value,
                            ),
                            roleDataRequest,
                        )

                    signatureInterface =
                        if (signedContainer.getSignatures().isEmpty()) {
                            null
                        } else {
                            signedContainer.getSignatures()
                                .last {
                                    it.validator.status == ValidatorInterface.Status.Invalid ||
                                        it.validator.status == ValidatorInterface.Status.Unknown
                                }
                        }

                    if (base64Hash.isNotEmpty()) {
                        LoggingUtil.debugLog(logTag, "Posting signature challenge response")

                        postSmartCreateSignatureChallengeResponse(base64Hash)
                        delay(INITIAL_STATUS_REQUEST_DELAY_IN_MILLISECONDS)

                        val requestString =
                            MessageUtil.toJsonString(getSignatureRequestV2(request, base64Hash))

                        LoggingUtil.debugLog(logTag, "Request: $requestString")

                        sessionStatusResponse =
                            doSessionStatusRequestLoop(
                                signedContainer,
                                sidRestServiceClient.getCreateSignature(
                                    sessionStatusResponse.result?.documentNumber, requestString,
                                ),
                                false,
                            )
                        if (sessionStatusResponse == null) {
                            LoggingUtil.errorLog(logTag, "Unable to get session status response")
                            return
                        }
                        LoggingUtil.debugLog(logTag, "SessionStatusResponse: $sessionStatusResponse")
                        LoggingUtil.debugLog(logTag, "Finalizing signature...")
                        containerWrapper.finalizeSignature(
                            signedContainer,
                            sessionStatusResponse.signature?.value,
                        )
                        LoggingUtil.debugLog(logTag, "Posting signature status response")
                        postSmartCreateSignatureStatusResponse(sessionStatusResponse)
                        return
                    } else {
                        val errorString = "Base64 (Prepare signature) is empty or null"
                        LoggingUtil.debugLog(logTag, errorString)
                        setErrorState(errorString)
                        return
                    }
                } catch (e: UnknownHostException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.NO_RESPONSE))
                    LoggingUtil.errorLog(
                        logTag,
                        "REST API certificate request failed. Unknown host. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    return
                } catch (e: SSLPeerUnverifiedException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE))
                    LoggingUtil.errorLog(
                        logTag,
                        "SSL handshake failed. Session status response. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    return
                } catch (e: IOException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.GENERAL_ERROR, e.message))
                    LoggingUtil.errorLog(
                        logTag,
                        "REST API certificate request failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    return
                } catch (e: CertificateException) {
                    postFault(ServiceFault(SessionStatusResponseProcessStatus.GENERAL_ERROR, e.message))
                    LoggingUtil.errorLog(
                        logTag,
                        "Generating certificate failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    return
                } catch (e: InterruptedException) {
                    val errorString =
                        "Waiting for next call to SID REST API interrupted. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}"
                    LoggingUtil.errorLog(logTag, errorString, e)
                    setErrorState(errorString)
                    return
                } catch (e: NoSuchAlgorithmException) {
                    postFault(
                        ServiceFault(
                            SessionStatusResponseProcessStatus.GENERAL_ERROR,
                            e.message,
                        ),
                    )
                    LoggingUtil.errorLog(
                        logTag,
                        "Generating verification code failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                    )
                    return
                } catch (e: Exception) {
                    LoggingUtil.errorLog(
                        logTag,
                        "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )

                    // If user has cancelled signing, do not show any message
                    if (e is SigningCancelledException) {
                        setStatus(SessionStatusResponseProcessStatus.USER_CANCELLED)
                        return
                    }

                    if (!e.message.isNullOrEmpty() && e.message?.contains("Too Many Requests") == true) {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS))
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID - Too Many Requests. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    } else if (!e.message.isNullOrEmpty() && e.message?.contains(
                            "OCSP response not in valid time slot",
                        ) == true
                    ) {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT))
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID - OCSP response not in valid time slot. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    } else if (!e.message.isNullOrEmpty() &&
                        e.message?.contains("Certificate status: revoked") == true
                    ) {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.CERTIFICATE_REVOKED))
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID - Certificate status: revoked. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    } else if (!e.message.isNullOrEmpty() &&
                        e.message?.contains("Failed to connect") == true
                    ) {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.NO_RESPONSE))
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID - Failed to connect to host. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    } else if (!e.message.isNullOrEmpty() && e.message?.startsWith(
                            "Failed to create ssl connection with host",
                        ) == true
                    ) {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE))
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID - Failed to create ssl connection with host. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    } else {
                        postFault(
                            ServiceFault(SessionStatusResponseProcessStatus.GENERAL_ERROR, e.message),
                        )
                        LoggingUtil.errorLog(
                            logTag,
                            "Failed to sign with Smart-ID. " +
                                "Exception message: ${e.message}. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                    }
                    return
                }
            } else {
                val errorString = "Invalid request"
                LoggingUtil.errorLog(logTag, errorString)
                setErrorState(errorString)
                return
            }
        }

        private fun createNotificationChannel(context: Context) {
            LoggingUtil.debugLog(logTag, "Creating notification channel")
            NotificationUtil.createNotificationChannel(
                context,
                Constant.SmartIdConstants.NOTIFICATION_CHANNEL,
                Constant.SmartIdConstants.NOTIFICATION_NAME,
            )
        }

        private fun showEmptyNotification(context: Context) {
            createNotificationChannel(context)
            val notification: Notification? =
                NotificationUtil.createNotification(
                    context,
                    Constant.SmartIdConstants.NOTIFICATION_CHANNEL,
                    R.mipmap.ic_launcher,
                    null,
                    null,
                    NotificationCompat.PRIORITY_MIN,
                    true,
                )
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(Constant.SmartIdConstants.NOTIFICATION_PERMISSION_CODE, notification)
        }

        private fun getCertificatePem(cert: String?): String {
            return Constant.SmartIdConstants.PEM_BEGIN_CERT + "\n" + cert +
                "\n" + Constant.SmartIdConstants.PEM_END_CERT
        }

        @Throws(IOException::class, SigningCancelledException::class)
        private fun doSessionStatusRequestLoop(
            signedContainer: SignedContainer,
            request: Call<SessionResponse>,
            certRequest: Boolean,
        ): SessionStatusResponse? {
            var timeout: Long = 0
            val sessionResponse: SessionResponse? = handleRequest(signedContainer, request)
            if (sessionResponse == null) {
                LoggingUtil.errorLog(logTag, "Session response null")
                return null
            }
            LoggingUtil.debugLog(logTag, sessionResponse.toString())

            if (sessionResponse.sessionID.isNullOrEmpty()) {
                postFault(ServiceFault(SessionStatusResponseProcessStatus.MISSING_SESSIONID))
                LoggingUtil.debugLog(logTag, "Received empty Smart-ID session response")
                return null
            }

            while (timeout < TIMEOUT_CANCEL) {
                LoggingUtil.debugLog(logTag, "doSessionStatusRequestLoop timeout counter: $timeout")
                val sessionStatusResponse: SessionStatusResponse? =
                    handleRequest(
                        signedContainer,
                        sidRestServiceClient.getSessionStatus(
                            sessionResponse.sessionID,
                            SUBSEQUENT_STATUS_REQUEST_DELAY_IN_MILLISECONDS,
                        ),
                    )
                if (sessionStatusResponse == null) {
                    LoggingUtil.errorLog(logTag, "No session status response")
                    return null
                }
                LoggingUtil.debugLog(
                    logTag,
                    "doSessionStatusRequestLoop session response: $sessionResponse",
                )
                if (sessionStatusResponse.state == SessionStatusResponseProcessState.COMPLETE) {
                    val status: SessionStatusResponseProcessStatus? =
                        sessionStatusResponse.result?.endResult
                    if (status == SessionStatusResponseProcessStatus.OK) {
                        return sessionStatusResponse
                    }
                    postSmartCreateSignatureStatusResponse(sessionStatusResponse)
                    LoggingUtil.debugLog(logTag, "Received Smart-ID session status response: $status")

                    return null
                }
                if (certRequest) {
                    postSmartCreateSignatureSelectDevice()
                }
                timeout += SUBSEQUENT_STATUS_REQUEST_DELAY_IN_MILLISECONDS
            }
            postFault(ServiceFault(SessionStatusResponseProcessStatus.TIMEOUT))
            LoggingUtil.debugLog(logTag, "Request timeout (TIMEOUT)")

            return null
        }

        private fun getSignatureRequestV2(
            request: SmartCreateSignatureRequest,
            hash: String,
        ): PostCreateSignatureRequestV2 {
            LoggingUtil.debugLog(logTag, "Signature request V2: $request")
            val allowedInteractionsOrder =
                RequestAllowedInteractionsOrder(
                    "confirmationMessageAndVerificationCodeChoice",
                    request.displayText,
                )
            val signatureRequest =
                PostCreateSignatureRequestV2(
                    request.relyingPartyName,
                    request.relyingPartyUUID,
                    hash,
                    request.hashType,
                    listOf(allowedInteractionsOrder),
                )

            return signatureRequest
        }

        @Throws(IOException::class, SigningCancelledException::class, IllegalStateException::class)
        private fun <S> handleRequest(
            signedContainer: SignedContainer,
            request: Call<S>,
        ): S? {
            checkSigningCancelled(signedContainer)
            val httpResponse = request.clone().execute()
            if (!httpResponse.isSuccessful) {
                LoggingUtil.debugLog(
                    logTag,
                    "Smart-ID request unsuccessful. Status: ${httpResponse.code()}, " +
                        "message: ${httpResponse.message()}, " +
                        "body: ${httpResponse.body()}, " +
                        "errorBody: ${httpResponse.errorBody()}",
                )
                when (httpResponse.code()) {
                    401, 403 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS))
                        LoggingUtil.debugLog(
                            logTag,
                            "Forbidden - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    404 -> {
                        postFault(
                            ServiceFault(
                                if (httpResponse.body() is SessionResponse) {
                                    SessionStatusResponseProcessStatus.SESSION_NOT_FOUND
                                } else {
                                    SessionStatusResponseProcessStatus.ACCOUNT_NOT_FOUND
                                },
                            ),
                        )
                        LoggingUtil.debugLog(
                            logTag,
                            "Account/session not found - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    409 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS))
                        LoggingUtil.debugLog(
                            logTag,
                            "Exceeded unsuccessful requests - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    429 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS))
                        LoggingUtil.debugLog(
                            logTag,
                            "Too many requests - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    471 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.NOT_QUALIFIED))
                        LoggingUtil.debugLog(
                            logTag,
                            "Not qualified - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    480 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.OLD_API))
                        LoggingUtil.debugLog(
                            logTag,
                            "Old API - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    580 -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.UNDER_MAINTENANCE))
                        LoggingUtil.debugLog(
                            logTag,
                            "Under maintenance - HTTP status code: ${httpResponse.code()}",
                        )
                    }

                    else -> {
                        postFault(ServiceFault(SessionStatusResponseProcessStatus.TECHNICAL_ERROR))
                        LoggingUtil.debugLog(
                            logTag,
                            "Request unsuccessful, technical or general error, " +
                                "HTTP status code: ${httpResponse.code()}",
                        )
                    }
                }
                return null
            }
            LoggingUtil.debugLog(
                logTag,
                "Response status: ${httpResponse.code()}, response body: ${httpResponse.body()}",
            )
            LoggingUtil.debugLog(
                logTag,
                "Smart-ID request: isSuccessful: ${httpResponse.isSuccessful}, " +
                    "status: ${httpResponse.code()}, " +
                    "message: ${httpResponse.message()}, " +
                    "body: ${httpResponse.body()}, " +
                    "errorBody: ${httpResponse.errorBody()}",
            )
            return httpResponse.body()
        }

        private fun checkSigningCancelled(signedContainer: SignedContainer) {
            if (_cancelled.value == true) {
                signatureInterface?.let { signedContainer.removeSignature(it) }

                throw SigningCancelledException("User cancelled signing")
            }
        }

        private fun generateSmartIdResponse(response: SessionStatusResponse): SmartIDServiceResponse {
            LoggingUtil.debugLog(logTag, "Generating Smart ID response: $response")
            val smartIdResponse =
                SmartIDServiceResponse(
                    response.result?.endResult,
                )
            return smartIdResponse
        }

        private fun postFault(fault: ServiceFault) {
            LoggingUtil.debugLog(logTag, "Updating fault: $fault")

            setResponse(null)
            setErrorState(fault.detailMessage)
            setStatus(fault.status)
            setChallenge(null)
            setSelectDevice(false)
        }

        private fun postSmartCreateSignatureSelectDevice() {
            LoggingUtil.debugLog(logTag, "User selecting device")
            setResponse(null)
            setErrorState(null)
            setStatus(null)
            setChallenge(null)
            setSelectDevice(true)
        }

        private fun postSmartCreateSignatureStatusResponse(response: SessionStatusResponse) {
            LoggingUtil.debugLog(logTag, "postSmartCreateSignatureStatusResponse: $response")
            val smartIdServiceResponse = generateSmartIdResponse(response)
            setResponse(smartIdServiceResponse)
            setErrorState(null)
            setStatus(smartIdServiceResponse.status)
            setChallenge(null)
            setSelectDevice(false)
            LoggingUtil.debugLog(logTag, "Smart-ID service response: $smartIdServiceResponse")
        }

        @Throws(NoSuchAlgorithmException::class)
        private fun postSmartCreateSignatureChallengeResponse(base64Hash: String) {
            LoggingUtil.debugLog(logTag, "Signature challenge")

            val verificationCode =
                VerificationCodeUtil.calculateSmartIdVerificationCode(
                    Base64.decode(base64Hash),
                )

            setResponse(null)
            setErrorState(null)
            setStatus(null)
            setChallenge(verificationCode)
        }

        private fun getCertificateRequest(request: SmartCreateSignatureRequest): PostCertificateRequest {
            val certificateRequest =
                PostCertificateRequest(
                    request.relyingPartyName,
                    request.relyingPartyUUID,
                )
            LoggingUtil.debugLog(logTag, "Certificate request: $request")
            return certificateRequest
        }

        companion object {
            private const val INITIAL_STATUS_REQUEST_DELAY_IN_MILLISECONDS: Long = 1000
            private const val SUBSEQUENT_STATUS_REQUEST_DELAY_IN_MILLISECONDS = (5 * 1000).toLong()
            private const val TIMEOUT_CANCEL = (80 * 1000).toLong()
        }
    }
