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

package ee.ria.DigiDoc.network.proxy

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import javax.net.ssl.SSLSocketFactory

class ProxyTunnelSocketFactoryTest {
    @get:Rule
    val proxyServer = MockWebServer()

    private val targetHost = "ldap.example.com"
    private val targetPort = 636

    @Test
    fun proxyTunnelSocketFactory_createSocket_throwsProxyAuthenticationExceptionWhenProxyDemandsCredentials() {
        proxyServer.enqueue(MockResponse().setResponseCode(407))

        assertThrows(ProxyAuthenticationException::class.java) {
            socketFactory("proxyUser", "proxyPass").createSocket(targetHost, targetPort)
        }
    }

    @Test
    fun proxyTunnelSocketFactory_createSocket_throwsProxyAuthenticationExceptionWhenProxyForbidsConnect() {
        proxyServer.enqueue(MockResponse().setResponseCode(403))

        assertThrows(ProxyAuthenticationException::class.java) {
            socketFactory("proxyUser", "proxyPass").createSocket(targetHost, targetPort)
        }
    }

    @Test
    fun proxyTunnelSocketFactory_createSocket_throwsPlainIOExceptionWhenProxyRefusesTunnel() {
        proxyServer.enqueue(MockResponse().setResponseCode(502))

        val exception =
            assertThrows(IOException::class.java) {
                socketFactory("proxyUser", "proxyPass").createSocket(targetHost, targetPort)
            }

        assertFalse(exception is ProxyAuthenticationException)
    }

    @Test
    fun proxyTunnelSocketFactory_createSocket_sendsConnectWithProxyAuthorization() {
        proxyServer.enqueue(MockResponse().setResponseCode(407))

        assertThrows(ProxyAuthenticationException::class.java) {
            socketFactory("proxyUser", "proxyPass").createSocket(targetHost, targetPort)
        }

        val request = proxyServer.takeRequest()
        assertEquals("CONNECT $targetHost:$targetPort HTTP/1.1", request.requestLine)
        assertEquals("Basic cHJveHlVc2VyOnByb3h5UGFzcw==", request.getHeader("Proxy-Authorization"))
    }

    @Test
    fun proxyTunnelSocketFactory_createSocket_omitsProxyAuthorizationWithoutCredentials() {
        proxyServer.enqueue(MockResponse().setResponseCode(407))

        assertThrows(ProxyAuthenticationException::class.java) {
            socketFactory("", "").createSocket(targetHost, targetPort)
        }

        assertNull(proxyServer.takeRequest().getHeader("Proxy-Authorization"))
    }

    @Test
    fun proxyTunnelSocketFactory_createSocket_throwsWhenUnconnectedSocketRequested() {
        assertThrows(UnsupportedOperationException::class.java) {
            socketFactory("proxyUser", "proxyPass").createSocket()
        }
    }

    private fun socketFactory(
        username: String,
        password: String,
    ) = ProxyTunnelSocketFactory(
        ManualProxy("127.0.0.1", proxyServer.port, username, password),
        targetHost,
        targetPort,
        SSLSocketFactory.getDefault() as SSLSocketFactory,
        5000,
    )
}
