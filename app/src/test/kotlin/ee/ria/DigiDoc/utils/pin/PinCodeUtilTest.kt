@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.pin

import ee.ria.DigiDoc.idcard.CodeType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCodeUtilTest {
    @Test
    fun pinCodeUtil_shouldShowPIN1CodeError_false() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN1)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_isPIN1CodeValid_shouldShowNullReturnFalse() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(null, CodeType.PIN1)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_isPIN1CodeValid_shouldShowEmptyReturnFalse() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(), CodeType.PIN1)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_shouldShowPIN1CodeError_returnTrueMinLength() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(1, 1, 5), CodeType.PIN1)
            assertTrue(result)
        }

    @Test
    fun pinCodeUtil_shouldShowPIN1CodeError_returnTrueMaxLength() =
        runTest {
            val result =
                PinCodeUtil
                    .shouldShowPINCodeError(
                        byteArrayOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7),
                        CodeType.PIN1,
                    )
            assertTrue(result)
        }

    @Test
    fun pinCodeUtil_shouldShowPIN2CodeError_false() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(1, 1, 5, 5, 5), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_isPIN2CodeValid_shouldShowNullReturnFalse() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(null, CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_isPIN2CodeValid_shouldShowEmptyReturnFalse() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(), CodeType.PIN2)
            assertFalse(result)
        }

    @Test
    fun pinCodeUtil_shouldShowPIN2CodeError_returnTrueMinLength() =
        runTest {
            val result = PinCodeUtil.shouldShowPINCodeError(byteArrayOf(1, 1, 5, 5), CodeType.PIN2)
            assertTrue(result)
        }

    @Test
    fun pinCodeUtil_shouldShowPIN2CodeError_returnTrueMaxLength() =
        runTest {
            val result =
                PinCodeUtil
                    .shouldShowPINCodeError(
                        byteArrayOf(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7),
                        CodeType.PIN2,
                    )
            assertTrue(result)
        }

    @Test
    fun pinCodeUtil_isPINLengthValid_pin1ValidLengths() {
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(4), CodeType.PIN1))
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(12), CodeType.PIN1))
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(7), CodeType.PIN1))
    }

    @Test
    fun pinCodeUtil_isPINLengthValid_pin1InvalidLengths() {
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(3), CodeType.PIN1))
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(13), CodeType.PIN1))
    }

    @Test
    fun pinCodeUtil_isPINLengthValid_pin2ValidLengths() {
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(5), CodeType.PIN2))
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(12), CodeType.PIN2))
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(8), CodeType.PIN2))
    }

    @Test
    fun pinCodeUtil_isPINLengthValid_pin2InvalidLengths() {
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(4), CodeType.PIN2))
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(13), CodeType.PIN2))
    }

    @Test
    fun pinCodeUtil_isPINLengthValid_pukValidLengths() {
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(9), CodeType.PUK))
        assertTrue(PinCodeUtil.isPINLengthValid(ByteArray(15), CodeType.PUK))
    }

    @Test
    fun pinCodeUtil_isPINLengthValid_pukInvalidLengths() {
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(8), CodeType.PUK))
        assertFalse(PinCodeUtil.isPINLengthValid(ByteArray(3), CodeType.PUK))
    }
}
