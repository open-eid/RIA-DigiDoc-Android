@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.request

import java.io.Serializable

data class SmartCreateSignatureRequest(
    val relyingPartyName: String?,
    var relyingPartyUUID: String?,
    val url: String?,
    val country: String?,
    val nationalIdentityNumber: String?,
    val containerPath: String?,
    val hashType: String?,
    val displayText: String?,
) : Serializable
