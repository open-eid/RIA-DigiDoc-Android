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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.VerticalButtonColumn
import ee.ria.DigiDoc.ui.component.shared.VerticalButtonConfig
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashDialog(
    modifier: Modifier = Modifier,
    onDontSendClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onAlwaysSendClick: () -> Unit = {},
) {
    BasicAlertDialog(
        onDismissRequest = onDontSendClick,
    ) {
        Surface(
            modifier =
                modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(screenViewLargePadding)
                    .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = modifier.padding(screenViewLargePadding),
            ) {
                Box(
                    modifier = modifier.fillMaxWidth(),
                ) {
                    Text(
                        modifier =
                            modifier.padding(
                                start = screenViewLargePadding,
                                top = screenViewSmallPadding,
                                end = screenViewLargePadding,
                            )
                                .align(Alignment.Center),
                        text = stringResource(R.string.crash_report_dialog_header),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                DynamicText(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = screenViewLargePadding),
                    text = stringResource(R.string.crash_report_dialog_text),
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
                VerticalButtonColumn(
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
                            ),
                            VerticalButtonConfig(
                                title = R.string.crash_report_dialog_dont_send_button,
                                isSubButton = true,
                                contentDescription =
                                    stringResource(
                                        R.string.crash_report_dialog_dont_send_button,
                                    ).lowercase(),
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = Red500,
                                onClick = onDontSendClick,
                            ),
                        ),
                )
            }
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
