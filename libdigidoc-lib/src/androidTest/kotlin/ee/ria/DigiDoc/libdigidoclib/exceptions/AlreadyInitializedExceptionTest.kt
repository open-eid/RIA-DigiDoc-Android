@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AlreadyInitializedExceptionTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testLocalizedMessage_isCorrect() {
        val expectedMessage = "Test message for signing cancellation"

        val exception = AlreadyInitializedException(expectedMessage)
        val actualMessage = exception.localizedMessage

        assertEquals(expectedMessage, actualMessage)
    }
}
