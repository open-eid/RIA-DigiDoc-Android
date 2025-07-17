@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.annotations.SerializedName

@JsonIgnoreProperties(ignoreUnknown = true)
data class MobileSignatureResponse(
    @SerializedName("value")
    var value: String? = null,
    @SerializedName("algorithm")
    var algorithm: String? = null,
) {
    override fun toString(): String =
        "MobileSignatureResponse{" +
            "value='" + value + '\'' +
            ", algorithm='" + algorithm + '\'' +
            '}'
}
