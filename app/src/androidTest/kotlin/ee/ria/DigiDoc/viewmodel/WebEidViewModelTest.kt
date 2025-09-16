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
import org.mockito.kotlin.whenever

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
    fun handleWebEidAuthResult_callsBuildAuthToken_whenPayloadValid() {
        val cert = byteArrayOf(1, 2, 3)
        val signingCert = byteArrayOf(9, 9, 9)
        val signature = byteArrayOf(4, 5, 6)
        val challenge = "test-challenge"
        val loginUri = "https://example.com/login"
        val origin = "https://example.com"

        val authRequest =
            WebEidAuthRequest(
                challenge = challenge,
                loginUri = loginUri,
                getSigningCertificate = true,
                origin = origin,
            )
        whenever(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        val token = JSONObject().put("mock", "token")
        whenever(authService.buildAuthToken(cert, signingCert, signature, challenge)).thenReturn(token)

        viewModel = WebEidViewModel(authService)

        viewModel.handleWebEidAuthResult(cert, signingCert, signature, activity)

        verify(authService).buildAuthToken(cert, signingCert, signature, challenge)
        verify(activity).startActivity(any())
        verify(activity).finish()
    }

    @Test
    fun handleWebEidAuthResult_doesNothing_whenChallengeMissing() {
        val cert = byteArrayOf(1)
        val signingCert = byteArrayOf(9)
        val signature = byteArrayOf(2)

        val authRequest =
            WebEidAuthRequest(
                challenge = "",
                loginUri = "https://example.com",
                getSigningCertificate = true,
                origin = "https://example.com",
            )
        whenever(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        viewModel = WebEidViewModel(authService)
        viewModel.handleWebEidAuthResult(cert, signingCert, signature, activity)

        verify(authService, never()).buildAuthToken(any(), any(), any(), any())
        verify(activity, never()).startActivity(any())
    }

    @Test
    fun handleWebEidAuthResult_doesNothing_whenLoginUriMissing() {
        val cert = byteArrayOf(1)
        val signingCert = byteArrayOf(9)
        val signature = byteArrayOf(2)

        val authRequest =
            WebEidAuthRequest(
                challenge = "abc",
                loginUri = "",
                getSigningCertificate = true,
                origin = "https://example.com",
            )
        whenever(authService.authRequest).thenReturn(MutableStateFlow(authRequest))

        viewModel = WebEidViewModel(authService)
        viewModel.handleWebEidAuthResult(cert, signingCert, signature, activity)

        verify(authService, never()).buildAuthToken(any(), any(), any(), any())
        verify(activity, never()).startActivity(any())
    }
}
