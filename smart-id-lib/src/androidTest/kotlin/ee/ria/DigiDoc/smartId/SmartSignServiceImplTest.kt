@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionStatusResponse
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.sid.dto.request.PostCertificateRequest
import ee.ria.DigiDoc.network.sid.dto.request.SmartCreateSignatureRequest
import ee.ria.DigiDoc.network.sid.dto.response.SessionResponse
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponse
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessState
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseStatus
import ee.ria.DigiDoc.network.sid.dto.response.SmartCertificateResponse
import ee.ria.DigiDoc.network.sid.dto.response.SmartIDServiceResponse
import ee.ria.DigiDoc.network.sid.dto.response.SmartSignatureResponse
import ee.ria.DigiDoc.network.sid.rest.SIDRestServiceClient
import ee.ria.DigiDoc.network.sid.rest.ServiceGenerator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Response
import retrofit2.mock.Calls
import java.io.IOException
import java.net.UnknownHostException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.net.ssl.SSLPeerUnverifiedException

@RunWith(MockitoJUnitRunner::class)
class SmartSignServiceImplTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var serviceGenerator: ServiceGenerator
    private lateinit var containerWrapper: ContainerWrapper
    private lateinit var smartSignServiceImpl: SmartSignServiceImpl

    @Mock
    lateinit var responseObserver: Observer<SmartIDServiceResponse?>

    @Mock
    lateinit var errorStateObserver: Observer<String?>

    @Mock
    lateinit var challengeObserver: Observer<String?>

    @Mock
    lateinit var statusObserver: Observer<SessionStatusResponseProcessStatus?>

    @Mock
    lateinit var selectDeviceObserver: Observer<Boolean?>

    @Mock
    lateinit var cancelledObserver: Observer<Boolean?>

    private lateinit var signedContainer: SignedContainer

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    Initialization(configurationRepository)
                        .init(InstrumentationRegistry.getInstrumentation().targetContext)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context
    private val url = "https://tsp.demo.sk.ee/sid-api"
    private val certBundle = ArrayList(listOf("CERT000011112222", "CERT000011112223"))
    private val proxySetting = ProxySetting.NO_PROXY
    private val manualProxy = ManualProxy("", 80, "", "")
    private val sidRestServiceClient = mock(SIDRestServiceClient::class.java)

    private val getSmartCertificateRequest =
        PostCertificateRequest(
            relyingPartyName = "DEMO",
            relyingPartyUUID = "00000000-0000-0000-0000-000000000000",
        )
    private val request =
        SmartCreateSignatureRequest(
            country = "EE",
            nationalIdentityNumber = "60001019906",
            url = url,
            relyingPartyUUID = "00000000-0000-0000-0000-000000000000",
            relyingPartyName = "DEMO",
            containerPath = "containerPath",
            hashType = "SHA256",
            displayText = "displayText",
        )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        serviceGenerator = mock(ServiceGenerator::class.java)
        containerWrapper = mock(ContainerWrapper::class.java)
        smartSignServiceImpl =
            SmartSignServiceImpl(
                containerWrapper = containerWrapper,
                serviceGenerator = serviceGenerator,
            )
        smartSignServiceImpl.cancelled.observeForever(cancelledObserver)
        smartSignServiceImpl.response.observeForever(responseObserver)
        smartSignServiceImpl.errorState.observeForever(errorStateObserver)
        smartSignServiceImpl.challenge.observeForever(challengeObserver)
        smartSignServiceImpl.status.observeForever(statusObserver)
        smartSignServiceImpl.selectDevice.observeForever(selectDeviceObserver)
        smartSignServiceImpl.cancelled.observeForever(cancelledObserver)

        val container =
            getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )
        runBlocking {
            signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)
        }
    }

    @Test
    fun smartSignService_setCancelled_success() {
        smartSignServiceImpl.setCancelled(signedContainer, true)
        verify(cancelledObserver, atLeastOnce()).onChanged(true)
    }

    @Test
    fun mobileSignService_resetValues_success() {
        smartSignServiceImpl.resetValues()
        verify(responseObserver, atLeastOnce()).onChanged(null)
        verify(errorStateObserver, atLeastOnce()).onChanged(null)
        verify(challengeObserver, atLeastOnce()).onChanged(null)
        verify(statusObserver, atLeastOnce()).onChanged(null)
        verify(selectDeviceObserver, atLeastOnce()).onChanged(false)
        verify(cancelledObserver, atLeastOnce()).onChanged(false)
    }

    @Test
    fun smartSignService_processSmartIdRequest_requestIsNull() =
        runTest {
            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = null,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(errorStateObserver, atLeastOnce()).onChanged("Invalid request")
        }

    @Test
    fun smartSignService_processSmartIdRequest_certificateCertBundleIsNull() =
        runTest {
            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = null,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verifyNoInteractions(serviceGenerator)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.GENERAL_ERROR)
        }

    @Test
    fun smartSignService_processSmartIdRequest_createServiceCertificateException() =
        runTest {
            doThrow(CertificateException()).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_createServiceNoSuchAlgorithmException() =
        runTest {
            doThrow(NoSuchAlgorithmException()).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_UUIDIsInvalid() =
        runTest {
            request.relyingPartyUUID = "zzz"
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS)
            request.relyingPartyUUID = "00000000-0000-0000-0000-000000000000"
        }

    @Test
    fun smartSignService_processSmartIdRequest_catchSigningCancelledException() =
        runTest {
            val call = mock(Call::class.java)
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                call,
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.setCancelled(signedContainer, true)
            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verifyNoInteractions(call)
        }

    @Test
    fun smartSignService_processSmartIdRequest_success() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionStatusResponse(
                            state = SessionStatusResponseProcessState.COMPLETE,
                            result =
                                SessionStatusResponseStatus(
                                    endResult = SessionStatusResponseProcessStatus.OK,
                                    documentNumber = "documentNumber",
                                ),
                            signature =
                                SmartSignatureResponse(
                                    value = "SmartIDSignatureValue",
                                    algorithm = "SmartIDSignatureAlgorithm",
                                ),
                            cert =
                                SmartCertificateResponse(
                                    value = "cert",
                                    assuranceLevel = "assuranceLevel",
                                    certificateLevel = "certificateLevel",
                                ),
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(
                sidRestServiceClient.getCreateSignature(
                    eq("documentNumber"),
                    any(),
                ),
            ).thenReturn(
                Calls.response(Response.success(SessionResponse(sessionID = "sessionID"))),
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(challengeObserver, atLeastOnce()).onChanged("2099")
            verify(responseObserver, atLeastOnce()).onChanged(
                SmartIDServiceResponse(status = SessionStatusResponseProcessStatus.OK),
            )
        }

    @Test
    fun smartSignService_processSmartIdRequest_catchUnknownHostException() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(call)
                .whenever(call).clone()

            doThrow(UnknownHostException())
                .whenever(call).execute()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.NO_RESPONSE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_catchSSLPeerUnverifiedException() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(call)
                .whenever(call).clone()

            doThrow(SSLPeerUnverifiedException(""))
                .whenever(call).execute()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_catchIOException() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(call)
                .whenever(call).clone()

            doThrow(IOException(""))
                .whenever(call).execute()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.GENERAL_ERROR)
        }

    @Test
    fun smartSignService_processSmartIdRequest_catchCertificateException() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionStatusResponse(
                            state = SessionStatusResponseProcessState.COMPLETE,
                            result =
                                SessionStatusResponseStatus(
                                    endResult = SessionStatusResponseProcessStatus.OK,
                                    documentNumber = "documentNumber",
                                ),
                            signature =
                                SmartSignatureResponse(
                                    value = "SmartIDSignatureValue",
                                    algorithm = "SmartIDSignatureAlgorithm",
                                ),
                            cert =
                                SmartCertificateResponse(
                                    value = "cert",
                                    assuranceLevel = "assuranceLevel",
                                    certificateLevel = "certificateLevel",
                                ),
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenThrow(CertificateException(""))

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.GENERAL_ERROR)
        }

    @Test
    fun smartSignService_processSmartIdRequest_TOO_MANY_REQUESTSexception() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doThrow(IllegalStateException("Too Many Requests"))
                .whenever(call).clone()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun smartSignService_processSmartIdRequest_OCSP_INVALID_TIME_SLOTexception() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doThrow(IllegalStateException("OCSP response not in valid time slot"))
                .whenever(call).clone()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT)
        }

    @Test
    fun smartSignService_processSmartIdRequest_CERTIFICATE_REVOKEDexception() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doThrow(IllegalStateException("Certificate status: revoked"))
                .whenever(call).clone()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.CERTIFICATE_REVOKED)
        }

    @Test
    fun smartSignService_processSmartIdRequest_NO_RESPONSEexception() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doThrow(IllegalStateException("Failed to connect"))
                .whenever(call).clone()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.NO_RESPONSE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_INVALID_SSL_HANDSHAKEexception() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doThrow(IllegalStateException("Failed to create ssl connection with host"))
                .whenever(call).clone()

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseTECHNICAL_ERROR() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionStatusResponse>(400, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.TECHNICAL_ERROR)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseUNDER_MAINTENANCE() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(580, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.UNDER_MAINTENANCE)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseOLD_API() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(480, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.OLD_API)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseNOT_QUALIFIED() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(471, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.NOT_QUALIFIED)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseTOO_MANY_REQUESTS() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(429, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseEXCEEDED_UNSUCCESSFUL_REQUESTS() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(409, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(
                statusObserver,
                atLeastOnce(),
            ).onChanged(SessionStatusResponseProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS)
        }

    @Test
    fun smartSignService_processSmartIdRequest_responseACCOUNT_NOT_FOUND() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "{\"sessionID\":\"sessionID\"}"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(404, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.ACCOUNT_NOT_FOUND)
        }

    @Test
    fun smartSignService_processSmartIdRequest_response401INVALID_ACCESS_RIGHTS() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(401, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS)
        }

    @Test
    fun smartSignService_processSmartIdRequest_response403INVALID_ACCESS_RIGHTS() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<SessionResponse>(403, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.INVALID_ACCESS_RIGHTS)
        }

    @Test
    fun smartSignService_processSmartIdRequest_timeout() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionStatusResponse(
                            state = SessionStatusResponseProcessState.RUNNING,
                            result =
                                SessionStatusResponseStatus(
                                    endResult = SessionStatusResponseProcessStatus.TIMEOUT,
                                    documentNumber = "documentNumber",
                                ),
                            signature =
                                SmartSignatureResponse(
                                    value = "SmartIDSignatureValue",
                                    algorithm = "SmartIDSignatureAlgorithm",
                                ),
                            cert =
                                SmartCertificateResponse(
                                    value = "cert",
                                    assuranceLevel = "assuranceLevel",
                                    certificateLevel = "certificateLevel",
                                ),
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(
                sidRestServiceClient.getCreateSignature(
                    eq("documentNumber"),
                    any(),
                ),
            ).thenReturn(
                Calls.response(Response.success(SessionResponse(sessionID = "sessionID"))),
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.TIMEOUT)
        }

    @Test
    fun smartSignService_processSmartIdRequest_userRefused() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionStatusResponse(
                            state = SessionStatusResponseProcessState.COMPLETE,
                            result =
                                SessionStatusResponseStatus(
                                    endResult = SessionStatusResponseProcessStatus.USER_REFUSED,
                                    documentNumber = "documentNumber",
                                ),
                            signature =
                                SmartSignatureResponse(
                                    value = "SmartIDSignatureValue",
                                    algorithm = "SmartIDSignatureAlgorithm",
                                ),
                            cert =
                                SmartCertificateResponse(
                                    value = "cert",
                                    assuranceLevel = "assuranceLevel",
                                    certificateLevel = "certificateLevel",
                                ),
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(
                sidRestServiceClient.getCreateSignature(
                    eq("documentNumber"),
                    any(),
                ),
            ).thenReturn(
                Calls.response(Response.success(SessionResponse(sessionID = "sessionID"))),
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.USER_REFUSED)
        }

    @Test
    fun smartSignService_processSmartIdRequest_sessionStatusResponseIsNull() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        null,
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(
                sidRestServiceClient.getCreateSignature(
                    eq("documentNumber"),
                    any(),
                ),
            ).thenReturn(
                Calls.response(Response.success(SessionResponse(sessionID = "sessionID"))),
            )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verifyNoInteractions(containerWrapper)
        }

    @Test
    fun smartSignService_processSmartIdRequest_sessionIdIsEmpty() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.MISSING_SESSIONID)
        }

    @Test
    fun smartSignService_processSmartIdRequest_sessionResponseIsNull() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        null,
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(sidRestServiceClient, never()).getSessionStatus(any(), any())
            verifyNoInteractions(containerWrapper)
        }

    @Test
    fun smartSignService_processSmartIdRequest_base64HashIsEmpty() =
        runTest {
            doReturn(sidRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sidSignServiceUrl = request.url + "/",
                certBundle = certBundle,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionResponse(
                            sessionID = "sessionID",
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getCertificateV2(
                    semanticsIdentifier = "PNOEE-60001019906",
                    body = getSmartCertificateRequest,
                )

            doReturn(
                Calls.response(
                    Response.success(
                        SessionStatusResponse(
                            state = SessionStatusResponseProcessState.COMPLETE,
                            result =
                                SessionStatusResponseStatus(
                                    endResult = SessionStatusResponseProcessStatus.OK,
                                    documentNumber = "documentNumber",
                                ),
                            signature =
                                SmartSignatureResponse(
                                    value = "SmartIDSignatureValue",
                                    algorithm = "SmartIDSignatureAlgorithm",
                                ),
                            cert =
                                SmartCertificateResponse(
                                    value = "cert",
                                    assuranceLevel = "assuranceLevel",
                                    certificateLevel = "certificateLevel",
                                ),
                        ),
                    ),
                ),
            )
                .whenever(sidRestServiceClient).getSessionStatus("sessionID", 5000)

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("")

            smartSignServiceImpl.processSmartIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = "accessTokenPath",
                accessTokenPass = "accessTokenPass",
            )

            verify(errorStateObserver, atLeastOnce()).onChanged("Base64 (Prepare signature) is empty or null")
        }
}
