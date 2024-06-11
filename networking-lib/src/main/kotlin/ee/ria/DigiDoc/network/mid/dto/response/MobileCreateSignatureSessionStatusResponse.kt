@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
data class MobileCreateSignatureSessionStatusResponse(
    @SerializedName("state")
    var state: MobileCreateSignatureProcessState? = null,
    @SerializedName("result")
    var result: MobileCreateSignatureProcessStatus? = null,
    @SerializedName("signature")
    var signature: MobileSignatureResponse? = null,
    @SerializedName("cert")
    var cert: String? = null,
    @SerializedName("time")
    var time: String? = null,
    @SerializedName("traceId")
    var traceId: String? = null,
    @SerializedName("error")
    var error: String? = null,
) {
    override fun toString(): String {
        return "MobileCreateSignatureSessionStatusResponse{" +
            "state=" + state +
            ", result=" + result +
            ", signature=" + signature +
            ", cert='" + cert + '\'' +
            ", time='" + time + '\'' +
            ", traceId='" + traceId + '\'' +
            ", error='" + error + '\'' +
            '}'
    }
}
