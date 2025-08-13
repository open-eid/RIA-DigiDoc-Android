@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
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
            activity: Activity,
        ) {
            val challenge = authPayload.value?.challenge
            val loginUri = authPayload.value?.loginUri

            if (challenge.isNullOrBlank()) {
                errorLog("WebEidViewModel", "Missing challenge in auth payload")
                return
            }

            if (loginUri.isNullOrBlank()) {
                errorLog("WebEidViewModel", "Missing login_uri in auth payload")
                return
            }

            val token = authService.buildAuthToken(cert, signature, challenge)

            debugLog("WebEidViewModel", "Sending token to loginUri: $loginUri")

            authService.sendAuthTokenToBackend(
                token,
                loginUri,
                onSuccess = {
                    debugLog("WebEidViewModel", "Authentication success")
                    activity.finish()
                },
                onError = {
                    errorLog("WebEidViewModel", "Authentication failed", it)
                },
            )
        }
    }
