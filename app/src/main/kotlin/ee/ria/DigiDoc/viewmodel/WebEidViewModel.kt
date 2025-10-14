@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode
import ee.ria.DigiDoc.webEid.exception.WebEidException
import ee.ria.DigiDoc.webEid.utils.WebEidRequestParser
import ee.ria.DigiDoc.webEid.utils.WebEidResponseUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class WebEidViewModel
    @Inject
    constructor(
        private val authService: WebEidAuthService,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName
        private val _authRequest = MutableStateFlow<WebEidAuthRequest?>(null)
        val authRequest: StateFlow<WebEidAuthRequest?> = _authRequest.asStateFlow()
        private val _signRequest = MutableStateFlow<WebEidSignRequest?>(null)
        val signRequest: StateFlow<WebEidSignRequest?> = _signRequest.asStateFlow()
        private val _relyingPartyResponseEvents = MutableSharedFlow<Uri>()
        val relyingPartyResponseEvents: SharedFlow<Uri> = _relyingPartyResponseEvents.asSharedFlow()
        private val _dialogError = MutableLiveData<Int>(null)
        val dialogError: LiveData<Int> = _dialogError

        suspend fun handleAuth(uri: Uri) {
            try {
                _authRequest.value = WebEidRequestParser.parseAuthUri(uri)
            } catch (e: WebEidException) {
                errorLog(logTag, "Invalid Web eID authentication request: $uri", e)
                val errorPayload = WebEidResponseUtil.createErrorPayload(e.errorCode, e.message)
                val responseUri = WebEidResponseUtil.createResponseUri(e.responseUri, errorPayload)
                _relyingPartyResponseEvents.emit(responseUri)
            } catch (e: Exception) {
                errorLog(logTag, "Unable parse Web eID authentication request: $uri", e)
                _dialogError.postValue(R.string.web_eid_invalid_auth_request_error)
            }
        }

        fun handleSign(uri: Uri) {
            try {
                _signRequest.value = WebEidRequestParser.parseSignUri(uri)
            } catch (e: Exception) {
                errorLog(logTag, "Unable parse Web eID signing request: $uri", e)
                _dialogError.postValue(R.string.web_eid_invalid_sign_request_error)
            }
        }

        fun handleUnknown(uri: Uri) {
            errorLog(logTag, "Unable parse Web eID request: $uri")
            _dialogError.postValue(R.string.web_eid_invalid_sign_request_error)
        }

        suspend fun handleWebEidAuthResult(
            authCert: ByteArray,
            signingCert: ByteArray,
            signature: ByteArray,
        ) {
            val loginUri = authRequest.value?.loginUri!!
            val getSigningCertificate = authRequest.value?.getSigningCertificate

            try {
                val token =
                    authService.buildAuthToken(
                        authCert,
                        if (getSigningCertificate == true) signingCert else null,
                        signature,
                    )
                val payload = JSONObject().put("auth-token", token)
                val responseUri = WebEidResponseUtil.createResponseUri(loginUri, payload)
                _relyingPartyResponseEvents.emit(responseUri)
            } catch (e: Exception) {
                errorLog(logTag, "Unexpected error building auth token", e)
                val errorPayload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCode.ERR_WEBEID_MOBILE_UNKNOWN_ERROR,
                        "Unexpected error",
                    )
                val responseUri = WebEidResponseUtil.createResponseUri(loginUri, errorPayload)
                _relyingPartyResponseEvents.emit(responseUri)
            }
        }
    }
