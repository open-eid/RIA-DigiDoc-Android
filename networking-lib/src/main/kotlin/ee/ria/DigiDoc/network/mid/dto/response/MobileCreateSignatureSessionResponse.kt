@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
data class MobileCreateSignatureSessionResponse(
    @SerializedName("sessionID")
    var sessionID: String? = null,
    @SerializedName("traceId")
    var traceId: String? = null,
    @SerializedName("time")
    var time: String? = null,
    @SerializedName("error")
    var error: String? = null,
) {
    override fun toString(): String {
        return "MobileCreateSignatureSessionResponse{" +
            "sessionID='" + sessionID + '\'' +
            ", traceId='" + traceId + '\'' +
            ", time='" + time + '\'' +
            ", error='" + error + '\'' +
            '}'
    }
}
