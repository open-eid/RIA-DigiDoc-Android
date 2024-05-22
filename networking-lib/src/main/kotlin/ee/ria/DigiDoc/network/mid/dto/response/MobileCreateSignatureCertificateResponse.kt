@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType

@JsonIgnoreProperties(ignoreUnknown = true)
class MobileCreateSignatureCertificateResponse {
    @SerializedName("result")
    var result: MobileCertificateResultType? = null

    @SerializedName("cert")
    var cert: String? = null

    @SerializedName("time")
    var time: String? = null

    @SerializedName("traceId")
    var traceId: String? = null

    @SerializedName("error")
    var error: String? = null

    override fun toString(): String {
        return "MobileCreateSignatureCertificateResponse{" +
            "result=" + result +
            ", cert='" + cert + '\'' +
            ", time='" + time + '\'' +
            ", traceId='" + traceId + '\'' +
            ", error='" + error + '\'' +
            '}'
    }
}
