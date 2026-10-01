/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.WebEidSignService
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidCertificateRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode
import ee.ria.DigiDoc.webEid.exception.WebEidException
import ee.ria.DigiDoc.webEid.utils.WebEidRequestParser
import ee.ria.DigiDoc.webEid.utils.WebEidResponseUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

data class WebEidResponse(
    val uri: Uri,
    val isCompletedOperation: Boolean,
)

@HiltViewModel
class WebEidViewModel
    @Inject
    constructor(
        private val authService: WebEidAuthService,
        private val signService: WebEidSignService,
    ) : ViewModel() {
        private val logTag = javaClass.simpleName

        private val _authRequest = MutableStateFlow<WebEidAuthRequest?>(null)
        val authRequest: StateFlow<WebEidAuthRequest?> = _authRequest.asStateFlow()
        private val _certificateRequest = MutableStateFlow<WebEidCertificateRequest?>(null)
        val certificateRequest: StateFlow<WebEidCertificateRequest?> = _certificateRequest.asStateFlow()
        private val _signRequest = MutableStateFlow<WebEidSignRequest?>(null)
        val signRequest: StateFlow<WebEidSignRequest?> = _signRequest.asStateFlow()
        private val _relyingPartyResponseEvents = Channel<WebEidResponse>(Channel.BUFFERED)
        val relyingPartyResponseEvents: Flow<WebEidResponse> = _relyingPartyResponseEvents.receiveAsFlow()
        private val hasResponded = AtomicBoolean(false)
        private val hasDispatchedResponse = AtomicBoolean(false)
        private val _dialogError = MutableStateFlow(0)
        val dialogError: StateFlow<Int> = _dialogError

        private val _browserSelectionUri = MutableStateFlow<Uri?>(null)
        val browserSelectionUri: StateFlow<Uri?> = _browserSelectionUri.asStateFlow()

        fun requestBrowserSelection(responseUri: Uri) {
            _browserSelectionUri.value = responseUri
        }

        fun startResponseDispatch(): Boolean {
            if (!hasDispatchedResponse.compareAndSet(false, true)) {
                errorLog(logTag, "Ignoring repeated Web eID response dispatch")
                return false
            }
            _browserSelectionUri.value = null
            return true
        }

        val requestOrigin: String?
            get() =
                authRequest.value?.origin
                    ?: certificateRequest.value?.origin
                    ?: signRequest.value?.origin

        suspend fun handleAuth(uri: Uri) {
            try {
                _authRequest.value = WebEidRequestParser.parseAuthUri(uri)
            } catch (e: WebEidException) {
                errorLog(logTag, "Invalid Web eID authentication request: $uri", e)
                val errorPayload = WebEidResponseUtil.createErrorPayload(e.errorCode, e.message)
                val responseUri = WebEidResponseUtil.createResponseUri(e.responseUri, errorPayload)
                sendResponse(responseUri, isCompletedOperation = false)
            } catch (e: Exception) {
                errorLog(logTag, "Unable parse Web eID authentication request: $uri", e)
                _dialogError.value = R.string.web_eid_invalid_auth_request_error
            }
        }

        fun handleCertificate(uri: Uri) {
            try {
                _certificateRequest.value = WebEidRequestParser.parseCertificateUri(uri)
            } catch (e: Exception) {
                errorLog(logTag, "Unable parse Web eID certificate request: $uri", e)
                _dialogError.value = R.string.web_eid_invalid_request_error
            }
        }

        suspend fun handleSign(uri: Uri) {
            try {
                _signRequest.value = WebEidRequestParser.parseSignUri(uri)
            } catch (e: WebEidException) {
                errorLog(logTag, "Invalid Web eID signing request: $uri", e)
                val errorPayload = WebEidResponseUtil.createErrorPayload(e.errorCode, e.message)
                val responseUri = WebEidResponseUtil.createResponseUri(e.responseUri, errorPayload)
                sendResponse(responseUri, isCompletedOperation = false)
            } catch (e: Exception) {
                errorLog(logTag, "Unable parse Web eID signing request: $uri", e)
                _dialogError.value = R.string.web_eid_invalid_request_error
            }
        }

        fun handleUnknown(uri: Uri) {
            errorLog(logTag, "Unable parse Web eID request: $uri")
            _dialogError.value = R.string.web_eid_invalid_request_error
        }

        suspend fun handleWebEidAuthResult(
            authCert: ByteArray,
            signingCert: ByteArray?,
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
                val payload = JSONObject().put("authToken", token)
                val responseUri = WebEidResponseUtil.createResponseUri(loginUri, payload)
                sendResponse(responseUri, isCompletedOperation = true)
            } catch (e: Exception) {
                errorLog(logTag, "Unexpected error building auth token", e)
                val errorPayload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCode.ERR_WEBEID_MOBILE_UNKNOWN_ERROR,
                        "Unexpected error",
                    )
                val responseUri = WebEidResponseUtil.createResponseUri(loginUri, errorPayload)
                sendResponse(responseUri, isCompletedOperation = false)
            }
        }

        suspend fun handleWebEidCertificateResult(signingCert: ByteArray) {
            val responseUri = certificateRequest.value?.responseUri

            if (responseUri.isNullOrBlank()) {
                errorLog(logTag, "Missing responseUri in sign payload for certificate step")
                return
            }

            try {
                val payload = signService.buildCertificatePayload(signingCert)
                val response = WebEidResponseUtil.createResponseUri(responseUri, payload)
                sendResponse(response, isCompletedOperation = true)
            } catch (e: Exception) {
                errorLog(logTag, "Unexpected error building certificate payload", e)
                val errorPayload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCode.ERR_WEBEID_MOBILE_UNKNOWN_ERROR,
                        "Unexpected error",
                    )
                val errorUri = WebEidResponseUtil.createResponseUri(responseUri, errorPayload)
                sendResponse(errorUri, isCompletedOperation = false)
            }
        }

        suspend fun handleWebEidSignResult(
            signingCert: String,
            signature: ByteArray,
            responseUri: String,
        ) {
            try {
                val hashFunction =
                    signRequest.value?.hashFunction
                        ?: throw IllegalStateException("Missing signRequest")

                val payload =
                    signService.buildSignPayload(
                        signingCert,
                        signature,
                        hashFunction,
                    )
                val response = WebEidResponseUtil.createResponseUri(responseUri, payload)
                sendResponse(response, isCompletedOperation = true)
            } catch (e: Exception) {
                errorLog(logTag, "Unexpected error building sign payload", e)
                val errorPayload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCode.ERR_WEBEID_MOBILE_UNKNOWN_ERROR,
                        "Unexpected error",
                    )
                val errorUri = WebEidResponseUtil.createResponseUri(responseUri, errorPayload)
                sendResponse(errorUri, isCompletedOperation = false)
            }
        }

        suspend fun handleUserCancelled() {
            try {
                val responseUri =
                    authRequest.value?.loginUri
                        ?: certificateRequest.value?.responseUri
                        ?: signRequest.value?.responseUri

                if (responseUri.isNullOrBlank()) {
                    errorLog(logTag, "Cannot send cancel response — missing response URI")
                    return
                }

                val errorPayload =
                    WebEidResponseUtil.createErrorPayload(
                        WebEidErrorCode.ERR_WEBEID_USER_CANCELLED,
                        "User cancelled",
                    )

                val errorUri =
                    WebEidResponseUtil.createResponseUri(responseUri, errorPayload)

                sendResponse(errorUri, isCompletedOperation = false)
            } catch (e: Exception) {
                errorLog(logTag, "Failed to send cancel response", e)
            }
        }

        private suspend fun sendResponse(
            responseUri: Uri,
            isCompletedOperation: Boolean,
        ) {
            if (!hasResponded.compareAndSet(false, true)) {
                errorLog(logTag, "Ignoring additional Web eID response")
                return
            }
            _relyingPartyResponseEvents.send(WebEidResponse(responseUri, isCompletedOperation))
        }
    }
