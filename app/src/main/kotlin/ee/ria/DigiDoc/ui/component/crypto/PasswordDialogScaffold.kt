// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
