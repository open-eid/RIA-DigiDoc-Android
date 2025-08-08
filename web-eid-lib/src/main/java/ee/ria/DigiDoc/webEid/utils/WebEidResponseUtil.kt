@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import androidx.core.net.toUri
import org.json.JSONObject
import java.util.Base64

data class WebEidError(val code: String, val message: String)

object WebEidResponseUtil {
    fun createErrorRedirect(
        loginUri: String,
        code: String = WebEidErrorCodes.UNKNOWN,
        message: String = WebEidErrorCodes.UNKNOWN_MESSAGE,
    ): String {
        val errorJson =
            JSONObject()
                .put("code", code)
                .put("message", message)
                .toString()

        val encoded = base64UrlEncode(errorJson)
        return appendFragment(loginUri, encoded)
    }

    fun createSuccessRedirect(loginUri: String): String {
        val successJson =
            JSONObject()
                .put("web_eid_auth_token", "mock-web-eid-auth-token")
                .put("eid_instance_attestation", "mock-attestation")
                .put("eid_instance_attestation_proof", "mock-attestation-proof")
                .toString()

        val encoded = base64UrlEncode(successJson)
        return appendFragment(loginUri, encoded)
    }

    private fun base64UrlEncode(input: String): String {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(input.toByteArray(Charsets.UTF_8))
    }

    private fun appendFragment(
        loginUri: String,
        fragment: String,
    ): String {
        val uri = loginUri.toUri()
        return uri.buildUpon()
            .fragment(fragment)
            .build()
            .toString()
    }
}
