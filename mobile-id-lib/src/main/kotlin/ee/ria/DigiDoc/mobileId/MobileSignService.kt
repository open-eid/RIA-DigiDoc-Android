@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ee.ria.DigiDoc.common.Constant.PEM_BEGIN_CERT
import ee.ria.DigiDoc.common.Constant.PEM_END_CERT
import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.common.model.AppState
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.MobileIdServiceResponse
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.SigningCancelledException
import ee.ria.DigiDoc.mobileId.utils.VerificationCodeUtil
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType
import ee.ria.DigiDoc.network.mid.dto.request.GetMobileCreateSignatureSessionStatusRequest
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.network.mid.dto.request.PostMobileCreateSignatureCertificateRequest
import ee.ria.DigiDoc.network.mid.dto.request.PostMobileCreateSignatureSessionRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureCertificateResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessState
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionStatusResponse
import ee.ria.DigiDoc.network.mid.dto.response.RESTServiceFault
import ee.ria.DigiDoc.network.mid.rest.MIDRestServiceClient
import ee.ria.DigiDoc.network.mid.rest.ServiceGenerator
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.utilsLib.extensions.removeWhitespaces
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import ee.ria.DigiDoc.utilsLib.signing.TrustManagerUtil
import ee.ria.DigiDoc.utilsLib.signing.UUIDUtil
import ee.ria.DigiDoc.utilsLib.text.MessageUtil
import ee.ria.libdigidocpp.Container
import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import org.bouncycastle.util.encoders.Base64
import retrofit2.Call
import retrofit2.Response
import java.io.FileInputStream
import java.io.IOException
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.TrustManager

interface MobileSignService {
    val response: LiveData<MobileIdServiceResponse?>
    val challenge: LiveData<String?>
    val status: LiveData<MobileCreateSignatureProcessStatus?>
    val result: LiveData<MobileCertificateResultType?>
    val errorState: LiveData<String?>
    val cancelled: LiveData<Boolean?>

    fun setCancelled(
        signedContainer: SignedContainer,
        cancelled: Boolean?,
    )

    fun resetValues()

    suspend fun processMobileIdRequest(
        context: Context,
        signedContainer: SignedContainer,
        request: MobileCreateSignatureRequest?,
        roleDataRequest: RoleData?,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
        certificateBundle: ArrayList<String>?,
        accessTokenPath: String?,
        accessTokenPass: String?,
    )
}

