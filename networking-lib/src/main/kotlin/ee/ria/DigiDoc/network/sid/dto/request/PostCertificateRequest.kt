@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.request

data class PostCertificateRequest(
    val relyingPartyName: String?,
    val relyingPartyUUID: String?,
)
