@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun HrefMessageDialog(
    modifier: Modifier = Modifier,
    @StringRes text1: Int,
    @StringRes text1Arg: Int? = null,
    @StringRes text2: Int?,
    @StringRes linkText: Int,
    @StringRes linkUrl: Int,
    showLinkOnOneLine: Boolean = false,
    showCancelButton: Boolean = true,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(screenViewLargePadding),
    ) {
        HrefDynamicText(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = screenViewLargePadding),
            text1 = stringResource(text1, text1Arg?.let { stringResource(it) } ?: ""),
            text2 = text2?.let { stringResource(it) } ?: "",
            linkText = stringResource(linkText),
            linkUrl = stringResource(linkUrl),
            showLinkOnOneLine = showLinkOnOneLine,
            textStyle = MaterialTheme.typography.bodyLarge,
        )
        CancelAndOkButtonRow(
            okButtonTestTag = "hrefMessageDialogOkButton",
            cancelButtonTestTag = "hrefMessageDialogCancelButton",
            cancelButtonClick = cancelButtonClick,
            okButtonClick = okButtonClick,
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.ok_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
            showCancelButton = showCancelButton,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HrefMessageDialogPreview() {
    RIADigiDocTheme {
        HrefMessageDialog(
            text1 = R.string.main_diagnostics_restart_message,
            text2 = R.string.main_diagnostics_restart_message_restart_now,
            linkText = R.string.main_diagnostics_restart_message_read_more,
            linkUrl = R.string.main_diagnostics_restart_message_href,
        )
    }
}
