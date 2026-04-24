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
fun ThreeButtonMenu(
    modifier: Modifier = Modifier,
    @StringRes firstButtonStringRes: Int,
    @StringRes secondButtonStringRes: Int,
    @StringRes thirdButtonStringRes: Int,
    @StringRes firstButtonStringResContentDescription: Int,
    @StringRes secondButtonStringResContentDescription: Int,
    @StringRes thirdButtonStringResContentDescription: Int,
    @DrawableRes firstButtonIcon: Int,
    @DrawableRes secondButtonIcon: Int,
    @DrawableRes thirdButtonIcon: Int,
    firstButtonClick: () -> Unit = {},
    secondButtonClick: () -> Unit = {},
    thirdButtonClick: () -> Unit = {},
    isFirstButtonVisible: Boolean = true,
    isSecondButtonVisible: Boolean = true,
    isThirdButtonVisible: Boolean = true,
    firstButtonTestTag: String,
    secondButtonTestTag: String,
    thirdButtonTestTag: String,
) {
    if (isFirstButtonVisible) {
        MenuButton(
            modifier = modifier,
            buttonStringRes = firstButtonStringRes,
            buttonIcon = firstButtonIcon,
            buttonStringResContentDescription = firstButtonStringResContentDescription,
            buttonClick = firstButtonClick,
            testTag = firstButtonTestTag,
        )
    }
    if (isSecondButtonVisible) {
        MenuButton(
            modifier = modifier,
            buttonStringRes = secondButtonStringRes,
            buttonIcon = secondButtonIcon,
            buttonStringResContentDescription = secondButtonStringResContentDescription,
            buttonClick = secondButtonClick,
            testTag = secondButtonTestTag,
        )
    }
    if (isThirdButtonVisible) {
        MenuButton(
            modifier = modifier,
            buttonStringRes = thirdButtonStringRes,
            buttonIcon = thirdButtonIcon,
            buttonStringResContentDescription = thirdButtonStringResContentDescription,
            buttonClick = thirdButtonClick,
            testTag = thirdButtonTestTag,
        )
    }
}
