@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.bottombar

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
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SigningBottomBar(
    modifier: Modifier,
    isUnsignedContainer: Boolean,
    onSignClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onAddMoreFiles: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signingBottomBar"),
    ) {
        if (isUnsignedContainer) {
            UnSignedContainerBottomBar(
                modifier = modifier,
                leftButtonText = stringResource(R.string.documents_add_button),
                leftButtonContentDescription = stringResource(R.string.documents_add_button),
                leftButtonIcon = R.drawable.ic_m3_add_48dp_wght400,
                onLeftButtonClick = onAddMoreFiles,
                rightButtonText = stringResource(R.string.sign_button),
                rightButtonContentDescription = stringResource(R.string.sign_button),
                rightButtonIcon = R.drawable.ic_m3_stylus_note_48dp_wght400,
                onRightButtonClick = onSignClick,
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
fun SigningBottomBarPreview() {
    RIADigiDocTheme {
        SigningBottomBar(
            modifier = Modifier,
            isUnsignedContainer = true,
        )

        SigningBottomBar(
            modifier = Modifier,
            isUnsignedContainer = false,
        )
    }
}
