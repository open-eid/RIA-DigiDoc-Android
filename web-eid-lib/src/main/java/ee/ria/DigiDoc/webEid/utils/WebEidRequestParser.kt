@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.net.Uri
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode.ERR_WEBEID_MOBILE_INVALID_REQUEST
import ee.ria.DigiDoc.webEid.exception.WebEidException
import org.json.JSONObject
import java.net.URI
import java.net.URISyntaxException
import java.util.Base64

object WebEidRequestParser {
    private const val MIN_CHALLENGE_LENGTH = 44
    private const val MAX_CHALLENGE_LENGTH = 128
    private const val MAX_ORIGIN_LENGTH = 255

    fun parseAuthUri(authUri: Uri): WebEidAuthRequest {
        val request = decodeUriFragment(authUri)
        val challenge = request.getString("challenge")
        val responseUri = validateResponseUri(request.getString("login_uri"))
        val origin = parseOrigin(responseUri)
        if (challenge.isNullOrBlank() ||
            challenge.length < MIN_CHALLENGE_LENGTH ||
            challenge.length > MAX_CHALLENGE_LENGTH
        ) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid challenge length",
                responseUri.toString(),
            )
        }

        return WebEidAuthRequest(
            challenge = challenge,
            loginUri = responseUri.toString(),
            getSigningCertificate = request.optBoolean("get_signing_certificate", false),
            origin = origin,
        )
    }

    fun parseSignUri(uri: Uri): WebEidSignRequest {
        val request = decodeUriFragment(uri)
        val responseUri = validateResponseUri(request.getString("response_uri"))

        return WebEidSignRequest(
            responseUri = responseUri.toString(),
            signCertificate = request.getString("sign_certificate"),
            hash = request.getString("hash"),
            hashFunction = request.getString("hash_function"),
        )
    }

    private fun validateResponseUri(responseUri: String): URI {
        try {
            val uri = URI(responseUri)
            if (uri.scheme.isNullOrBlank()) {
                throw IllegalArgumentException("Invalid response URI scheme")
            }
            if (!uri.scheme.equals("https", ignoreCase = true)) {
                throw IllegalArgumentException("Response URI must use HTTPS scheme")
            }
            if (uri.host.isNullOrBlank()) {
                throw IllegalArgumentException("Invalid response URI host")
            }
            if (uri.userInfo != null) {
                throw IllegalArgumentException("Response URI must not contain userinfo")
            }
            return uri
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException("Invalid response URI", e)
        }
    }

    private fun decodeUriFragment(uri: Uri): JSONObject {
        try {
            val fragment =
                uri.fragment ?: throw IllegalArgumentException("Missing URI fragment")
            val decoded = String(Base64.getDecoder().decode(fragment))
            return JSONObject(decoded)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URI fragment", e)
        }
    }

    private fun parseOrigin(uri: URI): String {
        val portPart = if (uri.port != -1) ":${uri.port}" else ""
        val origin = "${uri.scheme}://${uri.host}$portPart"
        if (origin.length > MAX_ORIGIN_LENGTH) {
            throw WebEidException(
                ERR_WEBEID_MOBILE_INVALID_REQUEST,
                "Invalid origin length",
                uri.toString(),
            )
        }
        return origin
    }
}
