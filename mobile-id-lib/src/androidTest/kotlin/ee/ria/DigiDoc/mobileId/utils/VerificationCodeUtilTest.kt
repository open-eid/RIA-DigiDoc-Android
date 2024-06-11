@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class VerificationCodeUtilTest {
    @Test
    fun verificationCodeUtil_calculateMobileIdVerificationCode_return0000() {
        val hash = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        val result = VerificationCodeUtil.calculateMobileIdVerificationCode(hash)
        assertEquals("0000", result)
    }

    @Test
    fun verificationCodeUtil_calculateMobileIdVerificationCode_ifHashNullReturn0000() {
        val result = VerificationCodeUtil.calculateMobileIdVerificationCode(null)
        assertEquals("0000", result)
    }
}
