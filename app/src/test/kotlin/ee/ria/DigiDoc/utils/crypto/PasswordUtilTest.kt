@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.crypto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordUtilTest {
    @Test
    fun passwordUtil_isPasswordValid_returnsTrueWithValidPassword() {
        assertTrue(PasswordUtil.isPasswordValid("Abcdefghij1234567890"))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsTrueWithMaximumLength() {
        assertTrue(PasswordUtil.isPasswordValid("Aa1" + "x".repeat(61)))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWhenTooShort() {
        assertFalse(PasswordUtil.isPasswordValid("Abcdefghij123456789"))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWhenTooLong() {
        assertFalse(PasswordUtil.isPasswordValid("Aa1" + "x".repeat(62)))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWhenMissingDigit() {
        assertFalse(PasswordUtil.isPasswordValid("Abcdefghijklmnopqrst"))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWhenMissingUppercase() {
        assertFalse(PasswordUtil.isPasswordValid("abcdefghij1234567890"))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWhenMissingLowercase() {
        assertFalse(PasswordUtil.isPasswordValid("ABCDEFGHIJ1234567890"))
    }

    @Test
    fun passwordUtil_isPasswordValid_returnsFalseWithEmptyPassword() {
        assertFalse(PasswordUtil.isPasswordValid(""))
    }
}
