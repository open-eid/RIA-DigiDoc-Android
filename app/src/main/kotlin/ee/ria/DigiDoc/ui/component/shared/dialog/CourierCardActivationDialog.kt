// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CourierCardActivationDialog(
    modifier: Modifier = Modifier,
    message: Int = R.string.id_card_courier_must_activate_to_sign,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Box(modifier = modifier.fillMaxSize()) {
        BasicAlertDialog(
            modifier =
                Modifier
                    .clip(buttonRoundCornerShape)
                    .background(MaterialTheme.colorScheme.surface),
            onDismissRequest = onDismiss,
        ) {
            LaunchedEffect(Unit) {
                delay(100)
                focusRequester.requestFocus()
            }
            Surface(
                modifier =
                    Modifier
                        .padding(SPadding)
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier =
                        Modifier
                            .semantics { testTagsAsResourceId = true }
                            .testTag("courierCardActivationDialogContainer"),
                ) {
                    Text(
                        modifier =
                            Modifier
                                .padding(SPadding)
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .focusable(),
                        text = stringResource(message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start,
                    )
                    CancelAndOkButtonRow(
                        okButtonTestTag = "courierCardDialogOkButton",
                        cancelButtonTestTag = "courierCardDialogCancelButton",
                        cancelButtonClick = {},
                        okButtonClick = onDismiss,
                        cancelButtonTitle = R.string.cancel_button,
                        okButtonTitle = R.string.ok_button,
                        cancelButtonContentDescription = stringResource(R.string.cancel_button).lowercase(),
                        okButtonContentDescription = stringResource(R.string.ok_button).lowercase(),
                        showCancelButton = false,
                    )
                }
            }
        }
        InvisibleElement(modifier = Modifier)
    }
}
