@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.model.AppState
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
import ee.ria.libdigidocpp.ExternalSigner
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

    private val cert =
        "MIIGGzCCBQOgAwIBAgIQDmRuJmtGcd4j6HiqQzw0hzANBgkqhkiG9w0BAQsFADBZ\n" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypE\n" +
            "aWdpQ2VydCBHbG9iYWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjMw\n" +
            "ODMxMDAwMDAwWhcNMjQwOTMwMjM1OTU5WjBXMQswCQYDVQQGEwJFRTEQMA4GA1UE\n" +
            "BxMHVGFsbGlubjEhMB8GA1UECgwYUmlpZ2kgSW5mb3PDvHN0ZWVtaSBBbWV0MRMw\n" +
            "EQYDVQQDDAoqLmVlc3RpLmVlMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEIbJjZD5M\n" +
            "fjpd2P6FDuNclnN0hp/1ANWr05wK6/Nl/BIR/rr702rV2Y17uoBukHA4TvChN3P8\n" +
            "YMHloK+TcXmjy+CQpRQtYUvm+meobN0NWSdKGASqtX9C4E6RYQKcs2mXo4IDjTCC\n" +
            "A4kwHwYDVR0jBBgwFoAUdIWAwGbH3zfez70pN6oDHb7tzRcwHQYDVR0OBBYEFB/b\n" +
            "eFjCUl4v17Qy2g1AgqvJwOaHMB8GA1UdEQQYMBaCCiouZWVzdGkuZWWCCGVlc3Rp\n" +
            "LmVlMA4GA1UdDwEB/wQEAwIHgDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH\n" +
            "AwIwgZ8GA1UdHwSBlzCBlDBIoEagRIZCaHR0cDovL2NybDMuZGlnaWNlcnQuY29t\n" +
            "L0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3JsMEigRqBE\n" +
            "hkJodHRwOi8vY3JsNC5kaWdpY2VydC5jb20vRGlnaUNlcnRHbG9iYWxHMlRMU1JT\n" +
            "QVNIQTI1NjIwMjBDQTEtMS5jcmwwPgYDVR0gBDcwNTAzBgZngQwBAgIwKTAnBggr\n" +
            "BgEFBQcCARYbaHR0cDovL3d3dy5kaWdpY2VydC5jb20vQ1BTMIGHBggrBgEFBQcB\n" +
            "AQR7MHkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBRBggr\n" +
            "BgEFBQcwAoZFaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xv\n" +
            "YmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3J0MAkGA1UdEwQCMAAwggF+Bgor\n" +
            "BgEEAdZ5AgQCBIIBbgSCAWoBaAB2AO7N0GTV2xrOxVy3nbTNE6Iyh0Z8vOzew1FI\n" +
            "WUZxH7WbAAABikpp0YgAAAQDAEcwRQIhAOuRDRbH2F/4xj+4psS1uN7agonxJpSX\n" +
            "7l1m9CpJX/gkAiBFDEGuoEijUPdQ3M5ibV6YsXW4648t7mkR0W56XiNZYAB2AEiw\n" +
            "42vapkc0D+VqAvqdMOscUgHLVt0sgdm7v6s52IRzAAABikppz/EAAAQDAEcwRQIh\n" +
            "ALEE3j07957wr2WLsozkjmXPepYu5p/iTZx65kYtO47aAiAKS1VoZ0mMssYUcwmY\n" +
            "s5FB79zNnVW5rXD4heRSFvpT9AB2ANq2v2s/tbYin5vCu1xr6HCRcWy7UYSFNL2k\n" +
            "PTBI1/urAAABikpp0BkAAAQDAEcwRQIgOuq96euO9Aade5R6HfpNGEciZUfbgW+o\n" +
            "MmstOl3YqAUCIQDsafdu8nlmkNrN7h8uuqVXBqyv9J/u0WU80dAxPCGBiTANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAaCYTTF6Sps1YXdD6kKiYkslaxzql6D/F9Imog4pJXRZH\n" +
            "7ye5kHuGOPFfnUQEqOziOspZCusX2Bz4DK4I/oc4cQnMxQHIDdF4H/GS/2aBbU/R\n" +
            "4Ustgxkd4PCdxOn6lVux8aFDCRKrNBrUF1/970StNuh8tatyYvDEenwC0F3l2hRB\n" +
            "Q3FYZMYkR9H8FM314a/sGST6lQiKJq2hrziMWilOwKxc88MBz9H9CYrEsCMI65iH\n" +
            "vWA8njofxSYdM5NHhxTxhHKn6qZxHSjiQvF9edUYTQ4wwTczmHuqYY2qxYh6WUzR\n" +
            "yaKSeng9fe8ZVZdjOwmCa9ZdgjQYMZbDezMt+oRp2Q=="

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        AppState.isAppInForeground = true
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
    fun mobileSignService_setCancelled_success() =
        runTest {
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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                                    value = "dTzZ20E8kmoXXBAh5cV5yw==",
                                    algorithm = "MobileIDSignatureAlgorithm",
                                ),
                            cert = cert,
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

            assertEquals("dTzZ20E8kmoXXBAh5cV5yw==", mobileSignServiceImpl.response.value?.signature)

            verify(challengeObserver, atLeastOnce()).onChanged("3261")

            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.OK)

            verify(responseObserver, atLeastOnce()).onChanged(
                MobileIdServiceResponse(
                    signature = "dTzZ20E8kmoXXBAh5cV5yw==",
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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("dTzZ20E8kmoXXBAh5cV5yw==".toByteArray())

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
                            cert = cert,
                        ),
                    ),
                ),
            )
                .whenever(midRestServiceClient).getCertificate(
                    body = getMobileCertificateRequest,
                )

            whenever(
                containerWrapper.prepareSignature(
                    signer = any<ExternalSigner>(),
                    signedContainer = any<SignedContainer>(),
                    cert = any<ByteArray>(),
                    roleData = isNull(),
                ),
            ).thenReturn("".toByteArray())

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
