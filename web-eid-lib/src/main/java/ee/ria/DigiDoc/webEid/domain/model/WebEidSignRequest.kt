@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

data class WebEidSignRequest(
    val responseUri: String,
    val signCertificate: String,
    val hash: String,
    val hashFunction: String,
)
