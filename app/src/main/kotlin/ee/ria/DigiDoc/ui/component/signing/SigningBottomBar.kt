@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SigningBottomBar(
    modifier: Modifier,
    showSignButton: Boolean,
    showEncryptButton: Boolean,
    showShareButton: Boolean,
    onSignClick: () -> Unit = {},
    onEncryptClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(screenViewLargePadding),
    ) {
        signingBottomNavigationItems(
            showSignButton = showSignButton,
            showEncryptButton = showEncryptButton,
            showShareButton = showShareButton,
            onSignClick = onSignClick,
            onEncryptClick = onEncryptClick,
            onShareClick = onShareClick,
        ).forEachIndexed { _, navigationItem ->
            if (navigationItem.showButton) {
                Row {
                    PrimaryButton(
                        title = navigationItem.label,
                        contentDescription = navigationItem.contentDescription,
                        isSubButton = navigationItem.isSubButton,
                        enabled = navigationItem.showButton,
                        onClickItem = navigationItem.onClick,
                    )
                }
            }
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
            showSignButton = true,
            showEncryptButton = true,
            showShareButton = true,
        )
    }
}
