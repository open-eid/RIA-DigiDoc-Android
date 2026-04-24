/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
