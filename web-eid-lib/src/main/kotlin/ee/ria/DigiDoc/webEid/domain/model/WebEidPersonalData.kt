// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.domain.model

data class WebEidPersonalData(
    val givenNames: String,
    val surname: String,
    val personalCode: String,
)
