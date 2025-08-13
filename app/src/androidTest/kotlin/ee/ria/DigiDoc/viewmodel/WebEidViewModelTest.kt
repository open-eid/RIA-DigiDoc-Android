@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class WebEidViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authService: WebEidAuthService

    @Mock
    private lateinit var activity: Activity

    private lateinit var viewModel: WebEidViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        `when`(authService.authRequest).thenReturn(MutableStateFlow(null))
        `when`(authService.signRequest).thenReturn(MutableStateFlow(null))
        `when`(authService.errorState).thenReturn(MutableStateFlow(null))
        `when`(authService.redirectUri).thenReturn(MutableStateFlow(null))

        viewModel = WebEidViewModel(authService)
    }

    @Test
    fun handleAuth_callsParseAuthUri() {
        val uri = Uri.parse("web-eid-mobile://auth#dummyData")
        viewModel.handleAuth(uri)
        verify(authService).parseAuthUri(uri)
    }

    @Test
    fun handleSign_callsParseSignUri() {
        val uri = Uri.parse("web-eid-mobile://sign#dummyData")
        viewModel.handleSign(uri)
        verify(authService).parseSignUri(uri)
    }

    @Test
    fun reset_callsResetValues() {
        viewModel.reset()
        verify(authService).resetValues()
    }

    @Test
    fun redirectUri_isExposedFromAuthService() {
        val redirectFlow = MutableStateFlow("https://example.com#encodedPayload")
        `when`(authService.redirectUri).thenReturn(redirectFlow)
        val vm = WebEidViewModel(authService)
        assert(vm.redirectUri.value == "https://example.com#encodedPayload")
    }

    @Test
    fun redirectUri_updatesWhenServiceUpdates() {
        val redirectFlow = MutableStateFlow<String?>(null)
        `when`(authService.redirectUri).thenReturn(redirectFlow)
        val vm = WebEidViewModel(authService)
        redirectFlow.value = "https://example.com#updatedPayload"
        assert(vm.redirectUri.value == "https://example.com#updatedPayload")
    }

    @Test
    fun handleWebEidAuthResult_callsBuildAuthToken_whenPayloadValid() {
        val cert = byteArrayOf(1, 2, 3)
        val signature = byteArrayOf(4, 5, 6)
        val challenge = "test-challenge"
        val loginUri = "https://example.com/login"
        val getSigningCertificate = true
        val origin = "https://example.com"

        val authRequest =
            WebEidAuthRequest(
                challenge = challenge,
                loginUri = loginUri,
                getSigningCertificate = getSigningCertificate,
                origin = origin,
            )
        `when`(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        val token = JSONObject().put("mock", "token")
        `when`(authService.buildAuthToken(cert, signature, challenge)).thenReturn(token)

        viewModel = WebEidViewModel(authService)

        viewModel.handleWebEidAuthResult(cert, signature, activity)

        verify(authService).buildAuthToken(cert, signature, challenge)
        verify(activity).startActivity(any())
        verify(activity).finish()
    }

    @Test
    fun handleWebEidAuthResult_doesNothing_whenChallengeMissing() {
        val cert = byteArrayOf(1)
        val signature = byteArrayOf(2)

        val authRequest =
            WebEidAuthRequest(
                challenge = "",
                loginUri = "https://example.com",
                getSigningCertificate = true,
                origin = "https://example.com",
            )
        `when`(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        viewModel = WebEidViewModel(authService)
        viewModel.handleWebEidAuthResult(cert, signature, activity)

        verify(authService, never()).buildAuthToken(any(), any(), any())
        verify(activity, never()).startActivity(any())
    }

    @Test
    fun handleWebEidAuthResult_doesNothing_whenLoginUriMissing() {
        val cert = byteArrayOf(1)
        val signature = byteArrayOf(2)

        val authRequest =
            WebEidAuthRequest(
                challenge = "abc",
                loginUri = "",
                getSigningCertificate = true,
                origin = "https://example.com",
            )
        `when`(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        viewModel = WebEidViewModel(authService)
        viewModel.handleWebEidAuthResult(cert, signature, activity)

        verify(authService, never()).buildAuthToken(any(), any(), any())
        verify(activity, never()).startActivity(any())
    }
}
