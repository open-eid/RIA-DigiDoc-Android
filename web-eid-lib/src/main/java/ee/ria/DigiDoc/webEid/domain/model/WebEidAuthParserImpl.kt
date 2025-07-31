@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

import android.net.Uri
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.json.JSONObject
import java.net.URI
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthParserImpl @Inject constructor() : WebEidAuthParser {

    private val logTag = javaClass.simpleName

    override fun parseAuthUri(uri: Uri): WebEidAuthRequest {
        val json = decodeUriFragment(uri)

        val challenge = json.getString("challenge")
        val loginUri = json.getString("login_uri")
        val getSigningCertificate = json.optBoolean("get_signing_certificate", false)

        validateHttpsScheme(loginUri)
        val origin = parseOriginFromLoginUri(loginUri)
        validateOriginCorrectness(loginUri, origin)

        return WebEidAuthRequest(
            challenge = challenge,
            loginUri = loginUri,
            getSigningCertificate = getSigningCertificate,
            origin = origin
        )
    }

    override fun parseSignUri(uri: Uri): WebEidSignRequest {
        val json = decodeUriFragment(uri)
        return WebEidSignRequest(
            responseUri = json.getString("response_uri"),
            signCertificate = json.getString("sign_certificate"),
            hash = json.getString("hash"),
            hashFunction = json.getString("hash_function")
        )
    }

    private fun decodeUriFragment(uri: Uri): JSONObject {
        val fragment = uri.fragment ?: throw IllegalArgumentException("No fragment in URI")
        val decoded = String(Base64.getDecoder().decode(fragment))
        return JSONObject(decoded)
    }

    private fun validateHttpsScheme(loginUri: String) {
        try {
            val parsed = URI(loginUri)
            if (!parsed.scheme.equals("https", ignoreCase = true)) {
                errorLog(logTag, "Invalid scheme in login_uri: $loginUri — must be HTTPS")
            }
        } catch (e: Exception) {
            errorLog(logTag, "Invalid login_uri format: $loginUri", e)
        }
    }

    private fun validateOriginCorrectness(loginUri: String, origin: String) {
        try {
            val parsedLogin = URI(loginUri)
            val expected = URI(origin)

            if (!parsedLogin.host.equals(expected.host, ignoreCase = true) ||
                parsedLogin.port != expected.port
            ) {
                errorLog(
                    logTag,
                    "Origin mismatch: expected $origin but login_uri points to host ${parsedLogin.host}"
                )
            }

            if (parsedLogin.userInfo != null) {
                errorLog(
                    logTag,
                    "Login URI contains userinfo (possible phishing attempt): $loginUri"
                )
            }
        } catch (e: Exception) {
            errorLog(logTag, "Failed to validate origin correctness for $loginUri", e)
        }
    }

    private fun parseOriginFromLoginUri(loginUri: String): String {
        return try {
            val parsed = URI(loginUri)
            if (parsed.scheme.isNullOrBlank() || parsed.host.isNullOrBlank()) {
                errorLog(logTag, "Invalid login_uri: missing scheme or host — $loginUri")
                return ""
            }
            val portPart = if (parsed.port != -1) ":${parsed.port}" else ""
            "${parsed.scheme}://${parsed.host}$portPart"
        } catch (e: Exception) {
            errorLog(logTag, "Failed to parse origin from login_uri: $loginUri", e)
            ""
        }
    }
}
