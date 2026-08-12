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

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import okhttp3.Credentials.basic
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory

class ProxyTunnelSocketFactory(
    private val manualProxy: ManualProxy?,
    private val targetHost: String,
    private val targetPort: Int,
    private val sslSocketFactory: SSLSocketFactory,
    private val connectTimeoutMillis: Int,
) : SocketFactory() {
    companion object {
        private const val LOG_TAG = "ProxyTunnelSocketFactory"
        private const val HEADER_TERMINATOR = "\r\n\r\n"
        private const val MAX_RESPONSE_LENGTH = 8192
        private const val HTTP_OK = 200
        private const val HTTP_FORBIDDEN = 403
        private const val HTTP_PROXY_AUTHENTICATION_REQUIRED = 407
    }

    override fun createSocket(): Socket =
        throw UnsupportedOperationException("Unconnected sockets cannot be tunnelled through a proxy")

    override fun createSocket(
        host: String,
        port: Int,
    ): Socket = openTunnel()

    override fun createSocket(
        host: String,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = openTunnel()

    override fun createSocket(
        address: InetAddress,
        port: Int,
    ): Socket = openTunnel()

    override fun createSocket(
        address: InetAddress,
        port: Int,
        localAddress: InetAddress,
        localPort: Int,
    ): Socket = openTunnel()

    private fun openTunnel(): Socket {
        val socket = Socket()
        try {
            val endpoint =
                if (manualProxy == null) {
                    InetSocketAddress(targetHost, targetPort)
                } else {
                    InetSocketAddress(manualProxy.host, manualProxy.port)
                }
            socket.connect(endpoint, connectTimeoutMillis)
            socket.soTimeout = connectTimeoutMillis
            if (manualProxy != null) {
                requestTunnel(socket, System.currentTimeMillis() + connectTimeoutMillis)
            }
            return sslSocketFactory.createSocket(socket, targetHost, targetPort, true)
        } catch (e: Exception) {
            closeQuietly(socket)
            throw e
        }
    }

    private fun requestTunnel(
        proxySocket: Socket,
        deadline: Long,
    ) {
        val authorization =
            if (manualProxy != null && manualProxy.username.isNotEmpty()) {
                "Proxy-Authorization: ${basic(manualProxy.username, manualProxy.password)}\r\n"
            } else {
                ""
            }
        val request =
            "CONNECT $targetHost:$targetPort HTTP/1.1\r\n" +
                "Host: $targetHost:$targetPort\r\n" +
                authorization +
                "\r\n"
        proxySocket.getOutputStream().apply {
            write(request.toByteArray(Charsets.ISO_8859_1))
            flush()
        }
        verifyTunnelEstablished(proxySocket.getInputStream(), deadline)
    }

    private fun verifyTunnelEstablished(
        input: InputStream,
        deadline: Long,
    ) {
        val response = StringBuilder()
        while (!response.endsWith(HEADER_TERMINATOR)) {
            if (System.currentTimeMillis() > deadline) {
                throw IOException("Proxy did not complete the tunnel to $targetHost in time")
            }
            val next = input.read()
            if (next == -1) {
                throw IOException("Proxy closed the connection before the tunnel was established")
            }
            response.append(next.toChar())
            if (response.length > MAX_RESPONSE_LENGTH) {
                throw IOException("Proxy sent an oversized response to the tunnel request")
            }
        }
        when (val statusCode = statusCode(response.toString())) {
            HTTP_OK -> return
            HTTP_FORBIDDEN, HTTP_PROXY_AUTHENTICATION_REQUIRED ->
                throw ProxyAuthenticationException("Proxy rejected the credentials for $targetHost")
            else ->
                throw IOException("Proxy refused a tunnel to $targetHost with status $statusCode")
        }
    }

    private fun statusCode(response: String): Int =
        response
            .substringBefore("\r\n")
            .split(' ')
            .getOrNull(1)
            ?.toIntOrNull()
            ?: throw IOException("Proxy sent an unparseable response to the tunnel request")

    private fun closeQuietly(proxySocket: Socket) {
        try {
            proxySocket.close()
        } catch (e: IOException) {
            debugLog(LOG_TAG, "Unable to close the proxy socket", e)
        }
    }
}
