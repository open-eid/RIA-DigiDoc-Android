@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import android.util.Base64.URL_SAFE
import android.util.Base64.decode
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ee.ria.DigiDoc.webEid.WebEidAuthService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class WebEidViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authService: WebEidAuthService

    private lateinit var viewModel: WebEidViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = WebEidViewModel(authService)
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
    fun webEidViewModel_handleSign_parsesSignUriAndSetsStateFlow() {
        val uri =
            Uri.parse(
                "web-eid-mobile://sign#eyJyZXNwb25zZV91cmkiOiJodHRwczovL2V4YW1wbGUuY29tL3Jlc3BvbnNlIiwic2lnbl9jZXJ0aWZpY2F0ZSI6InNpZ25pbmdfY2VydGlmaWNhdGUiLCJoYXNoIjoiaGFzaCIsImhhc2hfZnVuY3Rpb24iOiJoYXNoX2Z1bmN0aW9uIn0",
            )
        viewModel.handleSign(uri)
        val authRequest = viewModel.authRequest.value
        val signRequest = viewModel.signRequest.value
        assert(authRequest == null)
        assert(signRequest != null)
        assertEquals("https://example.com/response", signRequest?.responseUri)
        assertEquals("signing_certificate", signRequest?.signCertificate)
        assertEquals("hash", signRequest?.hash)
        assertEquals("hash_function", signRequest?.hashFunction)
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
            val authToken = jsonPayload.getJSONObject("auth-token")
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
}
