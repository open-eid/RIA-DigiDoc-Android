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

package ee.ria.DigiDoc.ui.component.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditValueDialog(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    editValue: TextFieldValue = TextFieldValue(""),
    onEditValueChange: (TextFieldValue) -> Unit = {},
    onClearValueClick: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }

    val buttonName = stringResource(id = R.string.button_name)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier =
            modifier
                .padding(MPadding)
                .fillMaxWidth()
                .testTag("editValueDialog"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            modifier =
                modifier
                    .size(MPadding)
                    .notAccessible(),
            tint = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = modifier.height(XSPadding))

        Text(
            modifier =
                modifier
                    .padding(
                        vertical = SPadding,
                    ).fillMaxWidth()
                    .semantics {
                        heading()
                        testTagsAsResourceId = true
                    }.testTag("editValueTitle"),
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        PrimaryTextField(
            modifier =
                Modifier
                    .padding(vertical = SPadding),
            value = editValue,
            onValueChange = { newValue ->
                onEditValueChange(newValue)
            },
            singleLine = true,
            label = subtitle,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Ascii,
                ),
            testTag = "editValueDialogTextField",
            removeIconTestTag = "editValueRemoveIconButton",
        )

        CancelAndOkButtonRow(
            modifier = modifier,
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.ok_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
            cancelButtonTestTag = "editValueDialogCancelButton",
            okButtonTestTag = "editValueDialogOkButton",
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditValueDialogPreview() {
    RIADigiDocTheme {
        EditValueDialog(
            title = "Change container name",
            editValue = TextFieldValue("some_File_name.pdf"),
            subtitle = "Container name",
        )
    }
}
