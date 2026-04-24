/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.pin

import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN1_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PUK_MIN_LENGTH
import ee.ria.DigiDoc.idcard.CodeType

object PinCodeUtil {
    fun shouldShowPINCodeError(
        pinCode: ByteArray?,
        codeType: CodeType,
    ): Boolean = (pinCode != null && pinCode.isNotEmpty() && !isPINLengthValid(pinCode, codeType))

    fun isPINLengthValid(
        pinCode: ByteArray,
        codeType: CodeType,
    ): Boolean =
        when (codeType) {
            CodeType.PIN1 -> pinCode.size in PIN1_MIN_LENGTH..PIN_MAX_LENGTH
            CodeType.PIN2 -> pinCode.size in PIN2_MIN_LENGTH..PIN_MAX_LENGTH
            CodeType.PUK -> pinCode.size > PUK_MIN_LENGTH
        }
}
