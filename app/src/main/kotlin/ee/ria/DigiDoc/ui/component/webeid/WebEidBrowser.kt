// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.webeid

import androidx.compose.ui.graphics.ImageBitmap

data class WebEidBrowser(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
)
