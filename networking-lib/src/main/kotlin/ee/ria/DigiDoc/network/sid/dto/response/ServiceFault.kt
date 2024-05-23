@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.response

import com.google.gson.Gson
import ee.ria.DigiDoc.common.DetailMessageSource

class ServiceFault : DetailMessageSource {
    val status: SessionStatusResponseProcessStatus?
    override val detailMessage: String?

    constructor(status: SessionStatusResponseProcessStatus) {
        this.status = status
        this.detailMessage = null
    }

    constructor(status: SessionStatusResponseProcessStatus, detailMessage: String?) {
        this.status = status
        this.detailMessage = detailMessage
    }

    companion object {
        fun toJson(fault: ServiceFault?): String {
            return Gson().toJson(fault)
        }

        fun fromJson(json: String?): ServiceFault {
            return Gson().fromJson(json, ServiceFault::class.java)
        }
    }
}
