// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight

@Composable
fun StatusAnnouncer(message: String) {
    var announcement by remember { mutableStateOf("") }
    var isInitialMessage by remember { mutableStateOf(true) }

    LaunchedEffect(message) {
        if (isInitialMessage) {
            isInitialMessage = false
        } else {
            announcement = message
        }
    }

    Box(
        modifier =
            Modifier
                .size(invisibleElementHeight)
                .semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = announcement
                },
    )
}
