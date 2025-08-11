@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.main

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.VerticalButtonColumn
import ee.ria.DigiDoc.ui.component.shared.VerticalButtonConfig
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CrashDialog(
    modifier: Modifier = Modifier,
    onDontSendClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onAlwaysSendClick: () -> Unit = {},
) {
    BasicAlertDialog(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("crashReportDialog"),
        onDismissRequest = onDontSendClick,
    ) {
        Surface(
            modifier =
                modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = modifier.padding(SPadding),
            ) {
                Box(
                    modifier = modifier.fillMaxWidth(),
                ) {
                    Text(
                        modifier =
                            modifier
                                .padding(vertical = SPadding)
                                .padding(top = XSPadding)
                                .align(Alignment.Center)
                                .testTag("crashReportDialogHeader"),
                        text = stringResource(R.string.crash_report_dialog_header),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                DynamicText(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = SPadding)
                            .testTag("crashReportDialogText"),
                    text = stringResource(R.string.crash_report_dialog_text),
                )
                VerticalButtonColumn(
                    modifier = modifier.testTag("crashReportDialogText"),
                    buttonConfigs =
                        listOf(
                            VerticalButtonConfig(
                                title = R.string.crash_report_dialog_send_button,
                                contentDescription =
                                    stringResource(
                                        R.string.crash_report_dialog_send_button,
                                    ).lowercase(),
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.background,
                                onClick = onSendClick,
                                testTag = "sendButton",
                            ),
                            VerticalButtonConfig(
                                title = R.string.crash_report_dialog_always_send_button,
                                isSubButton = true,
                                contentDescription =
                                    stringResource(
                                        R.string.crash_report_dialog_always_send_button,
                                    ).lowercase(),
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                onClick = onAlwaysSendClick,
                                testTag = "alwaysSendButton",
                            ),
                            VerticalButtonConfig(
                                title = R.string.crash_report_dialog_dont_send_button,
                                isSubButton = true,
                                contentDescription =
                                    stringResource(
                                        R.string.crash_report_dialog_dont_send_button,
                                    ).lowercase(),
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.error,
                                onClick = onDontSendClick,
                                testTag = "dontSendButton",
                            ),
                        ),
                )
            }
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CrashDialogPreview() {
    RIADigiDocTheme {
        CrashDialog()
    }
}
