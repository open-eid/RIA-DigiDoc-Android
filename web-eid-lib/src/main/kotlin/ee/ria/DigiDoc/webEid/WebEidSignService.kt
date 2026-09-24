// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import org.json.JSONObject

interface WebEidSignService {
    fun buildCertificatePayload(signingCert: ByteArray): JSONObject

    fun buildSignPayload(
        signingCert: String,
        signature: ByteArray,
        hashFunction: String,
    ): JSONObject
}
