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
