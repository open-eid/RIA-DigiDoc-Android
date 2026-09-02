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

package ee.ria.DigiDoc.utils.window

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowUtilTest {
    @Test
    fun windowUtil_isCompactLandscapeWindow_phoneLandscapeReturnsTrue() {
        assertTrue(WindowUtil.isCompactLandscapeWindow(DpSize(915.dp, 411.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_phonePortraitReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(411.dp, 915.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_phonePortraitSplitScreenReturnsFalse() {
        // Landscape-shaped but far too narrow for a single-line title
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(411.dp, 400.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_phoneLandscapeSideBySideSplitReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(453.dp, 412.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_tabletLandscapeReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(1280.dp, 800.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_tabletPortraitReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(800.dp, 1280.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_smallestDisplaySizePhoneLandscapeReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(1075.dp, 484.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_heightExactlyAtBreakpointReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(915.dp, 480.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_widthExactlyAtBreakpointReturnsTrue() {
        assertTrue(WindowUtil.isCompactLandscapeWindow(DpSize(600.dp, 479.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_widthJustBelowBreakpointReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize(599.dp, 479.dp)))
    }

    @Test
    fun windowUtil_isCompactLandscapeWindow_zeroSizeReturnsFalse() {
        assertFalse(WindowUtil.isCompactLandscapeWindow(DpSize.Zero))
    }
}
