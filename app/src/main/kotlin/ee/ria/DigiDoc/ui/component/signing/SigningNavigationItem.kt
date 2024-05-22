@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.SignatureBottomBarItem

@Composable
fun signingBottomNavigationItems(
    onSignClick: () -> Unit = {},
    onEncryptClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
): List<SignatureBottomBarItem> {
    return listOf(
        SignatureBottomBarItem(
            label = stringResource(id = R.string.sign_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_signature),
            contentDescription =
                stringResource(
                    id = R.string.sign_button,
                ).lowercase(),
            onClick = onSignClick,
        ),
        SignatureBottomBarItem(
            label = stringResource(id = R.string.crypto_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_crypto),
            contentDescription =
                stringResource(
                    id = R.string.crypto_button_accessibility,
                ),
            onClick = onEncryptClick,
        ),
        SignatureBottomBarItem(
            label = stringResource(id = R.string.share_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_share),
            contentDescription = stringResource(id = R.string.share_button),
            onClick = onShareClick,
        ),
    )
}
