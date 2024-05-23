@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.request

data class PostCreateSignatureRequestV2(
    val relyingPartyName: String?,
    val relyingPartyUUID: String?,
    val hash: String?,
    val hashType: String?,
    val allowedInteractionsOrder: List<RequestAllowedInteractionsOrder>?,
)
