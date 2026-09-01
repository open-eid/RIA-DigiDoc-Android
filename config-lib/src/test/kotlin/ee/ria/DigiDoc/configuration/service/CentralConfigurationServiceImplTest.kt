/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.service

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

@ExperimentalCoroutinesApi
class CentralConfigurationServiceImplTest {
    private lateinit var service: CentralConfigurationServiceImpl
    private lateinit var mockWebServer: MockWebServer
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var secureClient: OkHttpClient

    private lateinit var expectedBaseUrl: String

    private lateinit var context: Context
    private lateinit var property: ConfigurationProperty

    private lateinit var packageInfoFlags: MockedStatic<PackageManager.PackageInfoFlags>

    @Before
    fun setup() {
        val packageManager = mock(PackageManager::class.java)
        context = mock(Context::class.java)
        `when`(context.packageManager).thenReturn(packageManager)
        `when`(context.packageName).thenReturn("test")
        val packageInfo = mock(PackageInfo::class.java)
        `when`(packageInfo.longVersionCode).thenReturn(0L)
        packageInfoFlags = mockStatic(PackageManager.PackageInfoFlags::class.java)
        packageInfoFlags
            .`when`<PackageManager.PackageInfoFlags> { PackageManager.PackageInfoFlags.of(anyLong()) }
            .thenReturn(mock(PackageManager.PackageInfoFlags::class.java))
        `when`(packageManager.getPackageInfo(anyString(), any<PackageManager.PackageInfoFlags>()))
            .thenReturn(packageInfo)

        Dispatchers.setMain(testDispatcher)

        val localhostCert =
            HeldCertificate
                .Builder()
                .commonName("localhost")
                .addSubjectAlternativeName("localhost")
                .build()

        val serverCertificates =
            HandshakeCertificates
                .Builder()
                .heldCertificate(localhostCert)
                .build()

        val clientCertificates =
            HandshakeCertificates
                .Builder()
                .addTrustedCertificate(localhostCert.certificate)
                .build()

        secureClient =
            OkHttpClient
                .Builder()
                .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
                .hostnameVerifier { _, _ -> true }
                .build()

        mockWebServer = MockWebServer()
        mockWebServer.useHttps(serverCertificates.sslSocketFactory(), false)
        mockWebServer.start(0)

        expectedBaseUrl = mockWebServer.url("/").toString().removeSuffix("/")

        property =
            ConfigurationProperty(
                centralConfigurationServiceUrl = expectedBaseUrl,
            )

        service =
            object : CentralConfigurationServiceImpl(context, property) {
                override fun constructHttpClient(
                    defaultTimeout: Long,
                    proxySetting: ProxySetting?,
                    manualProxySettings: ManualProxy,
                ): OkHttpClient = secureClient
            }
    }

    @After
    fun tearDown() {
        packageInfoFlags.close()
        Dispatchers.resetMain()
        mockWebServer.shutdown()
    }

    @Test
    fun centralConfigurationServiceImpl_fetchConfiguration_returnsResponse() =
        runTest {
            val expected = "config-value"
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(expected))

            val result = service.fetchConfiguration()

            assertEquals(expected, result)
        }

    @Test
    fun centralConfigurationServiceImpl_fetchSignature_returnsResponse() =
        runTest {
            val expected = "signature-value"
            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(expected))

            val result = service.fetchSignature()

            assertEquals(expected, result)
        }

    @Test
    fun centralConfigurationServiceImpl_setupProxy_setsProxyConfig() =
        runTest {
            val proxy = ProxySetting.MANUAL_PROXY
            val manualProxy = ManualProxy("127.0.0.1", 8080, "user", "pass")

            service.setupProxy(proxy, manualProxy)
        }

    @Test
    fun centralConfigurationServiceImpl_constructHttpClient_createsClientWithProxy() {
        val service = CentralConfigurationServiceImpl(context, property)
        val proxySetting = ProxySetting.MANUAL_PROXY
        val manualProxy = ManualProxy("localhost", 8888, "user", "pass")

        val client = service.constructHttpClient(5L, proxySetting, manualProxy)

        assertNotNull(client)
        assertEquals(5L, client.connectTimeoutMillis.toLong() / 1000)
    }

    @Test
    fun centralConfigurationServiceImpl_constructRetrofit_createsValidRetrofit() {
        val client = OkHttpClient.Builder().build()
        val retrofit = service.constructRetrofit("http://localhost:8080", client)

        assertNotNull(retrofit.create(CentralConfigurationRepository::class.java))
    }
}
