@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WebEidViewModel
    @Inject
    constructor(
        private val authService: WebEidAuthService,
    ) : ViewModel() {
        val authPayload: StateFlow<WebEidAuthRequest?> = authService.authRequest
        val signPayload: StateFlow<WebEidSignRequest?> = authService.signRequest
        val errorState: StateFlow<String?> = authService.errorState
        val redirectUri: StateFlow<String?> = authService.redirectUri
        private val _canNumber = MutableStateFlow("")
        val canNumber = _canNumber.asStateFlow()

        private val _pin1Code = MutableStateFlow(ByteArray(0))
        val pin1Code = _pin1Code.asStateFlow()

        fun handleAuth(uri: Uri) {
            authService.parseAuthUri(uri)
        }

        fun handleSign(uri: Uri) {
            authService.parseSignUri(uri)
        }

        fun reset() {
            authService.resetValues()
        }

        fun setCanNumber(value: String) {
            _canNumber.value = value
        }

        fun setPin1Code(value: ByteArray) {
            _pin1Code.value = value
        }
    }
