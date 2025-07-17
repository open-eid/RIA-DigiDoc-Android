@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

class PostMobileCreateSignatureSessionRequest {
    var relyingPartyName: String? = null
    var relyingPartyUUID: String? = null
    var phoneNumber: String? = null
    var nationalIdentityNumber: String? = null

    var hash: String? = null

    var hashType: String? = null
    var language: String? = null
    var displayText: String? = null
    var displayTextFormat: String? = null

    override fun toString(): String =
        "PostMobileCreateSignatureSessionRequest{" +
            "relyingPartyName='" + relyingPartyName + '\'' +
            ", relyingPartyUUID='" + relyingPartyUUID + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", nationalIdentityNumber='" + nationalIdentityNumber + '\'' +
            ", hash='" + hash + '\'' +
            ", hashType='" + hashType + '\'' +
            ", language='" + language + '\'' +
            ", displayText='" + displayText + '\'' +
            ", displayTextFormat='" + displayTextFormat + '\'' +
            '}'
}
