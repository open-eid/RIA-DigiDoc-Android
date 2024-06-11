@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class VerificationCodeUtilTest {
    @Test
    fun verificationCodeUtil_calculateMobileIdVerificationCode_returnHash() {
        val hash = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        val result = VerificationCodeUtil.calculateSmartIdVerificationCode(hash)
        assertEquals("4377", result)
    }

    @Test
    fun verificationCodeUtil_calculateMobileIdVerificationCode_ifHashNullReturnNullString() {
        val result = VerificationCodeUtil.calculateSmartIdVerificationCode(null)
        assertEquals("null", result)
    }
}
