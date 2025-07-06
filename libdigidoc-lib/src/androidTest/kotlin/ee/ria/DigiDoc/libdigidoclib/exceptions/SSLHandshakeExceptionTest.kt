@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SSLHandshakeExceptionTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testLocalizedMessage_isCorrect() {
        val expectedMessage = context.getString(R.string.invalid_ssl_handshake)

        val exception = SSLHandshakeException(context)
        val actualMessage = exception.localizedMessage

        assertEquals(expectedMessage, actualMessage)
    }
}
