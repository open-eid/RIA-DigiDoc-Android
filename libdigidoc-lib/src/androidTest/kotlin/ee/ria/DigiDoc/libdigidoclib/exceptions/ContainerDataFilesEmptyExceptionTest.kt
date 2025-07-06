@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.exceptions

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ContainerDataFilesEmptyExceptionTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun testLocalizedMessage_isNull() {
        val exception = ContainerDataFilesEmptyException()
        val actualMessage = exception.localizedMessage

        assertNull(actualMessage)
    }
}
