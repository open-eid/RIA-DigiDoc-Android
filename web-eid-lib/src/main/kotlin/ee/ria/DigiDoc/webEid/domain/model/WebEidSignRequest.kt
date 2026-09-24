// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

import java.security.cert.X509Certificate

data class WebEidSignRequest(
    val responseUri: String,
    val origin: String,
    val signingCertificate: X509Certificate,
    val hash: String?,
    val hashFunction: String?,
    val personalData: WebEidPersonalData?,
)
