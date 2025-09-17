@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.app.Activity
import android.content.Intent
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

    fun createErrorPayload(
        code: String,
        message: String,
    ): JSONObject =
        JSONObject()
            .put("error", true)
            .put("code", code)
            .put("message", message)

    fun launchRedirect(
        activity: Activity,
        loginUri: String,
        payload: JSONObject,
    ) {
        val browserUri = createRedirect(loginUri, payload)
        val intent =
            Intent(Intent.ACTION_VIEW, browserUri.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        activity.startActivity(intent)
        activity.finish()
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
