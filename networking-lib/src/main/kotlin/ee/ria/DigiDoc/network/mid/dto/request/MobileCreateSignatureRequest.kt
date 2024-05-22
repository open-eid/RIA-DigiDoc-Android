@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

class MobileCreateSignatureRequest {
    var relyingPartyName: String? = null
    var relyingPartyUUID: String? = null
    var url: String? = null
    var phoneNumber: String? = null
    var nationalIdentityNumber: String? = null

    var containerPath: String? = null

    var hashType: String? = null
    var language: String? = null
    var displayText: String? = null
    var displayTextFormat: String? = null

    override fun toString(): String {
        return "MobileCreateSignatureRequest{" +
            "relyingPartyName='" + relyingPartyName + '\'' +
            ", relyingPartyUUID='" + relyingPartyUUID + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", nationalIdentityNumber='" + nationalIdentityNumber + '\'' +
            ", containerPath='" + containerPath + '\'' +
            ", hashType='" + hashType + '\'' +
            ", language='" + language + '\'' +
            ", displayText='" + displayText + '\'' +
            ", displayTextFormat='" + displayTextFormat + '\'' +
            '}'
    }
}
