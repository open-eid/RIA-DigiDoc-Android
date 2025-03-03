@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
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
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    val dismissIconResource = ImageVector.vectorResource(id = dismissIcon)
    val confirmIconResource = ImageVector.vectorResource(id = confirmIcon)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            DynamicText(
                modifier =
                    modifier
                        .fillMaxWidth(),
                text = message,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
            TextButton(onClick = onConfirm) {
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
            title = "Dialog title",
            message = "Dialog message",
            showIcons = true,
            dismissButtonText = "Cancel",
            confirmButtonText = "OK",
            dismissButtonContentDescription = "Cancel",
            confirmButtonContentDescription = "OK",
        )
    }
}
