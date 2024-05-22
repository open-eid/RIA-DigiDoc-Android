@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

class GetMobileCreateSignatureSessionStatusRequest(var sessionId: String) {
    var timeoutMs: String = "1000"

    override fun toString(): String {
        return "GetMobileCreateSignatureSessionStatusRequest{" +
            "sessionId='" + sessionId + '\'' +
            ", timeoutMs='" + timeoutMs + '\'' +
            '}'
    }
}
