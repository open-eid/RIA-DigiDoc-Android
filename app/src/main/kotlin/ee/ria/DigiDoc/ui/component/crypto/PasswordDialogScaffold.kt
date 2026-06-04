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

package ee.ria.DigiDoc.ui.component.crypto

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding

@Composable
fun PasswordDialogScaffold(
    modifier: Modifier = Modifier,
    title: String,
    @StringRes okButtonTitle: Int,
    okButtonEnabled: Boolean,
    onDismiss: () -> Unit,
    onOkButtonClick: () -> Unit,
    cancelButtonTestTag: String,
    okButtonTestTag: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    modifier
                        .padding(MPadding)
                        .fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = modifier.height(MPadding))

                content()

                Spacer(modifier = modifier.height(MPadding))

                CancelAndOkButtonRow(
                    modifier = modifier,
                    cancelButtonClick = onDismiss,
                    okButtonClick = onOkButtonClick,
                    okButtonEnabled = okButtonEnabled,
                    cancelButtonTitle = R.string.cancel_button,
                    okButtonTitle = okButtonTitle,
                    cancelButtonContentDescription =
                        stringResource(R.string.cancel_button).lowercase(),
                    okButtonContentDescription =
                        stringResource(okButtonTitle).lowercase(),
                    cancelButtonTestTag = cancelButtonTestTag,
                    okButtonTestTag = okButtonTestTag,
                )
            }
        }
    }
}
