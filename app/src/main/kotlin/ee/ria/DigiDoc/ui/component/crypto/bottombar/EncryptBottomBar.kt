@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.bottombar

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptBottomBar(
    modifier: Modifier,
    isEncryptButtonEnabled: Boolean = false,
    onEncryptClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("encryptBottomBar"),
    ) {
        EncryptButtonBottomBar(
            modifier = modifier,
            encryptButtonIcon = R.drawable.ic_m3_encrypted_48dp_wght400,
            encryptButtonName = R.string.encrypt_button,
            encryptButtonContentDescription = R.string.encrypt_button_accessibility,
            isEncryptButtonEnabled = isEncryptButtonEnabled,
            onEncryptButtonClick = onEncryptClick,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptBottomBarPreview() {
    RIADigiDocTheme {
        EncryptBottomBar(
            modifier = Modifier,
        )
    }
}
