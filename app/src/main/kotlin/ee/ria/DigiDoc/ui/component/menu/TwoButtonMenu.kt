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

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TwoButtonMenu(
    modifier: Modifier = Modifier,
    @StringRes firstButtonStringRes: Int,
    @StringRes secondButtonStringRes: Int,
    @StringRes firstButtonStringResContentDescription: Int,
    @StringRes secondButtonStringResContentDescription: Int,
    @DrawableRes firstButtonIcon: Int,
    @DrawableRes secondButtonIcon: Int,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    firstButtonTestTag: String,
    secondButtonTestTag: String,
) {
    MenuButton(
        modifier = modifier,
        buttonStringRes = firstButtonStringRes,
        buttonIcon = firstButtonIcon,
        buttonStringResContentDescription = firstButtonStringResContentDescription,
        buttonClick = firstButtonClick,
        testTag = firstButtonTestTag,
    )
    MenuButton(
        modifier = modifier,
        buttonStringRes = secondButtonStringRes,
        buttonIcon = secondButtonIcon,
        buttonStringResContentDescription = secondButtonStringResContentDescription,
        buttonClick = secondButtonClick,
        testTag = secondButtonTestTag,
    )
}
