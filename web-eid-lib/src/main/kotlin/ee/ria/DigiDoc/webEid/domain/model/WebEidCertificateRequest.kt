// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

data class WebEidCertificateRequest(
    val responseUri: String,
    val origin: String,
)
