@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.exception

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.common.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NoInternetConnectionExceptionTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testLocalizedMessage_isCorrect() {
        val expectedMessage = context.getString(R.string.no_internet_connection)

        val exception = NoInternetConnectionException(context)
        val actualMessage = exception.localizedMessage

        assertEquals(expectedMessage, actualMessage)
    }
}
