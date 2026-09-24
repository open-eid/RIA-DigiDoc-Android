// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.dto.request

class GetMobileCreateSignatureSessionStatusRequest(
    var sessionId: String,
) {
    var timeoutMs: String = "1000"

    override fun toString(): String =
        "GetMobileCreateSignatureSessionStatusRequest{" +
            "sessionId='" + sessionId + '\'' +
            ", timeoutMs='" + timeoutMs + '\'' +
            '}'
}
