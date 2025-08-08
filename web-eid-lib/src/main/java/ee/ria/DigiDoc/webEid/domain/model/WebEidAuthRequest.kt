@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

data class WebEidAuthRequest(
    val challenge: String,
    val loginUri: String,
    val getSigningCertificate: Boolean,
    val origin: String,
)
