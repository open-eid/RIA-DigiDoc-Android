@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.SignatureBottomBarItem

@Composable
fun signingBottomNavigationItems(
    showSignButton: Boolean,
    showEncryptButton: Boolean,
    showShareButton: Boolean,
    onSignClick: () -> Unit = {},
    onEncryptClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
): List<SignatureBottomBarItem> {
    return listOf(
        SignatureBottomBarItem(
            label = R.string.sign_button,
            contentDescription =
                stringResource(
                    id = R.string.sign_button,
                ).lowercase(),
            isSubButton = false,
            showButton = showSignButton,
            onClick = onSignClick,
        ),
        SignatureBottomBarItem(
            label = R.string.crypto_button,
            contentDescription =
                stringResource(
                    id = R.string.crypto_button_accessibility,
                ),
            isSubButton = true,
            showButton = showEncryptButton,
            onClick = onEncryptClick,
        ),
        SignatureBottomBarItem(
            label = R.string.share_button,
            contentDescription = stringResource(id = R.string.share_button),
            isSubButton = true,
            showButton = showShareButton,
            onClick = onShareClick,
        ),
    )
}
