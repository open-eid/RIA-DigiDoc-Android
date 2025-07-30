@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

import android.net.Uri
import org.json.JSONObject
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebEidAuthParser @Inject constructor() {

    fun parseAuthUri(uri: Uri): WebEidAuthRequest {
        val json = decodeUriFragment(uri)
        return WebEidAuthRequest(
            challenge = json.getString("challenge"),
            loginUri = json.getString("login_uri"),
            getSigningCertificate = json.optBoolean("get_signing_certificate", false)
        )
    }

    fun parseSignUri(uri: Uri): WebEidSignRequest {
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
}
