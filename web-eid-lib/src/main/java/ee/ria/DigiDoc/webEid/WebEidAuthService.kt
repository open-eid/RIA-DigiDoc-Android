@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import android.net.Uri
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import ee.ria.DigiDoc.webEid.domain.model.WebEidSignRequest
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

interface WebEidAuthService {
    val authRequest: StateFlow<WebEidAuthRequest?>
    val signRequest: StateFlow<WebEidSignRequest?>
    val errorState: StateFlow<String?>
    val redirectUri: StateFlow<String?>

    fun resetValues()

    fun parseAuthUri(uri: Uri)

    fun parseSignUri(uri: Uri)

    fun buildAuthToken(
        certBytes: ByteArray,
        signature: ByteArray,
        challenge: String,
    ): JSONObject
}
