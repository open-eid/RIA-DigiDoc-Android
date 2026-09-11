// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.window

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

object WindowUtil {
    private val compactHeightBreakpoint = 480.dp
    private val mediumWidthBreakpoint = 600.dp

    fun isCompactLandscapeWindow(windowSize: DpSize): Boolean =
        windowSize.height < compactHeightBreakpoint && windowSize.width >= mediumWidthBreakpoint
}
