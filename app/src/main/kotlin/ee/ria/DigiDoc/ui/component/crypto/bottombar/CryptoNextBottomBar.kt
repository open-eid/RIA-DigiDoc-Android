@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottombar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.signing.bottombar.ShareButtonBottomBar
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CryptoNextBottomBar(
    modifier: Modifier,
    isNoRecipientContainer: Boolean,
    onNextClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onAddMoreFiles: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("cryptoBottomBar"),
    ) {
        if (isNoRecipientContainer) {
            NoRecipientContainerBottomBar(
                modifier = modifier,
                leftButtonText = stringResource(R.string.documents_add_button),
                leftButtonContentDescription = stringResource(R.string.documents_add_button),
                leftButtonIcon = R.drawable.ic_m3_add_48dp_wght400,
                onLeftButtonClick = onAddMoreFiles,
                rightButtonText = stringResource(R.string.next_button),
                rightButtonContentDescription = stringResource(R.string.next_button),
                rightButtonIcon = R.drawable.ic_m3_arrow_forward_48dp_wght400,
                onRightButtonClick = onNextClick,
            )
        } else {
            ShareButtonBottomBar(
                modifier = modifier,
                shareButtonIcon = R.drawable.ic_m3_ios_share_48dp_wght400,
                shareButtonName = R.string.share_button,
                shareButtonContentDescription = R.string.share_button_accessibility,
                onShareButtonClick = onShareClick,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CryptoNextBottomBarPreview() {
    RIADigiDocTheme {
        CryptoNextBottomBar(
            modifier = Modifier,
            isNoRecipientContainer = true,
        )

        CryptoNextBottomBar(
            modifier = Modifier,
            isNoRecipientContainer = false,
        )

        CryptoNextBottomBar(
            modifier = Modifier,
            isNoRecipientContainer = false,
        )
    }
}
