@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

import ee.ria.cdoc.CDocException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class WrongPasswordExceptionTest {
    @Test
    fun wrongPasswordException_exceptionMessage_passwordIsWrong() {
        val cause = makeCDocException()
        val exception = WrongPasswordException(cause)
        assertEquals("Wrong password", exception.message)
    }

    @Test
    fun wrongPasswordException_cause_isCDocException() {
        val cause = makeCDocException()
        val exception = WrongPasswordException(cause)
        assertSame(cause, exception.cause)
    }

    @Test
    fun wrongPasswordException_isCryptoException() {
        val cause = makeCDocException()
        val exception = WrongPasswordException(cause)
        assertTrue(CryptoException::class.java.isAssignableFrom(exception.javaClass))
    }

    @Test
    fun wrongPasswordException_isException() {
        val cause = makeCDocException()
        val exception = WrongPasswordException(cause)
        assertTrue(Exception::class.java.isAssignableFrom(exception.javaClass))
    }

    @Test
    fun wrongPasswordException_defaultConstructor_messageIsWrongPassword() {
        val exception = WrongPasswordException()
        assertEquals("Wrong password", exception.message)
    }

    private fun makeCDocException(
        code: Int = -109,
        message: String = "Wrong key",
    ): CDocException {
        val constructor = CDocException::class.java.getDeclaredConstructor(Int::class.java, String::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(code, message) as CDocException
    }
}
