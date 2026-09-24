// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.request

data class PostCertificateRequest(
    val relyingPartyName: String?,
    val relyingPartyUUID: String?,
)
