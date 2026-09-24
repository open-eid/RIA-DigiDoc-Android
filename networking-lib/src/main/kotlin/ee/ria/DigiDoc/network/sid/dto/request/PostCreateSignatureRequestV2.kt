// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.dto.request

data class PostCreateSignatureRequestV2(
    val relyingPartyName: String?,
    val relyingPartyUUID: String?,
    val hash: String?,
    val hashType: String?,
    val allowedInteractionsOrder: List<RequestAllowedInteractionsOrder>?,
)
