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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthService @Inject constructor(
    private val parser: WebEidAuthParser
) {
    private val logTag = javaClass.simpleName

    private val _authRequest = MutableStateFlow<WebEidAuthRequest?>(null)
    val authRequest: StateFlow<WebEidAuthRequest?> = _authRequest.asStateFlow()

    private val _signRequest = MutableStateFlow<WebEidSignRequest?>(null)
    val signRequest: StateFlow<WebEidSignRequest?> = _signRequest.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    fun resetValues() {
        _authRequest.value = null
        _signRequest.value = null
        _errorState.value = null
    }

    fun parseAuthUri(uri: Uri) {
        try {
            _authRequest.value = parser.parseAuthUri(uri)
        } catch (e: Exception) {
            errorLog(logTag, "Failed to parse Web eID auth URI", e)
            _errorState.value = e.message
        }
    }

    fun parseSignUri(uri: Uri) {
        try {
            _signRequest.value = parser.parseSignUri(uri)
        } catch (e: Exception) {
            errorLog(logTag, "Failed to parse Web eID sign URI", e)
            _errorState.value = e.message
        }
    }
}
