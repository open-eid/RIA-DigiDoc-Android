/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.accessibility

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
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
        assertEquals("1 2 3 4 abc", formatNumbers("1234abc"))
        assertEquals("1 0 0 0", formatNumbers("1000"))
        assertEquals("abc", formatNumbers("abc"))
    }

    @Test
    fun accessibilityUtilTest_isTalkBackEnabled_success() {
        assertEquals(false, isTalkBackEnabled(context))
    }

    @Test
    fun accessibilityUtilTest_sendAccessibilityEvent_success() {
        // This is a void method, so we're just testing that it doesn't throw an exception
        sendAccessibilityEvent(context, "Test")
    }
}
