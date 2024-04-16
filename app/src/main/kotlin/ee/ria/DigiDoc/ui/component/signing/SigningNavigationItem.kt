@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.HomeNavigationItem
import ee.ria.DigiDoc.utils.Route

@Composable
fun SigningBottomNavigationItems(): List<HomeNavigationItem> {
    return listOf(
        HomeNavigationItem(
            label = stringResource(id = R.string.sign_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_signature),
            route = Route.Signing.route,
            contentDescription =
                stringResource(
                    id = R.string.sign_button,
                ).lowercase(),
        ),
        HomeNavigationItem(
            label = stringResource(id = R.string.crypto_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_crypto),
            route = Route.Signing.route,
            contentDescription =
                stringResource(
                    id = R.string.crypto_button_accessibility,
                ),
        ),
        HomeNavigationItem(
            label = stringResource(id = R.string.share_button),
            icon = ImageVector.vectorResource(id = R.drawable.ic_icon_share),
            route = Route.Signing.route,
            contentDescription = stringResource(id = R.string.share_button),
        ),
    )
}
