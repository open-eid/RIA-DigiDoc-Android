@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun CancelAndOkButtonRow(
    modifier: Modifier = Modifier,
    showCancelButton: Boolean = true,
    cancelButtonEnabled: Boolean = true,
    okButtonEnabled: Boolean = true,
    cancelButtonClick: () -> Unit = {},
    okButtonClick: () -> Unit = {},
    @StringRes cancelButtonTitle: Int,
    @StringRes okButtonTitle: Int,
    cancelButtonContentDescription: String,
    okButtonContentDescription: String,
    okButtonTestTag: String = "",
    cancelButtonTestTag: String = "",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = XSPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (showCancelButton) {
            Spacer(modifier = modifier.weight(1f).padding(XSPadding))
            PrimaryButton(
                enabled = cancelButtonEnabled,
                modifier =
                    modifier
                        .weight(1f)
                        .testTag(cancelButtonTestTag),
                title = cancelButtonTitle,
                contentDescription = cancelButtonContentDescription,
                onClickItem = cancelButtonClick,
            )
            Spacer(modifier = modifier.padding(XSPadding))
        }
        PrimaryButton(
            enabled = okButtonEnabled,
            modifier =
                modifier
                    .weight(1f)
                    .testTag(okButtonTestTag),
            title = okButtonTitle,
            contentDescription = okButtonContentDescription,
            onClickItem = okButtonClick,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CancelAndOkButtonRowPreview() {
    RIADigiDocTheme {
        CancelAndOkButtonRow(
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button),
            okButtonContentDescription = stringResource(id = R.string.sign_button),
            showCancelButton = true,
        )
    }
}
