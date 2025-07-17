@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

data class MobileCreateSignatureRequest(
    var relyingPartyName: String?,
    var relyingPartyUUID: String?,
    var url: String?,
    var phoneNumber: String?,
    var nationalIdentityNumber: String?,
    var containerPath: String?,
    var hashType: String?,
    var language: String?,
    var displayText: String?,
    var displayTextFormat: String?,
) {
    override fun toString(): String =
        "MobileCreateSignatureRequest{" +
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
