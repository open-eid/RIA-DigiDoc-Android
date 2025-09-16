@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.util.Base64
import androidx.core.net.toUri
import org.json.JSONObject

object WebEidResponseUtil {
    fun createRedirect(
        loginUri: String,
        payload: JSONObject,
    ): String {
        val encoded = base64UrlEncode(payload)
        return appendFragment(loginUri, encoded)
    }

    fun base64UrlEncode(input: JSONObject): String =
        Base64.encodeToString(
            input.toString().toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
        )

    fun appendFragment(
        loginUri: String,
        fragment: String,
    ): String {
        val uri = loginUri.toUri()
        return uri
            .buildUpon()
            .fragment(fragment)
            .build()
            .toString()
    }
}
