@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import org.json.JSONObject

interface WebEidAuthService {
    fun buildAuthToken(
        authCert: ByteArray,
        signingCert: ByteArray?,
        signature: ByteArray,
    ): JSONObject
}
