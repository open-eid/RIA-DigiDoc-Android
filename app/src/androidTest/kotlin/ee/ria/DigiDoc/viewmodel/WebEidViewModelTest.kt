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

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import android.util.Base64.URL_SAFE
import android.util.Base64.decode
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.WebEidSignService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Base64

@RunWith(MockitoJUnitRunner::class)
class WebEidViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authService: WebEidAuthService

    @Mock
    private lateinit var signService: WebEidSignService

    private lateinit var viewModel: WebEidViewModel

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
        MockitoAnnotations.openMocks(this)
        viewModel = WebEidViewModel(authService, signService)
    }

    @Test
    fun webEidViewModel_handleAuth_parsesAuthUriAndSetsStateFlow() {
        runTest {
            val uri =
                Uri.parse(
                    "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsImxvZ2luX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vcmVzcG9uc2UiLCJnZXRfc2lnbmluZ19jZXJ0aWZpY2F0ZSI6dHJ1ZX0",
                )
            viewModel.handleAuth(uri)
            val authRequest = viewModel.authRequest.value
            val signRequest = viewModel.signRequest.value
            assert(authRequest != null)
            assert(signRequest == null)
            assertEquals("test-challenge-00000000000000000000000000000", authRequest?.challenge)
            assertEquals("https://example.com/response", authRequest?.loginUri)
            assertEquals("https://example.com", authRequest?.origin)
            assertEquals(true, authRequest?.getSigningCertificate)
        }
    }

    @Test
    fun webEidViewModel_handleAuth_emitErrorResponseEventWhenChallengeMinLength() {
        val uri =
            Uri.parse(
                "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwIiwibG9naW5fdXJpIjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9yZXNwb25zZSIsImdldF9zaWduaW5nX2NlcnRpZmljYXRlIjp0cnVlfQ",
            )
        webEidViewModel_handleAuth_emitErrorResponseEventWhenInvalidChallenge(uri)
    }

    @Test
    fun webEidViewModel_handleAuth_emitErrorResponseEventWhenChallengeMaxLength() {
        val uri =
            Uri.parse(
                "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAiLCJsb2dpbl91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIiwiZ2V0X3NpZ25pbmdfY2VydGlmaWNhdGUiOnRydWV9",
            )
        webEidViewModel_handleAuth_emitErrorResponseEventWhenInvalidChallenge(uri)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun webEidViewModel_handleAuth_emitErrorResponseEventWhenInvalidChallenge(uri: Uri) {
        runTest(UnconfinedTestDispatcher()) {
            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleAuth(uri)

            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_INVALID_REQUEST", jsonPayload.getString("code"))
            assertEquals("Invalid challenge length", jsonPayload.getString("message"))
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleAuth_emitErrorResponseEventWhenOriginMaxLength() {
        runTest(UnconfinedTestDispatcher()) {
            val uri =
                Uri.parse(
                    "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsImxvZ2luX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS54eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eC5jb20vcmVzcG9uc2UiLCJnZXRfc2lnbmluZ19jZXJ0aWZpY2F0ZSI6dHJ1ZX0",
                )
            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleAuth(uri)

            val emittedUri = deferred.await()
            assert(
                emittedUri.toString().startsWith(
                    "https://example.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.com/response#",
                ),
            )
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_INVALID_REQUEST", jsonPayload.getString("code"))
            assertEquals("Invalid origin length", jsonPayload.getString("message"))
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleAuth_emitDialogErrorWhenGenericException() {
        runTest(UnconfinedTestDispatcher()) {
            val uri = Uri.parse("web-eid-mobile://auth#{}")
            viewModel.handleAuth(uri)
            assertEquals(R.string.web_eid_invalid_auth_request_error, viewModel.dialogError.value)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidAuthResult_buildsAuthTokenAndEmitsResponseEvent() {
        runTest(UnconfinedTestDispatcher()) {
            val cert = byteArrayOf(1, 2, 3)
            val signingCert = byteArrayOf(9, 9, 9)
            val signature = byteArrayOf(4, 5, 6)
            val uri =
                Uri.parse(
                    "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsImxvZ2luX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vcmVzcG9uc2UiLCJnZXRfc2lnbmluZ19jZXJ0aWZpY2F0ZSI6dHJ1ZX0",
                )
            whenever(authService.buildAuthToken(cert, signingCert, signature))
                .thenReturn(JSONObject().put("format", "web-eid:1.0"))
            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }
            viewModel.handleAuth(uri)
            viewModel.handleWebEidAuthResult(cert, signingCert, signature)

            verify(authService).buildAuthToken(cert, signingCert, signature)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            val authToken = jsonPayload.getJSONObject("auth_token")
            assertEquals("web-eid:1.0", authToken.getString("format"))
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidAuthResult_buildsAuthTokenWithoutSigningCert() {
        runTest(UnconfinedTestDispatcher()) {
            val cert = byteArrayOf(1, 2, 3)
            val signingCert = byteArrayOf(9, 9, 9)
            val signature = byteArrayOf(4, 5, 6)
            val uri =
                Uri.parse(
                    "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsImxvZ2luX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vcmVzcG9uc2UiLCJnZXRfc2lnbmluZ19jZXJ0aWZpY2F0ZSI6ZmFsc2V9",
                )
            whenever(authService.buildAuthToken(cert, null, signature))
                .thenReturn(JSONObject().put("format", "web-eid:1.0"))
            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }
            viewModel.handleAuth(uri)
            viewModel.handleWebEidAuthResult(cert, signingCert, signature)

            verify(authService).buildAuthToken(cert, null, signature)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            val authToken = jsonPayload.getJSONObject("auth_token")
            assertEquals("web-eid:1.0", authToken.getString("format"))
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidAuthResult_emitErrorResponseEventWhenException() {
        runTest(UnconfinedTestDispatcher()) {
            val cert = byteArrayOf(1, 2, 3)
            val signingCert = byteArrayOf(9, 9, 9)
            val signature = byteArrayOf(4, 5, 6)
            val uri =
                Uri.parse(
                    "web-eid-mobile://auth#eyJjaGFsbGVuZ2UiOiJ0ZXN0LWNoYWxsZW5nZS0wMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMCIsImxvZ2luX3VyaSI6Imh0dHBzOi8vZXhhbXBsZS5jb20vcmVzcG9uc2UiLCJnZXRfc2lnbmluZ19jZXJ0aWZpY2F0ZSI6dHJ1ZX0",
                )
            whenever(authService.buildAuthToken(cert, signingCert, signature))
                .thenThrow(RuntimeException("Test exception"))
            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }
            viewModel.handleAuth(uri)

            viewModel.handleWebEidAuthResult(cert, signingCert, signature)

            verify(authService).buildAuthToken(cert, signingCert, signature)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_UNKNOWN_ERROR", jsonPayload.getString("code"))
            assertEquals("Unexpected error", jsonPayload.getString("message"))
        }
    }

    @Test
    fun webEidViewModel_handleCertificate_parsesCertificateUriAndSetsStateFlow() {
        runTest {
            val uri =
                Uri.parse(
                    "web-eid-mobile://cert#eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIn0",
                )
            viewModel.handleCertificate(uri)
            val authRequest = viewModel.authRequest.value
            val certificateRequest = viewModel.certificateRequest.value
            val signRequest = viewModel.signRequest.value
            assert(authRequest == null)
            assert(certificateRequest != null)
            assert(signRequest == null)
            assertEquals("https://example.com/response", certificateRequest?.responseUri)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleCertificate_emitDialogErrorWhenGenericException() {
        runTest(UnconfinedTestDispatcher()) {
            val uri = Uri.parse("web-eid-mobile://cert#{}")
            viewModel.handleCertificate(uri)
            assertEquals(
                R.string.web_eid_invalid_request_error,
                viewModel.dialogError.value,
            )
        }
    }

    @Test
    fun webEidViewModel_handleSign_parsesSignUriAndSetsStateFlow() {
        runTest {
            val uri = Uri.parse(createSignUri(signingCertBase64))
            viewModel.handleSign(uri)
            val authRequest = viewModel.authRequest.value
            val signRequest = viewModel.signRequest.value
            assert(authRequest == null)
            assert(signRequest != null)
            assertEquals("https://rp.example.com/sign/response", signRequest?.responseUri)
            assertNotNull(signRequest?.hash)
            assertEquals("SHA-384", signRequest?.hashFunction)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleSign_emitErrorResponseEventWhenWebEidException() {
        runTest(UnconfinedTestDispatcher()) {
            val uri =
                Uri.parse(
                    "web-eid-mobile://sign#" +
                        "eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIiwic2lnbl9jZXJ0aWZpY2F0ZSI6InNpZ25lcnNlcnQiLCJoYXNoIjoiIn0",
                )

            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleSign(uri)

            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_INVALID_REQUEST", jsonPayload.getString("code"))
            assertEquals(
                "Invalid signing request: missing hash or hash_function",
                jsonPayload.getString("message"),
            )
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleSign_emitDialogErrorWhenGenericException() {
        runTest(UnconfinedTestDispatcher()) {
            val uri = Uri.parse("web-eid-mobile://sign#{}")
            viewModel.handleSign(uri)
            assertEquals(R.string.web_eid_invalid_request_error, viewModel.dialogError.value)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleUnknown_emitDialogError() {
        runTest(UnconfinedTestDispatcher()) {
            val uri = Uri.parse("web-eid-mobile://unknown#{}")
            viewModel.handleUnknown(uri)
            assertEquals(
                R.string.web_eid_invalid_request_error,
                viewModel.dialogError.value,
            )
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidCertificateResult_buildsCertificatePayloadAndEmitsResponseEvent() {
        runTest(UnconfinedTestDispatcher()) {
            val signingCert = byteArrayOf(1, 2, 3)
            val uri =
                Uri.parse(
                    "web-eid-mobile://sign#eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIiwic2lnbl9jZXJ0aWZpY2F0ZSI6InNpZ25pbmdfY2VydGlmaWNhdGUiLCJoYXNoIjoiaGFzaCIsImhhc2hfZnVuY3Rpb24iOiJoYXNoX2Z1bmN0aW9uIn0",
                )
            viewModel.handleCertificate(uri)

            whenever(signService.buildCertificatePayload(signingCert))
                .thenReturn(JSONObject().put("certificate", "mock-cert"))

            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleWebEidCertificateResult(signingCert)

            verify(signService).buildCertificatePayload(signingCert)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            val certificateValue = jsonPayload.getString("certificate")
            assertEquals("mock-cert", certificateValue)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidCertificateResult_emitErrorResponseEventWhenException() {
        runTest(UnconfinedTestDispatcher()) {
            val signingCert = byteArrayOf(1, 2, 3)
            val uri =
                Uri.parse(
                    "web-eid-mobile://sign#eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIiwic2lnbl9jZXJ0aWZpY2F0ZSI6InNpZ25pbmdfY2VydGlmaWNhdGUiLCJoYXNoIjoiaGFzaCIsImhhc2hfZnVuY3Rpb24iOiJoYXNoX2Z1bmN0aW9uIn0",
                )
            viewModel.handleCertificate(uri)

            whenever(signService.buildCertificatePayload(signingCert))
                .thenThrow(RuntimeException("Test exception"))

            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleWebEidCertificateResult(signingCert)

            verify(signService).buildCertificatePayload(signingCert)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_UNKNOWN_ERROR", jsonPayload.getString("code"))
            assertEquals("Unexpected error", jsonPayload.getString("message"))
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidSignResult_buildsSignPayloadAndEmitsResponseEvent() {
        runTest(UnconfinedTestDispatcher()) {
            val signingCert = "mock-sign-cert"
            val signature = byteArrayOf(1, 2, 3)
            val responseUri = "https://example.com/response"
            val hashFunction = "SHA-384"

            whenever(signService.buildSignPayload(signingCert, signature, hashFunction))
                .thenReturn(JSONObject().put("signature", "mock-signature"))

            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }

            viewModel.handleSign(Uri.parse(createSignUri(signingCertBase64)))
            viewModel.handleWebEidSignResult(signingCert, signature, responseUri)

            verify(signService).buildSignPayload(signingCert, signature, hashFunction)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            val signValue = jsonPayload.getString("signature")
            assertEquals("mock-signature", signValue)
        }
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun webEidViewModel_handleWebEidSignResult_emitErrorResponseEventWhenException() {
        runTest(UnconfinedTestDispatcher()) {
            val signingCert = "mock-sign-cert"
            val signature = byteArrayOf(1, 2, 3)
            val responseUri = "https://example.com/response"
            val hashFunction = "SHA-384"

            whenever(signService.buildSignPayload(signingCert, signature, hashFunction))
                .thenThrow(RuntimeException("Test exception"))

            val deferred =
                async {
                    viewModel.relyingPartyResponseEvents.first()
                }
            viewModel.handleSign(Uri.parse(createSignUri(signingCertBase64)))
            viewModel.handleWebEidSignResult(signingCert, signature, responseUri)

            verify(signService).buildSignPayload(signingCert, signature, hashFunction)
            val emittedUri = deferred.await()
            assert(emittedUri.toString().startsWith("https://example.com/response#"))
            assert(emittedUri.fragment != null)
            val decodedPayload = String(decode(emittedUri.fragment, URL_SAFE))
            val jsonPayload = JSONObject(decodedPayload)
            assertEquals("ERR_WEBEID_MOBILE_UNKNOWN_ERROR", jsonPayload.getString("code"))
            assertEquals("Unexpected error", jsonPayload.getString("message"))
        }
    }

    private fun createSignUri(signingCertificate: String? = null): String {
        val hash = validSha384Base64()
        val hashFunction = "SHA-384"
        val responseUri = "https://rp.example.com/sign/response"
        val sb = StringBuilder()
        sb.append("{\"response_uri\":\"$responseUri\"")
        sb.append(",\"hash\":\"$hash\"")
        sb.append(",\"hash_function\":\"$hashFunction\"")
        if (signingCertificate != null) {
            sb.append(",\"signing_certificate\":\"$signingCertificate\"")
        }
        sb.append("}")
        val encoded = Base64.getEncoder().encodeToString(sb.toString().toByteArray())
        return "web-eid-mobile://sign#$encoded"
    }

    private fun validSha384Base64(): String {
        val digest = java.security.MessageDigest.getInstance("SHA-384")
        val hash = digest.digest("test-data".toByteArray())
        return Base64.getEncoder().encodeToString(hash)
    }
}
