@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

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
                    modifier = modifier.weight(1f),
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = modifier,
                ) {
                    Icon(
                        modifier = modifier.size(iconSizeXXS),
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_m3_close_48dp_wght400),
                        contentDescription = null,
                    )
                }
            }
        },
        text = {
            DynamicText(
                modifier = modifier.fillMaxWidth(),
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
                                .size(iconSizeXXS),
                        imageVector = dismissIconResource,
                        contentDescription = dismissButtonContentDescription,
                    )
                }
                Text(dismissButtonText)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmButton) {
                if (showIcons) {
                    Icon(
                        modifier =
                            modifier
                                .padding(XSPadding)
                                .size(iconSizeXXS),
                        imageVector = confirmIconResource,
                        contentDescription = confirmButtonContentDescription,
                    )
                }
                Text(confirmButtonText)
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
