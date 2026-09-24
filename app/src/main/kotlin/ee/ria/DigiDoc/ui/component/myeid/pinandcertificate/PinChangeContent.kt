// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import ee.ria.DigiDoc.idcard.CodeType

data class PinChangeContent(
    val title: Int,
    val codeType: CodeType,
    val isForgottenPin: Boolean = false,
)
