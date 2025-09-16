@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
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
            authCert: ByteArray,
            signingCert: ByteArray,
            signature: ByteArray,
            activity: Activity,
        ) {
            val challenge = authPayload.value?.challenge
            val loginUri = authPayload.value?.loginUri

            if (challenge.isNullOrBlank() || loginUri.isNullOrBlank()) {
                errorLog("WebEidViewModel", "Missing challenge or loginUri in auth payload")
                return
            }

            val token = authService.buildAuthToken(authCert, signingCert, signature, challenge)

            try {
                val payload = JSONObject().put("auth-token", token)
                val encoded = Base64.encodeToString(payload.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val browserUri = "$loginUri#$encoded"

                debugLog("WebEidViewModel", "Launching browser back to: $browserUri")

                val intent =
                    Intent(Intent.ACTION_VIEW, browserUri.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                activity.startActivity(intent)
                activity.finish()
            } catch (e: Exception) {
                val payload = JSONObject()
                    .put("error", true)
                    .put("code", "TOKEN_BUILD_FAILED")
                    .put("message", e.message ?: "Failed to return token to browser")

                val encoded = Base64.encodeToString(payload.toString().toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val browserUri = "$loginUri#$encoded"
                activity.startActivity(Intent(Intent.ACTION_VIEW, browserUri.toUri()).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                })
                activity.finish()
            }
        }
    }
