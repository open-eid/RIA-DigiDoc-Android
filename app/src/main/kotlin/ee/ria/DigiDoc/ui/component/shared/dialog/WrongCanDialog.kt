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

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun WrongCanDialog(onDismiss: (doNotShowAgain: Boolean) -> Unit) {
    var doNotShowAgain by remember { mutableStateOf(false) }

    val message = stringResource(R.string.signature_update_nfc_wrong_can_message)
    val url = stringResource(R.string.signature_update_nfc_wrong_can_url)
    val doNotShowAgainMessage = stringResource(R.string.do_not_show_again)
    val dialog = stringResource(R.string.dialog)

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val annotatedString =
        buildAnnotatedString {
            append(message)
            append("\n")
            pushLink(
                LinkAnnotation.Url(
                    url = url,
                    styles =
                        TextLinkStyles(
                            style =
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                ),
                        ),
                ),
            )
            append(stringResource(R.string.read_more_here))
            pop()
        }

    Dialog(onDismissRequest = { onDismiss(doNotShowAgain) }) {
        Surface(
            modifier = Modifier.semantics { paneTitle = dialog },
            shape = RoundedCornerShape(SPadding),
        ) {
            Column(modifier = Modifier.padding(MPadding)) {
                Text(
                    modifier =
                        Modifier
                            .padding(top = SPadding)
                            .focusRequester(focusRequester)
                            .focusable(),
                    text = annotatedString,
                )

                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides zeroPadding) {
                    Row(
                        modifier =
                            Modifier
                                .padding(top = SPadding)
                                .clickable { doNotShowAgain = !doNotShowAgain }
                                .semantics(mergeDescendants = true) {
                                    toggleableState = ToggleableState(doNotShowAgain)
                                    role = Role.Checkbox
                                },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            modifier =
                                Modifier.semantics {
                                    contentDescription = doNotShowAgainMessage
                                },
                            checked = doNotShowAgain,
                            onCheckedChange = null,
                        )
                        Text(
                            modifier =
                                Modifier
                                    .padding(horizontal = XSPadding)
                                    .notAccessible(),
                            text = doNotShowAgainMessage,
                        )
                    }
                }
                TextButton(
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .padding(top = MPadding),
                    onClick = { onDismiss(doNotShowAgain) },
                ) {
                    Text(stringResource(R.string.ok_button))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WrongCanDialogPreview() {
    RIADigiDocTheme {
        WrongCanDialog(
            onDismiss = {},
        )
    }
}
