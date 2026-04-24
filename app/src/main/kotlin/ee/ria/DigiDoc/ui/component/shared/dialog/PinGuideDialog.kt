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

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.ui.component.shared.MessageDialog
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PinGuideDialog(
    modifier: Modifier = Modifier,
    showDialog: MutableState<Boolean>,
    pinChangeVariant: PinChangeVariant,
    @StringRes title: Int,
    titleExtra: String = "",
    guidelines: String,
    @StringRes confirmButton: Int,
    confirmButtonExtra: String = "",
    onResult: (Boolean, PinChangeVariant) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    val titleText = stringResource(title, titleExtra)
    val dismissButtonText = stringResource(R.string.close_button)
    val confirmButtonText = stringResource(confirmButton, confirmButtonExtra)

    LaunchedEffect(Unit, showDialog) {
        if (showDialog.value) {
            focusRequester.requestFocus()
        }
    }

    if (showDialog.value) {
        BasicAlertDialog(
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("pinGuideDialogDialog"),
            onDismissRequest = {
                showDialog.value = false
            },
        ) {
            Surface(
                modifier =
                    modifier
                        .padding(SPadding)
                        .verticalScroll(rememberScrollState())
                        .testTag("pinGuideDialog"),
            ) {
                MessageDialog(
                    modifier = modifier,
                    title = titleText,
                    message = guidelines,
                    showIcons = false,
                    dismissButtonText = dismissButtonText,
                    confirmButtonText = confirmButtonText,
                    dismissButtonContentDescription = dismissButtonText,
                    confirmButtonContentDescription = confirmButtonText,
                    onDismissRequest = {
                        showDialog.value = false
                    },
                    onDismissButton = {
                        showDialog.value = false
                        onResult(false, pinChangeVariant)
                    },
                    onConfirmButton = {
                        showDialog.value = true
                        onResult(true, pinChangeVariant)
                    },
                )
            }
        }
    }
}
