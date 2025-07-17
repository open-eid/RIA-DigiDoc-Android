@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ee.ria.DigiDoc.common.DetailMessageSource
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType

@JsonIgnoreProperties(ignoreUnknown = true)
class RESTServiceFault : DetailMessageSource {
    private var httpStatus: Int = 0
    private var state: MobileCreateSignatureProcessState? = null
    private var time: String? = null
    private var traceId: String? = null
    var status: MobileCreateSignatureProcessStatus? = null
    var result: MobileCertificateResultType? = null
    var error: String? = null

    override var detailMessage: String? = null
        private set

    constructor(status: MobileCreateSignatureProcessStatus?) {
        this.status = status
    }

    constructor(
        status: MobileCreateSignatureProcessStatus?,
        detailMessage: String?,
    ) {
        this.status = status
        this.detailMessage = detailMessage
    }

    constructor(
        httpStatus: Int,
        state: MobileCreateSignatureProcessState?,
        status: MobileCreateSignatureProcessStatus?,
        time: String?,
        traceId: String?,
        error: String?,
    ) {
        this.httpStatus = httpStatus
        this.state = state
        this.status = status
        this.time = time
        this.traceId = traceId
        this.error = error
    }

    constructor(
        httpStatus: Int,
        result: MobileCertificateResultType?,
        time: String?,
        traceId: String?,
        error: String?,
    ) {
        this.httpStatus = httpStatus
        this.result = result
        this.time = time
        this.traceId = traceId
        this.error = error
    }

    override fun toString(): String =
        "RESTServiceFault{" +
            "httpStatus=" + httpStatus +
            ", state=" + state +
            ", status=" + status +
            ", result=" + result +
            ", time='" + time + '\'' +
            ", traceId='" + traceId + '\'' +
            ", error='" + error + '\'' +
            '}'
}
