// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid

import org.json.JSONObject

interface WebEidAuthService {
    fun buildAuthToken(
        authCert: ByteArray,
        signingCert: ByteArray?,
        signature: ByteArray,
    ): JSONObject
}
