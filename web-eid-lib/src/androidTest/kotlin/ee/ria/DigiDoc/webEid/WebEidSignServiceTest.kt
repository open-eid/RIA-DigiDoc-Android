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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class WebEidSignServiceTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: WebEidSignService

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
        service = WebEidSignServiceImpl()
    }

    @Test
    fun buildCertificatePayload_withValidCert_returnsExpectedJson() {
        val signingCertBytes = Base64.getMimeDecoder().decode(signingCertBase64)
        val result = service.buildCertificatePayload(signingCertBytes)

        assertTrue(result.has("certificate"))
        assertTrue(result.has("supportedSignatureAlgorithms"))
        assertEquals(
            Base64.getEncoder().encodeToString(signingCertBytes),
            result.getString("certificate"),
        )

        val algorithms = result.getJSONArray("supportedSignatureAlgorithms")
        assertTrue(algorithms.length() > 0)
        val firstAlgo = algorithms.getJSONObject(0)
        assertEquals("ECC", firstAlgo.getString("cryptoAlgorithm"))
        assertEquals("NONE", firstAlgo.getString("paddingScheme"))
    }

    @Test
    fun buildSignPayload_withValidInputs_returnsExpectedJson() {
        val signatureBytes = byteArrayOf(11, 22, 33, 44, 55)
        val hashFunction = "SHA-384"
        val result = service.buildSignPayload(signingCertBase64, signatureBytes, hashFunction)

        assertEquals(
            setOf("signature", "signatureAlgorithm"),
            result.keys().asSequence().toSet(),
        )

        val expectedSignature = Base64.getEncoder().encodeToString(signatureBytes)
        assertEquals(expectedSignature, result.getString("signature"))

        val signatureAlgorithm = result.getJSONObject("signatureAlgorithm")

        assertEquals("ECC", signatureAlgorithm.getString("cryptoAlgorithm"))
        assertEquals("NONE", signatureAlgorithm.getString("paddingScheme"))
        assertTrue(signatureAlgorithm.getString("hashFunction").startsWith("SHA-"))
    }

    @Test
    fun buildSignPayload_differentSignatures_produceDifferentJson() {
        val sig1 = byteArrayOf(1, 2, 3)
        val sig2 = byteArrayOf(4, 5, 6)
        val hashFunction = "SHA-384"
        val result1 = service.buildSignPayload(signingCertBase64, sig1, hashFunction)
        val result2 = service.buildSignPayload(signingCertBase64, sig2, hashFunction)

        assertNotEquals(
            result1.getString("signature"),
            result2.getString("signature"),
        )
    }

    @Test
    fun buildCertificatePayload_invalidCert_throwsException() {
        val invalidBytes = "not-a-real-cert".toByteArray()
        val exception =
            assertThrows(Exception::class.java) {
                service.buildCertificatePayload(invalidBytes)
            }
        assertTrue(exception.message!!.contains("certificate") || exception.message!!.contains("Certificate"))
    }
}
