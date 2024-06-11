@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

data class PostMobileCreateSignatureCertificateRequest(
    var relyingPartyName: String? = null,
    var relyingPartyUUID: String? = null,
    var phoneNumber: String? = null,
    var nationalIdentityNumber: String? = null,
) {
    override fun toString(): String {
        return "GetMobileCertificateRequest{" +
            "relyingPartyName='" + relyingPartyName + '\'' +
            ", relyingPartyUUID='" + relyingPartyUUID + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", nationalIdentityNumber='" + nationalIdentityNumber + '\'' +
            '}'
    }
}
