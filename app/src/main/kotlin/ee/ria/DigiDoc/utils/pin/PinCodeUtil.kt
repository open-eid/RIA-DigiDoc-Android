// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.pin

import ee.ria.DigiDoc.idcard.CodeType

object PinCodeUtil {
    fun shouldShowPINCodeError(
        pinCode: ByteArray?,
        codeType: CodeType,
    ): Boolean = (pinCode != null && pinCode.isNotEmpty() && !isPINLengthValid(pinCode, codeType))

    fun isPINLengthValid(
        pinCode: ByteArray,
        codeType: CodeType,
    ): Boolean = codeType.isLengthValid(pinCode.size)
}
