// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
