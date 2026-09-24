// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

data class WebEidAuthRequest(
    val challenge: String,
    val loginUri: String,
    val getSigningCertificate: Boolean,
    val origin: String,
)