@Singleton
class MobileSignServiceImpl
    @Inject
    constructor(
        private val serviceGenerator: ServiceGenerator,
        private val containerWrapper: ContainerWrapper,
    ) : MobileSignService {
        private val logTag = javaClass.simpleName
        private var timeout: Long = 0
        private lateinit var midRestServiceClient: MIDRestServiceClient

        private val _response = MutableLiveData<MobileIdServiceResponse?>(null)
        override val response: LiveData<MobileIdServiceResponse?> = _response
        private val _errorState = MutableLiveData<String?>(null)
        override val errorState: LiveData<String?> = _errorState
        private val _challenge = MutableLiveData<String?>(null)
        override val challenge: LiveData<String?> = _challenge
        private val _status = MutableLiveData<MobileCreateSignatureProcessStatus?>(null)
        override val status: LiveData<MobileCreateSignatureProcessStatus?> = _status
        private val _result = MutableLiveData<MobileCertificateResultType?>(null)
        override val result: LiveData<MobileCertificateResultType?> = _result

        private val _cancelled = MutableLiveData(false)
        override val cancelled: LiveData<Boolean?> = _cancelled

        private var signatureInterface: SignatureInterface? = null

        override fun resetValues() {
            _response.postValue(null)
            _errorState.postValue(null)
            _challenge.postValue(null)
            _status.postValue(null)
            _result.postValue(null)
            _cancelled.postValue(false)
        }

        override fun setCancelled(
            signedContainer: SignedContainer,
            cancelled: Boolean?,
        ) {
            try {
                signatureInterface?.let {
                    signedContainer.removeSignature(it)
                }
            } catch (e: Exception) {
                errorLog(
                    logTag,
                    "Failed to remove signature from container. Exception message: ${e.message}. " +
                        "Exception: ${e.stackTrace.contentToString()}",
                    e,
                )
            }
            _cancelled.postValue(cancelled)
        }

        private fun setResponse(response: MobileIdServiceResponse?) {
            _response.postValue(response)
        }

        private fun setErrorState(errorState: String?) {
            _errorState.postValue(errorState)
        }

        private fun setChallenge(challenge: String?) {
            _challenge.postValue(challenge)
        }

        private fun setStatus(status: MobileCreateSignatureProcessStatus?) {
            _status.postValue(status)
        }

        private fun setResult(result: MobileCertificateResultType?) {
            _result.postValue(result)
        }

        override suspend fun processMobileIdRequest(
            context: Context,
            signedContainer: SignedContainer,
            request: MobileCreateSignatureRequest?,
            roleDataRequest: RoleData?,
            proxySetting: ProxySetting?,
            manualProxySettings: ManualProxy,
            certificateBundle: ArrayList<String>?,
            accessTokenPath: String?,
            accessTokenPass: String?,
        ) {
            debugLog(logTag, "Handling mobile sign service")

            val trustManagers: Array<TrustManager>
            try {
                trustManagers = TrustManagerUtil.trustManagers
            } catch (e: NoSuchAlgorithmException) {
                val errorString =
                    "Unable to get Trust Managers. Exception message: ${e.message}. " +
                        "Exception: ${e.stackTrace.contentToString()}"
                errorLog(logTag, errorString, e)
                setErrorState(errorString)
                return
            } catch (e: KeyStoreException) {
                val errorString =
                    "Unable to get Trust Managers. Exception message: ${e.message}. " +
                        "Exception: ${e.stackTrace.contentToString()}"
                errorLog(logTag, errorString, e)
                setErrorState(errorString)
                return
            }

            timeout = 0

            if (request != null) {
                val certificateRequest: PostMobileCreateSignatureCertificateRequest =
                    getCertificateRequest(request)
                debugLog(logTag, "Certificate request: $certificateRequest")
                var restSSLConfig: SSLContext?
                try {
                    debugLog(logTag, "Creating SSL config")
                    restSSLConfig = createSSLConfig(accessTokenPath, accessTokenPass, trustManagers)
                } catch (e: Exception) {
                    errorLog(
                        logTag,
                        "Can't create SSL config. Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    restSSLConfig = null
                }

                try {
                    if (certificateBundle != null) {
                        midRestServiceClient =
                            serviceGenerator.createService(
                                context,
                                restSSLConfig,
                                request.url,
                                certificateBundle,
                                trustManagers,
                                proxySetting,
                                manualProxySettings,
                            )
                    } else {
                        val errorString = "Certificate cert bundle is null"
                        debugLog(logTag, "Certificate cert bundle is null")
                        setErrorState(errorString)
                        return
                    }
                } catch (iae: IllegalArgumentException) {
                    errorLog(
                        logTag,
                        "Can't create create service. Exception message: ${iae.message}. " +
                            "Exception: ${iae.stackTrace.contentToString()}",
                        iae,
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.GENERAL_ERROR))
                    return
                } catch (e: CertificateException) {
                    errorLog(
                        logTag,
                        "Can't create MIDRestServiceClient. Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE))
                    return
                } catch (e: NoSuchAlgorithmException) {
                    errorLog(
                        logTag,
                        "Can't create MIDRestServiceClient. Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE))
                    return
                }

                if (isCountryCodeError(request.phoneNumber)) {
                    debugLog(logTag, "Failed to sign with Mobile-ID. Invalid country code")
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_COUNTRY_CODE))
                    return
                }

                if (!UUIDUtil.isValid(request.relyingPartyUUID)) {
                    debugLog(
                        logTag,
                        "Failed to sign with Mobile-ID. ${request.relyingPartyUUID} - " +
                            "Relying Party UUID not in valid format",
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS))
                    return
                }

                try {
                    checkSigningCancelled(signedContainer)
                    val call: Call<MobileCreateSignatureCertificateResponse> =
                        midRestServiceClient.getCertificate(certificateRequest)

                    val responseWrapper: Response<MobileCreateSignatureCertificateResponse> =
                        call.execute()
                    if (!responseWrapper.isSuccessful) {
                        debugLog(
                            logTag,
                            "Mobile-ID certificate request unsuccessful. " +
                                "Status: ${responseWrapper.code()}, " +
                                "message: ${responseWrapper.message()}, " +
                                "body: ${responseWrapper.body()}, " +
                                "errorBody: ${responseWrapper.errorBody()}",
                        )
                        parseMobileCreateSignatureCertificateResponseAndPost(responseWrapper)
                        return
                    } else {
                        val response: MobileCreateSignatureCertificateResponse? = responseWrapper.body()
                        if (response == null) {
                            val errorString = "Mobile-ID signature certificate response is null"
                            debugLog(logTag, errorString)
                            setErrorState(errorString)
                            return
                        }
                        debugLog(logTag, "MobileCreateSignatureCertificateResponse response body: $response")
                        if (isResponseError(
                                responseWrapper,
                                response,
                                MobileCreateSignatureCertificateResponse::class.java,
                            )
                        ) {
                            return
                        }

                        val signerCert = getCertificate(response.cert)
                        val signer = ExternalSigner(signerCert)
                        signer.setProfile(SIGNATURE_PROFILE_TS)
                        signer.setUserAgent(UserAgentUtil.getUserAgent(context))

                        val dataToSignBytes =
                            Base64.encode(
                                containerWrapper.prepareSignature(
                                    signer,
                                    signedContainer,
                                    signerCert,
                                    roleDataRequest,
                                ),
                            )

                        val base64Hash =
                            String(dataToSignBytes, StandardCharsets.UTF_8)
                                .removeWhitespaces()

                        val containerSignatures = signedContainer.getSignatures(Main)

                        signatureInterface =
                            if (containerSignatures.isEmpty()) {
                                null
                            } else {
                                containerSignatures
                                    .lastOrNull {
                                        it.validator.status == ValidatorInterface.Status.Invalid ||
                                            it.validator.status == ValidatorInterface.Status.Unknown
                                    }
                            }
                        if (base64Hash.isNotEmpty()) {
                            debugLog(logTag, "Posting create signature response")
                            postMobileCreateSignatureResponse(base64Hash)
                            sleep(INITIAL_STATUS_REQUEST_DELAY_IN_MILLISECONDS)
                            val sessionId = getMobileIdSession(base64Hash, signedContainer, request)
                            if (sessionId == null) {
                                val errorString = "Session ID missing"
                                debugLog(logTag, errorString)
                                setErrorState(errorString)

                                return
                            }
                            debugLog(logTag, "Session ID: $sessionId")
                            doCreateSignatureStatusRequestLoop(
                                signer,
                                signedContainer,
                                GetMobileCreateSignatureSessionStatusRequest(sessionId),
                            )
                        } else {
                            val errorString = "Base64 (Prepare signature) is empty or null"
                            debugLog(logTag, errorString)
                            setErrorState(errorString)

                            return
                        }
                    }
                } catch (e: UnknownHostException) {
                    errorLog(
                        logTag,
                        "Failed to sign with Mobile-ID. REST API certificate request failed. " +
                            "Unknown host. Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.NO_RESPONSE))
                    return
                } catch (e: SSLPeerUnverifiedException) {
                    errorLog(
                        logTag,
                        "Failed to sign with Mobile-ID. SSL handshake failed. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE))
                    return
                } catch (e: IOException) {
                    val message = e.message

                    if (message != null && message.contains("CONNECT: 403")) {
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.NO_RESPONSE))
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID. " +
                                "REST API certificate request failed. Received HTTP status 403. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        return
                    } else if (message != null && (
                            ProxyUtil.getProxySetting(context) !== ProxySetting.NO_PROXY &&
                                message.contains("Failed to authenticate with proxy")
                        )
                    ) {
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_PROXY_SETTINGS))
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID. " +
                                "REST API certificate request failed with current proxy settings. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        return
                    }
                    errorLog(
                        logTag,
                        "Failed to sign with Mobile-ID. REST API certificate request failed. " +
                            "Exception message: $message. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(defaultError(message))
                    return
                } catch (e: CertificateException) {
                    errorLog(
                        logTag,
                        "Failed to sign with Mobile-ID. Certificate exception. " +
                            "Exception message: ${e.message}. " +
                            "Exception: ${e.stackTrace.contentToString()}",
                        e,
                    )
                    postFault(defaultError(e.message))
                    return
                } catch (e: SigningCancelledException) {
                    errorLog(
                        logTag,
                        "Failed to sign with Mobile-ID. User cancelled signing. " +
                            "Exception message: ${e.message}. Exception: ${
                                e.stackTrace.contentToString()
                            }",
                        e,
                    )
                    // If user has cancelled signing, do not show any message
                    setStatus(MobileCreateSignatureProcessStatus.USER_CANCELLED)
                    return
                } catch (e: Exception) {
                    val message = e.message
                    if (!message.isNullOrEmpty() && message.contains("Too Many Requests")) {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID - Too Many Requests. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS))
                    } else if (!message.isNullOrEmpty() &&
                        message.contains(
                            "OCSP response not in valid time slot",
                        )
                    ) {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID - OCSP response not in valid time slot. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.OCSP_INVALID_TIME_SLOT))
                    } else if (!message.isNullOrEmpty() && message.contains("Certificate status: revoked")) {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID - Certificate status: revoked. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.CERTIFICATE_REVOKED))
                    } else if (!message.isNullOrEmpty() && message.contains("Failed to connect")) {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID - Failed to connect to host. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.NO_RESPONSE))
                    } else if (!message.isNullOrEmpty() &&
                        message.startsWith("Failed to create ssl connection with host")
                    ) {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID - Failed to create ssl connection with host. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE))
                    } else {
                        errorLog(
                            logTag,
                            "Failed to sign with Mobile-ID. Technical or general error. " +
                                "Exception message: $message. " +
                                "Exception: ${e.stackTrace.contentToString()}",
                            e,
                        )
                        postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR))
                    }
                    return
                }
            }
            return
        }

        private fun getCertificate(cert: String?): ByteArray {
            val certPemString = (PEM_BEGIN_CERT + "\n" + cert + "\n" + PEM_END_CERT).trimIndent()

            return certPemString.let {
                CertificateUtil.x509Certificate(it).encoded
            }
        }

        @Throws(IOException::class, SigningCancelledException::class)
        private suspend fun doCreateSignatureStatusRequestLoop(
            signer: ExternalSigner,
            signedContainer: SignedContainer,
            request: GetMobileCreateSignatureSessionStatusRequest,
        ) {
            try {
                checkSigningCancelled(signedContainer)
            } catch (sce: SigningCancelledException) {
                errorLog(logTag, "Unable to sign with Mobile-ID. Signing has been cancelled", sce)
                return
            }

            // Wait until the app is in the foreground to avoid networking errors
            while (!AppState.isAppInForeground) {
                debugLog(logTag, "Mobile-ID: App is in the background, waiting to return to foreground...")
                delay(1000)
                timeout += 1000
            }

            val responseCall: Call<MobileCreateSignatureSessionStatusResponse> =
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    request.sessionId,
                    request.timeoutMs,
                )

            val responseWrapper: Response<MobileCreateSignatureSessionStatusResponse> =
                responseCall.clone().execute()
            debugLog(
                logTag,
                "MobileCreateSignatureSessionStatusResponse response: $responseWrapper",
            )
            if (!responseWrapper.isSuccessful) {
                debugLog(
                    logTag,
                    "MobileCreateSignatureSessionStatusResponse responseWrapper unsuccessful: $responseWrapper",
                )
                parseMobileCreateSignatureSessionStatusResponseAndPost(responseWrapper)
                return
            }

            val response: MobileCreateSignatureSessionStatusResponse? = responseWrapper.body()
            debugLog(
                logTag,
                "MobileCreateSignatureSessionStatusResponse response body: ${response?.toString() ?: "null"}",
            )
            if (response != null && response.state?.let { isSessionStatusRequestComplete(it) } == true) {
                if (isResponseError(
                        responseWrapper,
                        response,
                        MobileCreateSignatureSessionStatusResponse::class.java,
                    )
                ) {
                    debugLog(logTag, "Response error: $responseWrapper")
                    if (response.result == MobileCreateSignatureProcessStatus.USER_CANCELLED) {
                        return
                    }
                    throw IOException(
                        java.lang.String.format(
                            "Error getting response: %s",
                            responseWrapper.errorBody(),
                        ),
                    )
                }
                debugLog(logTag, "Finalizing signature...")
                val signatureValueBytes: ByteArray = Base64.decode(response.signature?.value)
                containerWrapper.finalizeSignature(signer, signedContainer, signatureValueBytes)
                debugLog(logTag, "Posting create signature status response")
                signedContainer.rawContainer()?.let {
                    postMobileCreateSignatureStatusResponse(
                        response,
                        it,
                    )
                }
                return
            } else {
                if (timeout > TIMEOUT_CANCEL) {
                    debugLog(logTag, "Timeout: doCreateSignatureStatusRequestLoop timeout counter: $timeout")
                    postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TIMEOUT))
                    debugLog(logTag, "Failed to sign with Mobile-ID. Request timeout")
                    return
                }
                debugLog(logTag, "doCreateSignatureStatusRequestLoop timeout counter: $timeout")
                sleep(SUBSEQUENT_STATUS_REQUEST_DELAY_IN_MILLISECONDS)
                doCreateSignatureStatusRequestLoop(signer, signedContainer, request)
            }
        }

        private suspend fun sleep(millis: Long) {
            timeout += millis
            delay(millis)
        }

        private fun isSessionStatusRequestComplete(state: MobileCreateSignatureProcessState): Boolean {
            return state == MobileCreateSignatureProcessState.COMPLETE
        }

        @Throws(SigningCancelledException::class)
        private fun checkSigningCancelled(signedContainer: SignedContainer) {
            if (_cancelled.value == true) {
                try {
                    signatureInterface?.let { signedContainer.removeSignature(it) }
                } catch (e: Exception) {
                    debugLog(
                        logTag,
                        "Unable to remove signature from container after " +
                            "cancelling Mobile-ID signing in app: ${e.localizedMessage}",
                        e,
                    )
                }

                throw SigningCancelledException("User cancelled signing")
            }
        }

        @Throws(IOException::class, SigningCancelledException::class)
        private fun getMobileIdSession(
            hash: String,
            signedContainer: SignedContainer,
            request: MobileCreateSignatureRequest?,
        ): String? {
            val sessionRequest: PostMobileCreateSignatureSessionRequest = getSessionRequest(request)
            debugLog(logTag, "Session request: $sessionRequest")
            sessionRequest.hash = hash
            debugLog(logTag, "Request hash: $hash")

            val requestString: String = MessageUtil.toJsonString(sessionRequest)
            debugLog(logTag, "Request string: $requestString")

            try {
                checkSigningCancelled(signedContainer)
            } catch (sce: SigningCancelledException) {
                errorLog(logTag, "Unable to sign with Mobile-ID. Signing has been cancelled", sce)
                return null
            }

            val call: Call<MobileCreateSignatureSessionResponse> =
                midRestServiceClient.getMobileCreateSession(requestString)

            val sessionResponse: MobileCreateSignatureSessionResponse?

            val responseWrapper: Response<MobileCreateSignatureSessionResponse> = call.execute()
            if (!responseWrapper.isSuccessful) {
                debugLog(
                    logTag,
                    "Mobile-ID session request unsuccessful. " +
                        "Status: ${responseWrapper.code()}, " +
                        "message: ${responseWrapper.message()}, " +
                        "body: ${responseWrapper.body()}, " +
                        "errorBody: ${responseWrapper.errorBody()}",
                )
                if (!isResponseError(
                        responseWrapper,
                        responseWrapper.body(),
                        MobileCreateSignatureSessionResponse::class.java,
                    )
                ) {
                    parseMobileCreateSignatureSessionResponseAndPost(responseWrapper)
                }
                return null
            } else {
                sessionResponse = responseWrapper.body()
                debugLog(logTag, "Session response: $sessionResponse")
            }

            return sessionResponse?.sessionID
        }

        private fun parseMobileCreateSignatureCertificateResponseAndPost(
            responseWrapper: Response<MobileCreateSignatureCertificateResponse>,
        ) {
            if (responseWrapper.code() == 429) {
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. Too many requests, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS))
            } else if (responseWrapper.code() == 401) {
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. Invalid access rights, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS))
            } else if (responseWrapper.code() == 409) {
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. Exceeded unsuccessful requests, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS))
            } else {
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Request unsuccessful, technical or general error, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR))
            }
        }

        private fun parseMobileCreateSignatureSessionResponseAndPost(
            responseWrapper: Response<MobileCreateSignatureSessionResponse>,
        ) {
            if (responseWrapper.code() == 429) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Too many requests, HTTP status code: ${responseWrapper.code()}",
                )
            } else if (responseWrapper.code() == 401) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Invalid access rights, HTTP status code: ${responseWrapper.code()}",
                )
            } else if (responseWrapper.code() == 409) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Exceeded unsuccessful requests, HTTP status code: ${responseWrapper.code()}",
                )
            } else {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Request unsuccessful, technical or general error, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
            }
        }

        private fun parseMobileCreateSignatureSessionStatusResponseAndPost(
            responseWrapper: Response<MobileCreateSignatureSessionStatusResponse>,
        ) {
            if (responseWrapper.code() == 429) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Too many requests, HTTP status code: ${responseWrapper.code()}",
                )
            } else if (responseWrapper.code() == 401) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Invalid access rights, HTTP status code: ${responseWrapper.code()}",
                )
            } else if (responseWrapper.code() == 409) {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Exceeded unsuccessful requests, HTTP status code: ${responseWrapper.code()}",
                )
            } else {
                postFault(RESTServiceFault(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR))
                debugLog(
                    logTag,
                    "Failed to sign with Mobile-ID. " +
                        "Request unsuccessful, technical or general error, " +
                        "HTTP status code: ${responseWrapper.code()}",
                )
            }
        }

        private fun postFault(fault: RESTServiceFault) {
            debugLog(logTag, "Updating fault: $fault")

            setResponse(null)
            setErrorState(null)
            setStatus(fault.status)
            setResult(fault.result)
            setChallenge(null)
        }

        private fun postMobileCreateSignatureStatusResponse(
            response: MobileCreateSignatureSessionStatusResponse,
            container: Container,
        ) {
            debugLog(logTag, "Posting create signature status response: $response")
            val mobileIdServiceResponse = generateMobileIdResponse(response, container)

            setResponse(mobileIdServiceResponse)
            setErrorState(null)
            setStatus(mobileIdServiceResponse.status)
            setChallenge(null)
        }

        private fun postMobileCreateSignatureResponse(base64Hash: String) {
            debugLog(logTag, "Posting create signature response: $base64Hash")
            val verificationCode =
                VerificationCodeUtil.calculateMobileIdVerificationCode(
                    Base64.decode(base64Hash),
                )

            setResponse(null)
            setErrorState(null)
            setStatus(null)
            setChallenge(verificationCode)
        }

        private fun isCountryCodeError(phoneNumber: String?): Boolean {
            if (phoneNumber != null) {
                return phoneNumber.length <= 9
            }
            return true
        }

        private fun generateMobileIdResponse(
            response: MobileCreateSignatureSessionStatusResponse,
            container: Container,
        ): MobileIdServiceResponse {
            val mobileIdResponse = MobileIdServiceResponse()
            mobileIdResponse.container = container
            mobileIdResponse.status = response.result
            mobileIdResponse.signature = response.signature?.value

            debugLog(
                logTag,
                "Mobile-ID status: ${mobileIdResponse.status}, signature: ${mobileIdResponse.signature}",
            )
            return mobileIdResponse
        }

        private fun getCertificateRequest(
            signatureRequest: MobileCreateSignatureRequest?,
        ): PostMobileCreateSignatureCertificateRequest {
            val certificateRequest = PostMobileCreateSignatureCertificateRequest()
            certificateRequest.relyingPartyName = signatureRequest?.relyingPartyName
            certificateRequest.relyingPartyUUID = signatureRequest?.relyingPartyUUID
            certificateRequest.phoneNumber = signatureRequest?.phoneNumber
            certificateRequest.nationalIdentityNumber = signatureRequest?.nationalIdentityNumber
            debugLog(logTag, "Mobile-ID certificate request: $certificateRequest")
            return certificateRequest
        }

        private fun getSessionRequest(
            signatureRequest: MobileCreateSignatureRequest?,
        ): PostMobileCreateSignatureSessionRequest {
            val sessionRequest = PostMobileCreateSignatureSessionRequest()
            sessionRequest.relyingPartyUUID = signatureRequest?.relyingPartyUUID
            sessionRequest.relyingPartyName = signatureRequest?.relyingPartyName
            sessionRequest.phoneNumber = signatureRequest?.phoneNumber
            sessionRequest.nationalIdentityNumber = signatureRequest?.nationalIdentityNumber
            sessionRequest.language = signatureRequest?.language
            sessionRequest.hashType = signatureRequest?.hashType
            sessionRequest.displayText = signatureRequest?.displayText
            sessionRequest.displayTextFormat = signatureRequest?.displayTextFormat
            debugLog(logTag, "Mobile-ID session request: $sessionRequest")
            return sessionRequest
        }

        private fun <S> isResponseError(
            httpResponse: Response<S>,
            response: S?,
            responseClass: Class<S>,
        ): Boolean {
            try {
                if (responseClass == MobileCreateSignatureCertificateResponse::class.java) {
                    val certificateResponse: MobileCreateSignatureCertificateResponse? =
                        response as MobileCreateSignatureCertificateResponse?
                    if (certificateResponse != null) {
                        if (certificateResponse.result != MobileCertificateResultType.OK) {
                            val fault =
                                RESTServiceFault(
                                    httpStatus = httpResponse.code(),
                                    result = certificateResponse.result,
                                    time = certificateResponse.time,
                                    traceId = certificateResponse.traceId,
                                    error = certificateResponse.error,
                                )
                            postFault(fault)
                            debugLog(
                                logTag,
                                "Received Mobile-ID certificate response: $certificateResponse",
                            )
                            return true
                        }
                    }
                } else if (responseClass == MobileCreateSignatureSessionStatusResponse::class.java) {
                    val sessionStatusResponse: MobileCreateSignatureSessionStatusResponse? =
                        response as MobileCreateSignatureSessionStatusResponse?
                    if (sessionStatusResponse != null) {
                        if (sessionStatusResponse.result != MobileCreateSignatureProcessStatus.OK) {
                            val restServiceFault =
                                RESTServiceFault(
                                    httpStatus = httpResponse.code(),
                                    state = sessionStatusResponse.state,
                                    time = sessionStatusResponse.time,
                                    traceId = sessionStatusResponse.traceId,
                                    status = sessionStatusResponse.result,
                                    error = sessionStatusResponse.error,
                                )
                            postFault(restServiceFault)
                            debugLog(
                                logTag,
                                "Received Mobile-ID session status response: $sessionStatusResponse",
                            )
                            return true
                        }
                    }
                }
            } catch (e: ClassCastException) {
                postFault(defaultError(e.message))
                errorLog(
                    logTag,
                    "Failed to sign with Mobile-ID. Unable to get correct response type. " +
                        "Exception message: ${e.message}. " +
                        "Exception: ${e.stackTrace.contentToString()}",
                )
                return true
            }

            return false
        }

        private fun defaultError(detailMessage: String?): RESTServiceFault {
            debugLog(logTag, "Default error: $detailMessage")
            return RESTServiceFault(
                MobileCreateSignatureProcessStatus.GENERAL_ERROR,
                detailMessage,
            )
        }

        companion object {
            private const val INITIAL_STATUS_REQUEST_DELAY_IN_MILLISECONDS: Long = 1000
            private const val SUBSEQUENT_STATUS_REQUEST_DELAY_IN_MILLISECONDS = (5 * 1000).toLong()
            private const val TIMEOUT_CANCEL = (120 * 1000).toLong()

            @Throws(
                CertificateException::class,
                IOException::class,
                KeyStoreException::class,
                NoSuchAlgorithmException::class,
                KeyManagementException::class,
                UnrecoverableKeyException::class,
            )
            private fun createSSLConfig(
                accessTokenPath: String?,
                accessTokenPass: String?,
                trustManagers: Array<TrustManager>,
            ): SSLContext {
                FileInputStream(accessTokenPath).use { key ->
                    val keyStoreType = "PKCS12"
                    val keyStore =
                        KeyStore.getInstance(keyStoreType)
                    if (accessTokenPass != null) {
                        keyStore.load(key, accessTokenPass.toCharArray())
                    }
                    val kmf =
                        KeyManagerFactory.getInstance("X509")
                    kmf.init(keyStore, null)
                    val sslContext =
                        SSLContext.getInstance("TLS")
                    sslContext.createSSLEngine().enabledProtocols =
                        arrayOf("TLSv1.2", "TLSv1.3")
                    sslContext.init(kmf.keyManagers, trustManagers, null)
                    return sslContext
                }
            }
        }
    }
