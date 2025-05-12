@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    showIcons: Boolean = true,
    @DrawableRes dismissIcon: Int = R.drawable.ic_m3_delete_48dp_wght400,
    @DrawableRes confirmIcon: Int = R.drawable.ic_m3_download_48dp_wght400,
    dismissButtonText: String,
    confirmButtonText: String,
    dismissButtonContentDescription: String,
    confirmButtonContentDescription: String,
    onDismissRequest: () -> Unit = {},
    onDismissButton: () -> Unit = {},
    onConfirmButton: () -> Unit = {},
) {
    val dismissIconResource = ImageVector.vectorResource(id = dismissIcon)
    val confirmIconResource = ImageVector.vectorResource(id = confirmIcon)

    val buttonName = stringResource(id = R.string.button_name)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = SPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    modifier =
                        modifier.weight(1f)
                            .semantics {
                                heading()
                                this.contentDescription = title.lowercase()
                                testTagsAsResourceId = true
                            }
                            .testTag("messageDialogTitleText"),
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = modifier,
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("messageDialogCancelIconButton"),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_close_48dp_wght400),
                        contentDescription = "${stringResource(R.string.cancel_button)} $buttonName",
                    )
                }
            }
        },
        text = {
            DynamicText(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics {
                            this.contentDescription = message.lowercase()
                            testTagsAsResourceId = true
                        }
                        .verticalScroll(rememberScrollState())
                        .testTag("messageDialogMessageText"),
                text = message,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissButton) {
                if (showIcons) {
                    Icon(
                        modifier =
                            modifier
                                .padding(XSPadding)
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("messageDialogDismissButton"),
                        imageVector = dismissIconResource,
                        contentDescription = "$dismissButtonContentDescription $buttonName",
                    )
                }
                Text(
                    modifier =
                        modifier
                            .semantics {
                                this.contentDescription = dismissButtonText.lowercase()
                                testTagsAsResourceId = true
                            }
                            .testTag("messageDialogDismissButtonText"),
                    text = dismissButtonText,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmButton) {
                if (showIcons) {
                    Icon(
                        modifier =
                            modifier
                                .padding(XSPadding)
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("messageDialogConfirmButton"),
                        imageVector = confirmIconResource,
                        contentDescription = "$confirmButtonContentDescription $buttonName",
                    )
                }
                Text(
                    modifier =
                        modifier
                            .semantics {
                                this.contentDescription = confirmButtonText.lowercase()
                                testTagsAsResourceId = true
                            }
                            .testTag("messageDialogConfirmButtonText"),
                    text = confirmButtonText,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MessageDialogPreview() {
    RIADigiDocTheme {
        MessageDialog(
            modifier = Modifier,
            title = "Dialog titles n ".repeat(5),
            message = "Dialog message",
            showIcons = true,
            dismissButtonText = "Cancel",
            confirmButtonText = "OK",
            dismissButtonContentDescription = "Cancel",
            confirmButtonContentDescription = "OK",
        )
    }
}
