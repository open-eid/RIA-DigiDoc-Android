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

package ee.ria.DigiDoc.webEid

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidCertificateRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode
import ee.ria.DigiDoc.webEid.exception.WebEidException
import ee.ria.DigiDoc.webEid.utils.WebEidRequestParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.security.MessageDigest
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidRequestParserTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    private val signingCertBase64Raw =
        """
        MIID8zCCA3mgAwIBAgIUeHSVTuHxrs0ASYMbqOjDX5yFVnswCgYIKoZIzj0EAwMwXDEYMBYGA1UEAwwPVGVzdCBFU1RFSUQyMDI1MRcwFQYDVQRh
        DA5OVFJFRS0xNzA2NjA0OTEaMBgGA1UECgwRWmV0ZXMgRXN0b25pYSBPw5wxCzAJBgNVBAYTAkVFMB4XDTI0MTIxODEwMjY0MVoXDTI5MTIwOTIw
        NTk0MVowfzEqMCgGA1UEAwwhSsOVRU9SRyxKQUFLLUtSSVNUSkFOLDM4MDAxMDg1NzE4MRowGAYDVQQFExFQTk9FRS0zODAwMTA4NTcxODEWMBQG
        A1UEKgwNSkFBSy1LUklTVEpBTjEQMA4GA1UEBAwHSsOVRU9SRzELMAkGA1UEBhMCRUUwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAR9DpcXt4J2NwqG
        B3pS1RcGlBM7tcoG82OGpLwCr4xn9LZgc5QRk/oGmRoJ6Nk9/BbHgoYYvBXW8xzcTNZwKIxwz7FRI9cFF+4+4i/ywqkRV9ApH112xQ7L+p9ANCP/
        va6jggHXMIIB0zAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFO7ylT+MsvxRnoTm5l6EEX5CuiA2MHAGCCsGAQUFBwEBBGQwYjA4BggrBgEFBQcwAoYs
        aHR0cDovL2NydC10ZXN0LmVpZHBraS5lZS90ZXN0RVNURUlEMjAyNS5jcnQwJgYIKwYBBQUHMAGGGmh0dHA6Ly9vY3NwLXRlc3QuZWlkcGtpLmVl
        MFcGA1UdIARQME4wCQYHBACL7EABAjBBBg6INwEDBgEEAYORIQIBATAvMC0GCCsGAQUFBwIBFiFodHRwczovL3JlcG9zaXRvcnktdGVzdC5laWRw
        a2kuZWUwbAYIKwYBBQUHAQMEYDBeMAgGBgQAjkYBATAIBgYEAI5GAQQwEwYGBACORgEGMAkGBwQAjkYBBgEwMwYGBACORgEFMCkwJxYhaHR0cHM6
        Ly9yZXBvc2l0b3J5LXRlc3QuZWlkcGtpLmVlEwJlbjA9BgNVHR8ENjA0MDKgMKAuhixodHRwOi8vY3JsLXRlc3QuZWlkcGtpLmVlL3Rlc3RFU1RF
        SUQyMDI1LmNybDAdBgNVHQ4EFgQUH6IlbFh9H8w0BIsDCgq01rqaFVUwDgYDVR0PAQH/BAQDAgZAMAoGCCqGSM49BAMDA2gAMGUCMQDGeR+QV6MF
        sWnB7LoXrpOfPQFTT366CLbdmQQMbIzJtysZTrOSQ95yxpulvpxOKsoCMAsT41AJ3de5JSrW89S5x5zgvi1K7PG1zhzSGgUuMElzDZPJSyp4TE8k
        FvCDizwjaQ==
        """.trimIndent()

    private val signingCertBase64 = signingCertBase64Raw.replace("\\s+".toRegex(), "")

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun parseAuthUri_validUri_success() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val uri = Uri.parse(createAuthUri("test-challenge-00000000000000000000000000000", loginUri, true))
        val result: WebEidAuthRequest = WebEidRequestParser.parseAuthUri(uri)

        assertEquals("test-challenge-00000000000000000000000000000", result.challenge)
        assertEquals(loginUri, result.loginUri)
        assertEquals(true, result.getSigningCertificate)
        assertTrue(result.origin.startsWith("https://rp.example.com"))
    }

    @Test
    fun parseAuthUri_missingScheme_throwsException() {
        val loginUri = "rp.example.com/auth/eid/login"
        val uri = Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }

        assertEquals("Invalid response URI scheme", exception.message)
    }

    @Test
    fun parseAuthUri_invalidScheme_throwsException() {
        val loginUri = "http://rp.example.com/auth/eid/login"
        val uri = Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }
        assertEquals("Response URI must use HTTPS scheme", exception.message)
    }

    @Test
    fun parseAuthUri_emptyHost_throwsException() {
        val loginUri = "https:///auth/eid/login"
        val uri = Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }

        assertEquals("Invalid response URI host", exception.message)
    }

    @Test
    fun parseAuthUri_forbiddenUserInfo_throwsException() {
        val loginUri = "https://rp.example.com:pass@evil.example.com/auth/eid/login"
        val uri = Uri.parse(createAuthUri("abc1235", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }
        assertTrue(exception.message!!.contains("Response URI must not contain userinfo"))
    }

    @Test
    fun parseAuthUri_invalidResponseUri_throwsException() {
        val loginUri = "://rp.example.com/auth/eid/login"
        val uri = Uri.parse(createAuthUri("abc1234", loginUri, false))

        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }

        assertTrue(exception.message!!.contains("Invalid response URI"))
    }

    @Test
    fun parseAuthUri_invalidChallengeLength_throwsWebEidException() {
        val loginUri = "https://rp.example.com/auth/eid/login"
        val json =
            """
            {
              "challenge": "abc123",
              "loginUri": "$loginUri",
              "getSigningCertificate": false
            }
            """.trimIndent()

        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        val uri = Uri.parse("web-eid://auth#$encoded")

        val exception =
            assertThrows(WebEidException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }

        assertEquals(WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST, exception.errorCode)
        assertTrue(exception.message.contains("Invalid challenge length"))
        assertEquals(loginUri, exception.responseUri)
    }

    private fun createAuthUri(
        challenge: String,
        loginUri: String,
        getCert: Boolean,
    ): String {
        val json =
            """
            {
              "challenge": "$challenge",
              "loginUri": "$loginUri",
              "getSigningCertificate": $getCert
            }
            """.trimIndent()
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        return "web-eid://auth#$encoded"
    }

    @Test
    fun parseAuthUri_invalidBase64_throwsException() {
        val uri = Uri.parse("web-eid://auth#%%%INVALID%%%")
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }
        assertTrue(exception.message!!.contains("Invalid URI fragment"))
    }

    @Test
    fun parseAuthUri_originTooLong_throwsWebEidException() {
        val longHost = "a".repeat(260)
        val loginUri = "https://$longHost.com/auth/eid/login"

        val json =
            """
            {
              "challenge": "${"b".repeat(60)}",
              "loginUri": "$loginUri",
              "getSigningCertificate": false
            }
            """.trimIndent()

        val encoded = Base64.getEncoder().encodeToString(json.toByteArray())
        val uri = Uri.parse("web-eid://auth#$encoded")

        val exception =
            assertThrows(WebEidException::class.java) {
                WebEidRequestParser.parseAuthUri(uri)
            }

        assertEquals(WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST, exception.errorCode)
        assertTrue(exception.message.contains("Invalid origin length"))
    }

    @Test
    fun parseSignUri_valid_withHashAndFunction_success() {
        val responseUri = "https://rp.example.com/sign/response"
        val hash = validSha384Base64()
        val uri = Uri.parse(createSignUri(hash, "SHA-384", signingCertBase64))
        val result: WebEidSignRequest = WebEidRequestParser.parseSignUri(uri)

        assertEquals(responseUri, result.responseUri)
        assertEquals(hash, result.hash)
        assertEquals("SHA-384", result.hashFunction)
        assertNotNull(result.signingCertificate)
    }

    @Test
    fun parseCertificateUri_valid_success() {
        val responseUri = "https://rp.example.com/sign/response"
        val uri = Uri.parse(createSignUri(null, null))
        val result: WebEidCertificateRequest = WebEidRequestParser.parseCertificateUri(uri)

        assertEquals(responseUri, result.responseUri)
        assertNotNull(result.origin)
    }

    @Test
    fun parseSignUri_invalidBase64_throwsException() {
        val uri = Uri.parse("web-eid://sign#%%%INVALID%%%")
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                WebEidRequestParser.parseSignUri(uri)
            }
        assertTrue(exception.message!!.contains("Invalid URI fragment"))
    }

    private fun createSignUri(
        hash: String?,
        hashFunction: String?,
        signingCertificate: String? = null,
    ): String {
        val responseUri = "https://rp.example.com/sign/response"
        val sb = StringBuilder()
        sb.append("{\"responseUri\":\"$responseUri\"")
        if (hash != null) sb.append(",\"hash\":\"$hash\"")
        if (hashFunction != null) sb.append(",\"hashFunction\":\"$hashFunction\"")
        if (signingCertificate != null) {
            sb.append(",\"signingCertificate\":\"$signingCertificate\"")
        }
        sb.append("}")
        val encoded = Base64.getEncoder().encodeToString(sb.toString().toByteArray())
        return "web-eid://sign#$encoded"
    }

    private fun validSha384Base64(): String {
        val digest = MessageDigest.getInstance("SHA-384")
        val hash = digest.digest("test-data".toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }
}
