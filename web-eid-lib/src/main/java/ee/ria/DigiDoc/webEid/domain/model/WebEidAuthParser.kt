@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

import android.net.Uri

interface WebEidAuthParser {
    fun parseAuthUri(uri: Uri): WebEidAuthRequest
    fun parseSignUri(uri: Uri): WebEidSignRequest
    fun handleAuthFlow(uri: Uri): String
}
