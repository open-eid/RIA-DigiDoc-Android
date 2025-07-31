@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ee.ria.DigiDoc.webEid.WebEidAuthService
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@RunWith(AndroidJUnit4::class)
class WebEidViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authService: WebEidAuthService

    private lateinit var viewModel: WebEidViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Default StateFlows for mock
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
}
