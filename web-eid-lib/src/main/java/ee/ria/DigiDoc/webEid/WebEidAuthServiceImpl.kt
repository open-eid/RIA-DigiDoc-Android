@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthServiceImpl
    @Inject
    constructor() : WebEidAuthService {
        private val logTag = javaClass.simpleName

        private val _authRequest = MutableStateFlow<WebEidAuthRequest?>(null)
        override val authRequest: StateFlow<WebEidAuthRequest?> = _authRequest.asStateFlow()

        private val _signRequest = MutableStateFlow<WebEidSignRequest?>(null)
        override val signRequest: StateFlow<WebEidSignRequest?> = _signRequest.asStateFlow()

        private val _errorState = MutableStateFlow<String?>(null)
        override val errorState: StateFlow<String?> = _errorState.asStateFlow()

        override fun resetValues() {
            _authRequest.value = null
            _signRequest.value = null
            _errorState.value = null
        }

        override fun parseAuthUri(uri: Uri) {
            try {
                _authRequest.value = WebEidAuthParser.parseAuthUri(uri)
            } catch (e: IllegalArgumentException) {
                errorLog(logTag, "Validation failed in parseAuthUri", e)
                _errorState.value = e.message
            } catch (e: Exception) {
                errorLog(logTag, "Failed to parse Web eID auth URI", e)
                _errorState.value = e.message
            }
        }

        override fun parseSignUri(uri: Uri) {
            try {
                _signRequest.value = WebEidAuthParser.parseSignUri(uri)
            } catch (e: IllegalArgumentException) {
                errorLog(logTag, "Validation failed in parseSignUri", e)
                _errorState.value = e.message
            } catch (e: Exception) {
                errorLog(logTag, "Failed to parse Web eID sign URI", e)
                _errorState.value = e.message
            }
        }

        override fun buildAuthToken(
            authCert: ByteArray,
            signingCert: ByteArray,
            signature: ByteArray,
            challenge: String,
        ): JSONObject = WebEidAuthParser.buildAuthToken(authCert, signingCert, signature, challenge)
    }
