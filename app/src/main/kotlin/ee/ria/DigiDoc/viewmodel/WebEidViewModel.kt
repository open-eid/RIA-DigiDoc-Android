@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.utils.WebEidErrorCodes
import ee.ria.DigiDoc.webEid.utils.WebEidResponseUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
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
        val errorEvents: SharedFlow<Triple<String, String, String>> get() = _errorEvents

        private val _errorEvents = MutableSharedFlow<Triple<String, String, String>>()

        fun handleAuth(uri: Uri) {
            try {
                authService.parseAuthUri(uri)
            } catch (e: IllegalArgumentException) {
                errorLog("WebEidViewModel", "Invalid Web eID auth URI", e)

                _errorEvents.tryEmit(
                    Triple(
                        uri.toString(),
                        WebEidErrorCodes.ERR_WEBEID_MOBILE_INVALID_REQUEST,
                        WebEidErrorCodes.ERR_WEBEID_MOBILE_INVALID_REQUEST,
                    ),
                )
            } catch (e: Exception) {
                errorLog("WebEidViewModel", "Unexpected error parsing Web eID auth URI", e)

                _errorEvents.tryEmit(
                    Triple(
                        uri.toString(),
                        WebEidErrorCodes.ERR_WEBEID_MOBILE_UNKNOWN,
                        e.message ?: WebEidErrorCodes.ERR_WEBEID_MOBILE_UNKNOWN,
                    ),
                )
            }
        }

        fun handleSign(uri: Uri) {
            authService.parseSignUri(uri)
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

            try {
                val token = authService.buildAuthToken(authCert, signingCert, signature, challenge)
                val payload = JSONObject().put("auth-token", token)

                WebEidResponseUtil.launchRedirect(activity, loginUri, payload)
            } catch (e: Exception) {
                val payload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCodes.ERR_WEBEID_MOBILE_UNKNOWN,
                        e.message ?: WebEidErrorCodes.ERR_WEBEID_MOBILE_UNKNOWN,
                    )

                WebEidResponseUtil.launchRedirect(activity, loginUri, payload)
            }
        }
    }
