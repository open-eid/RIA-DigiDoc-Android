@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
class MobileSignatureResponse {
    @SerializedName("value")
    var value: String? = null

    @SerializedName("algorithm")
    var algorithm: String? = null

    override fun toString(): String {
        return "MobileSignatureResponse{" +
            "value='" + value + '\'' +
            ", algorithm='" + algorithm + '\'' +
            '}'
    }
}
