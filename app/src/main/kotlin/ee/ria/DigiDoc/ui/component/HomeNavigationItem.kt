@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Route

data class HomeNavigationItem(
    val label: String = "",
    val icon: ImageVector = Icons.Filled.Home,
    val route: String = "",
    val contentDescription: String = "",
) {
    @Composable
    fun bottomNavigationItems(): List<HomeNavigationItem> {
        return listOf(
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_signature),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_signature),
                route = Route.Signature.route,
                contentDescription =
                    stringResource(
                        id = R.string.main_home_navigation_signature_accessibility,
                    ),
            ),
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_crypto),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_crypto),
                route = Route.Crypto.route,
                contentDescription =
                    stringResource(
                        id = R.string.main_home_navigation_crypto_accessibility,
                    ),
            ),
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_eid),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_eid),
                route = Route.EID.route,
                contentDescription = stringResource(id = R.string.main_home_navigation_eid_accessibility),
            ),
        )
    }
}
