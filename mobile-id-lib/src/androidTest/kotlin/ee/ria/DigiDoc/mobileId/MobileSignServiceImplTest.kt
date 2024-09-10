@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.MobileIdServiceResponse
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.network.mid.dto.request.PostMobileCreateSignatureCertificateRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureCertificateResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessState
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionStatusResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileSignatureResponse
import ee.ria.DigiDoc.network.mid.rest.MIDRestServiceClient
import ee.ria.DigiDoc.network.mid.rest.ServiceGenerator
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utilsLib.signing.TrustManagerUtil
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
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
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import retrofit2.Call
import retrofit2.Response
import retrofit2.mock.Calls
import java.net.UnknownHostException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import javax.net.ssl.SSLPeerUnverifiedException

@RunWith(MockitoJUnitRunner::class)
class MobileSignServiceImplTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var serviceGenerator: ServiceGenerator
    private lateinit var containerWrapper: ContainerWrapper
    private lateinit var mobileSignServiceImpl: MobileSignServiceImpl

    @Mock
    lateinit var responseObserver: Observer<MobileIdServiceResponse?>

    @Mock
    lateinit var errorStateObserver: Observer<String?>

    @Mock
    lateinit var challengeObserver: Observer<String?>

    @Mock
    lateinit var statusObserver: Observer<MobileCreateSignatureProcessStatus?>

    @Mock
    lateinit var resultObserver: Observer<MobileCertificateResultType?>

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
    private val url = "https://tsp.demo.sk.ee/mid-api"
    private val trustManagers = TrustManagerUtil.trustManagers
    private val proxySetting = ProxySetting.NO_PROXY
    private val manualProxy = ManualProxy("", 80, "", "")
    private val certBundle = ArrayList(listOf("CERT000011112222", "CERT000011112223"))
    private val request =
        MobileCreateSignatureRequest(
            phoneNumber = "37200000766",
            nationalIdentityNumber = "60001019906",
            url = url,
            relyingPartyUUID = "00000000-0000-0000-0000-000000000000",
            relyingPartyName = "DEMO",
            containerPath = "containerPath",
            hashType = "SHA256",
            language = "EST",
            displayText = "displayText",
            displayTextFormat = "GSM7",
        )

    private val getMobileCertificateRequest =
        PostMobileCreateSignatureCertificateRequest(
            relyingPartyName = "DEMO",
            relyingPartyUUID = "00000000-0000-0000-0000-000000000000",
            phoneNumber = "37200000766",
            nationalIdentityNumber = "60001019906",
        )

    private val midRestServiceClient = mock(MIDRestServiceClient::class.java)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        serviceGenerator = mock(ServiceGenerator::class.java)
        containerWrapper = mock(ContainerWrapper::class.java)
        mobileSignServiceImpl =
            MobileSignServiceImpl(
                containerWrapper = containerWrapper,
                serviceGenerator = serviceGenerator,
            )
        mobileSignServiceImpl.response.observeForever(responseObserver)
        mobileSignServiceImpl.errorState.observeForever(errorStateObserver)
        mobileSignServiceImpl.challenge.observeForever(challengeObserver)
        mobileSignServiceImpl.status.observeForever(statusObserver)
        mobileSignServiceImpl.result.observeForever(resultObserver)
        mobileSignServiceImpl.cancelled.observeForever(cancelledObserver)

        val container =
            AssetFile.getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )
        runBlocking {
            signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)
        }
    }

    @Test
    fun mobileSignService_setCancelled_success() {
        mobileSignServiceImpl.setCancelled(signedContainer, true)
        verify(cancelledObserver, atLeastOnce()).onChanged(true)
    }

    @Test
    fun mobileSignService_resetValues_success() {
        mobileSignServiceImpl.resetValues()
        verify(responseObserver, atLeastOnce()).onChanged(null)
        verify(errorStateObserver, atLeastOnce()).onChanged(null)
        verify(challengeObserver, atLeastOnce()).onChanged(null)
        verify(statusObserver, atLeastOnce()).onChanged(null)
        verify(resultObserver, atLeastOnce()).onChanged(null)
        verify(cancelledObserver, atLeastOnce()).onChanged(false)
    }

    @Test
    fun mobileSignService_processMobileIdRequest_certificateCertBundleIsNull() =
        runTest {
            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(errorStateObserver, atLeastOnce()).onChanged("Certificate cert bundle is null")
        }

    @Test
    fun mobileSignService_processMobileIdRequest_createServiceThrowsCertificateException() =
        runTest {
            doThrow(CertificateException()).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_createServiceThrowsNoSuchAlgorithmException() =
        runTest {
            doThrow(NoSuchAlgorithmException()).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_countryCodeIsInvalid() =
        runTest {
            request.phoneNumber = null

            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_COUNTRY_CODE)

            request.phoneNumber = "37200000766"
        }

    @Test
    fun mobileSignService_processMobileIdRequest_relyingPartyUUIDIsInvalid() =
        runTest {
            request.relyingPartyUUID = "zzz"

            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS)

            request.relyingPartyUUID = "00000000-0000-0000-0000-000000000000"
        }

    @Test
    fun mobileSignService_processMobileIdRequest_success() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureSessionStatusResponse(
                            state = MobileCreateSignatureProcessState.COMPLETE,
                            result = MobileCreateSignatureProcessStatus.OK,
                            signature =
                                MobileSignatureResponse(
                                    value = "MobileIDSignatureValue",
                                    algorithm = "MobileIDSignatureAlgorithm",
                                ),
                            cert = "cert",
                            time = "time",
                            traceId = "traceId",
                            error = null,
                        ),
                    ),
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            assertEquals(mobileSignServiceImpl.response.value?.signature, "MobileIDSignatureValue")

            verify(challengeObserver, atLeastOnce()).onChanged("3787")

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.OK)

            verify(responseObserver, atLeastOnce()).onChanged(
                MobileIdServiceResponse(
                    signature = "MobileIDSignatureValue",
                    container = signedContainer.rawContainer(),
                    status = MobileCreateSignatureProcessStatus.OK,
                ),
            )
        }

    @Test
    fun mobileSignService_processMobileIdRequest_timeout() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureSessionStatusResponse(
                            state = MobileCreateSignatureProcessState.RUNNING,
                            result = MobileCreateSignatureProcessStatus.TIMEOUT,
                            signature =
                                MobileSignatureResponse(
                                    value = "MobileIDSignatureValue",
                                    algorithm = "MobileIDSignatureAlgorithm",
                                ),
                            cert = "cert",
                            time = "time",
                            traceId = "traceId",
                            error = null,
                        ),
                    ),
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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
            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TIMEOUT)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateResponseEmptyBody() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
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
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(errorStateObserver, atLeastOnce()).onChanged("Mobile-ID signature certificate response is null")
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchUnknownHostException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )
            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(UnknownHostException())
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.NO_RESPONSE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchSSLPeerUnverifiedException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )
            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(SSLPeerUnverifiedException(""))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchCertificateException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenThrow(CertificateException("test error message"))

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.GENERAL_ERROR)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchSigningCancelledException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            mobileSignServiceImpl.setCancelled(signedContainer, true)
            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verifyNoInteractions(midRestServiceClient)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchTooManyRequestsException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException("Too Many Requests"))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchOCSPInvalidTimeSlotException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException("OCSP response not in valid time slot"))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.OCSP_INVALID_TIME_SLOT)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchCertificateRevokedException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException("Certificate status: revoked"))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.CERTIFICATE_REVOKED)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchNoResponseException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException("Failed to connect"))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.NO_RESPONSE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchSSLErrorException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException("Failed to create ssl connection with host"))
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_catchTechnicalErrorException() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val call = mock(Call::class.java)
            doReturn(call)
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            doThrow(IllegalStateException())
                .whenever(call).execute()

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateResponseError() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.NOT_FOUND,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
                context = context,
                signedContainer = signedContainer,
                request = request,
                roleDataRequest = null,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
                certificateBundle = certBundle,
                accessTokenPath = null,
                accessTokenPass = null,
            )

            verify(resultObserver, atLeastOnce()).onChanged(MobileCertificateResultType.NOT_FOUND)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSignatureSessionStatusResponseError() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureSessionStatusResponse(
                            state = MobileCreateSignatureProcessState.COMPLETE,
                            result = MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS,
                            signature =
                                MobileSignatureResponse(
                                    value = "MobileIDSignatureValue",
                                    algorithm = "MobileIDSignatureAlgorithm",
                                ),
                            cert = "cert",
                            time = "time",
                            traceId = "traceId",
                            error = null,
                        ),
                    ),
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSignatureSessionStatusReturnTECHNICAL_ERROR() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionStatusResponse>(400, errorResponseBody)

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    mockResponse,
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR)
        }

    @Test
    fun getMobileCreateSignatureSessionStatusReturnEXCEEDED_UNSUCCESSFUL_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionStatusResponse>(409, errorResponseBody)

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    mockResponse,
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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
            ).onChanged(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSignatureSessionStatusReturnINVALID_ACCESS_RIGHTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionStatusResponse>(401, errorResponseBody)

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    mockResponse,
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSignatureSessionStatusReturnTOO_MANY_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        Response.success(
                            MobileCreateSignatureSessionResponse(
                                sessionID = "sessionID",
                                traceId = "traceId",
                                time = "time",
                                error = null,
                            ),
                        ),
                    ),
                )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionStatusResponse>(429, errorResponseBody)

            whenever(
                midRestServiceClient.getMobileCreateSignatureSessionStatus(
                    eq("sessionID"),
                    eq("1000"),
                ),
            ).thenReturn(
                Calls.response(
                    mockResponse,
                ),
            )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateReturnsTECHNICAL_ERROR() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureCertificateResponse>(400, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateReturnsEXCEEDED_UNSUCCESSFUL_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureCertificateResponse>(409, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
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
            ).onChanged(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateReturnsINVALID_ACCESS_RIGHTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureCertificateResponse>(401, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getCertificateReturnsTOO_MANY_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureCertificateResponse>(429, errorResponseBody)

            doReturn(
                Calls.response(
                    mockResponse,
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSessionReturnTECHNICAL_ERROR() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionResponse>(400, errorResponseBody)

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        mockResponse,
                    ),
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TECHNICAL_ERROR)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSessionReturnEXCEEDED_UNSUCCESSFUL_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionResponse>(409, errorResponseBody)

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        mockResponse,
                    ),
                )

            mobileSignServiceImpl.processMobileIdRequest(
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
            ).onChanged(MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSessionReturnINVALID_ACCESS_RIGHTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionResponse>(401, errorResponseBody)

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        mockResponse,
                    ),
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_getMobileCreateSessionReturnTOO_MANY_REQUESTS() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==")

            val errorResponse = "errorResponse"
            val errorResponseBody = errorResponse.toResponseBody("application/json".toMediaTypeOrNull())
            val mockResponse = Response.error<MobileCreateSignatureSessionResponse>(429, errorResponseBody)

            whenever(midRestServiceClient.getMobileCreateSession(any()))
                .thenReturn(
                    Calls.response(
                        mockResponse,
                    ),
                )

            mobileSignServiceImpl.processMobileIdRequest(
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

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS)
        }

    @Test
    fun mobileSignService_processMobileIdRequest_Base64SignatureIsEmpty() =
        runTest {
            doReturn(midRestServiceClient).whenever(serviceGenerator).createService(
                context = context,
                sslContext = null,
                midSignServiceUrl = request.url,
                certBundle = certBundle,
                trustManagers = trustManagers,
                proxySetting = proxySetting,
                manualProxySettings = manualProxy,
            )

            doReturn(
                Calls.response(
                    Response.success(
                        MobileCreateSignatureCertificateResponse(
                            result = MobileCertificateResultType.OK,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signedContainer = any<SignedContainer>(),
                    cert = any(),
                    roleData = isNull(),
                ),
            ).thenReturn("")

            mobileSignServiceImpl.processMobileIdRequest(
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
