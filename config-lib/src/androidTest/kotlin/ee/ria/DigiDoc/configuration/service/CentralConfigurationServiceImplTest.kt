@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.service

import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class CentralConfigurationServiceImplTest {
    private lateinit var service: CentralConfigurationServiceImpl
    private lateinit var mockWebServer: MockWebServer
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var secureClient: OkHttpClient

    private lateinit var expectedBaseUrl: String

    private lateinit var property: ConfigurationProperty

    private val userAgent = "test-agent"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create a self-signed certificate for localhost
        val localhostCert =
            HeldCertificate.Builder()
                .commonName("localhost")
                .addSubjectAlternativeName("localhost")
                .build()

        val serverCertificates =
            HandshakeCertificates.Builder()
                .heldCertificate(localhostCert)
                .build()

        val clientCertificates =
            HandshakeCertificates.Builder()
                .addTrustedCertificate(localhostCert.certificate)
                .build()

        secureClient =
            OkHttpClient.Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .hostnameVerifier { _, _ -> true } // Accept "localhost" for testing
                .build()

        mockWebServer = MockWebServer()
        mockWebServer.useHttps(serverCertificates.sslSocketFactory(), false)
        mockWebServer.start(0) // Automatically chooses a free port

        expectedBaseUrl = mockWebServer.url("/").toString().removeSuffix("/")

        property =
            ConfigurationProperty(
                centralConfigurationServiceUrl = expectedBaseUrl,
            )

        service =
            object : CentralConfigurationServiceImpl(userAgent, property) {
                override fun constructHttpClient(
                    defaultTimeout: Long,
                    proxySetting: ProxySetting?,
                    manualProxySettings: ManualProxy,
                ): OkHttpClient = secureClient
            }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockWebServer.shutdown()
    }

    @Test
    fun `fetchConfiguration returns response`() =
        runTest {
            val expected = "config-value"
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(expected))

            val result = service.fetchConfiguration()

            assertEquals(expected, result)
        }

    @Test
    fun `fetchPublicKey returns response`() =
        runTest {
            val expected = "public-key-value"
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(expected))

            val result = service.fetchPublicKey()

            assertEquals(expected, result)
        }

    @Test
    fun `fetchSignature returns response`() =
        runTest {
            val expected = "signature-value"
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(expected))

            val result = service.fetchSignature()

            assertEquals(expected, result)
        }

    @Test
    fun `setupProxy sets internal proxy config`() =
        runTest {
            val proxy = ProxySetting.MANUAL_PROXY
            val manualProxy = ManualProxy("127.0.0.1", 8080, "user", "pass")

            service.setupProxy(proxy, manualProxy)
        }

    @Test
    fun `constructHttpClient creates client with proxy`() {
        val service = CentralConfigurationServiceImpl(userAgent, property)
        val proxySetting = ProxySetting.MANUAL_PROXY
        val manualProxy = ManualProxy("localhost", 8888, "user", "pass")

        val client = service.constructHttpClient(5L, proxySetting, manualProxy)

        assertNotNull(client)
        assertEquals(5L, client.connectTimeoutMillis.toLong() / 1000)
    }

    @Test
    fun `constructRetrofit creates valid Retrofit instance`() {
        val client = OkHttpClient.Builder().build()
        val retrofit = service.constructRetrofit("http://localhost:8080", client)

        assertNotNull(retrofit.create(CentralConfigurationRepository::class.java))
    }
}
