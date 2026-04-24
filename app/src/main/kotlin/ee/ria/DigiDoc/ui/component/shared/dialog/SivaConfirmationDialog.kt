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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import ee.ria.DigiDoc.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SivaConfirmationDialog(
    showDialog: MutableState<Boolean>,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit, showDialog) {
        if (showDialog.value) {
            focusRequester.requestFocus()
        }
    }

    if (showDialog.value) {
        ConfirmationDialog(
            modifier = modifier,
            showDialog = showDialog.value,
            text1 = R.string.siva_send_message_dialog,
            text2 = R.string.siva_continue_question,
            linkText = R.string.siva_read_here,
            linkUrl = R.string.siva_info_url,
            newLineBeforeText2 = true,
            onConfirm = {
                showDialog.value = false
                onResult(true)
            },
            onDismiss = {
                showDialog.value = false
                onResult(false)
            },
        )
    }
}
