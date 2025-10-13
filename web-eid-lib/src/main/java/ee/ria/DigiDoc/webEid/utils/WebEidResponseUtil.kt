@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.utils

import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import ee.ria.DigiDoc.webEid.exception.WebEidErrorCode
import org.json.JSONObject

object WebEidResponseUtil {
    fun createErrorPayload(
        code: WebEidErrorCode,
        message: String,
    ): JSONObject =
        JSONObject()
            .put("error", true)
            .put("code", code)
            .put("message", message)

    fun createResponseUri(
        responseUri: String,
        payload: JSONObject,
    ): Uri {
        val encodedPayload =
            Base64.encodeToString(
                payload.toString().toByteArray(Charsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
            )
        return responseUri
            .toUri()
            .buildUpon()
            .fragment(encodedPayload)
            .build()
    }
}
