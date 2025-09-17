@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Base64

object WebEidAuthParser {
    private val logTag = javaClass.simpleName

    fun parseAuthUri(uri: Uri): WebEidAuthRequest {
        val json = decodeUriFragment(uri)

        val challenge = json.getString("challenge")
        val loginUriEncoded = json.getString("login_uri")
        val getSigningCertificate = json.optBoolean("get_signing_certificate", false)

        val loginUri = URLDecoder.decode(loginUriEncoded, StandardCharsets.UTF_8.name())

        validateHttpsScheme(loginUri)
        val origin = parseOriginFromLoginUri(loginUri)
        validateOriginCorrectness(loginUri, origin)

        return WebEidAuthRequest(
            challenge = challenge,
            loginUri = loginUri,
            getSigningCertificate = getSigningCertificate,
            origin = origin,
        )
    }

    fun parseSignUri(uri: Uri): WebEidSignRequest {
        val json = decodeUriFragment(uri)
        return WebEidSignRequest(
            responseUri = json.getString("response_uri"),
            signCertificate = json.getString("sign_certificate"),
            hash = json.getString("hash"),
            hashFunction = json.getString("hash_function"),
        )
    }

    private fun decodeUriFragment(uri: Uri): JSONObject {
        try {
            val fragment =
                uri.fragment ?: throw IllegalArgumentException(WebEidErrorCodes.ERR_WEBEID_MOBILE_INVALID_REQUEST)
            val decoded = String(Base64.getDecoder().decode(fragment))
            return JSONObject(decoded)
        } catch (e: Exception) {
            LoggingUtil.Companion.errorLog(
                logTag,
                "Failed to decode or parse URI fragment: ${uri.fragment}",
                e,
            )
            throw IllegalArgumentException(WebEidErrorCodes.ERR_WEBEID_MOBILE_INVALID_REQUEST, e)
        }
    }

    private fun validateHttpsScheme(loginUri: String) {
        try {
            val parsed = URI(loginUri)
            if (!parsed.scheme.equals("https", ignoreCase = true)) {
                LoggingUtil.Companion.errorLog(
                    logTag,
                    "Invalid scheme in login_uri: $loginUri — must be HTTPS",
                )
                throw IllegalArgumentException("login_uri must use HTTPS")
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            LoggingUtil.Companion.errorLog(logTag, "Invalid login_uri format: $loginUri", e)
            throw IllegalArgumentException("Invalid login_uri format", e)
        }
    }

    private fun validateOriginCorrectness(
        loginUri: String,
        origin: String,
    ) {
        try {
            val parsedLogin = URI(loginUri)
            val expected = URI(origin)

            if (!parsedLogin.host.equals(expected.host, ignoreCase = true) ||
                parsedLogin.port != expected.port
            ) {
                LoggingUtil.Companion.errorLog(
                    logTag,
                    "Origin mismatch: expected $origin but login_uri points to host ${parsedLogin.host}",
                )
                throw IllegalArgumentException("Origin mismatch: expected $origin")
            }

            if (parsedLogin.userInfo != null) {
                LoggingUtil.Companion.errorLog(
                    logTag,
                    "Login URI contains userinfo (possible phishing attempt): $loginUri",
                )
                throw IllegalArgumentException("Login URI contains userinfo (possible phishing attempt)")
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            LoggingUtil.Companion.errorLog(
                logTag,
                "Failed to validate origin correctness for $loginUri",
                e,
            )
            throw IllegalArgumentException("Invalid origin in login_uri", e)
        }
    }

    private fun parseOriginFromLoginUri(loginUri: String): String {
        return try {
            val parsed = URI(loginUri)
            if (parsed.scheme.isNullOrBlank() || parsed.host.isNullOrBlank()) {
                LoggingUtil.Companion.errorLog(
                    logTag,
                    "Invalid login_uri: missing scheme or host — $loginUri",
                )
                return ""
            }
            val portPart = if (parsed.port != -1) ":${parsed.port}" else ""
            "${parsed.scheme}://${parsed.host}$portPart"
        } catch (e: Exception) {
            LoggingUtil.Companion.errorLog(
                logTag,
                "Failed to parse origin from login_uri: $loginUri",
                e,
            )
            ""
        }
    }
}
