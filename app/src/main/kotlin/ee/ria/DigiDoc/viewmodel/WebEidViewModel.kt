@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import kotlinx.coroutines.flow.StateFlow
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

        fun handleAuth(uri: Uri) {
            authService.parseAuthUri(uri)
        }

        fun handleSign(uri: Uri) {
            authService.parseSignUri(uri)
        }

        fun reset() {
            authService.resetValues()
        }

        fun handleWebEidAuthResult(
            cert: ByteArray,
            signature: ByteArray,
        ) {
            val token = authService.buildAuthToken(cert, signature)

            // TODO: send tokenJson.toString() to backend or pass to WebEidViewModel
            LoggingUtil.debugLog("WebEidViewModel", "Web EID token = $token")
        }
    }
