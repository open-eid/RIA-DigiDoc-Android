@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AccessibilityUtilTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun accessibilityUtilTest_formatNumbers_success() {
        assertEquals("1 2 3 4 abc", AccessibilityUtil.formatNumbers("1234abc"))
        assertEquals("1 0 0 0", AccessibilityUtil.formatNumbers("1000"))
        assertEquals("abc", AccessibilityUtil.formatNumbers("abc"))
    }

    @Test
    fun accessibilityUtilTest_isTalkBackEnabled_success() {
        assertEquals(false, AccessibilityUtil.isTalkBackEnabled(context))
    }

    @Test
    fun accessibilityUtilTest_sendAccessibilityEvent_success() {
        // This is a void method, so we're just testing that it doesn't throw an exception
        AccessibilityUtil.sendAccessibilityEvent(context, 0, "Test")
    }
}
