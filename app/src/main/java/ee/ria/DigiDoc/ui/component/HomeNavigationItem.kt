import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import ee.ria.DigiDoc.R

data class HomeNavigationItem(
    val label: String = "",
    val icon : ImageVector = Icons.Filled.Home,
    val route : String = "",
    val contentDescription: String = ""
) {
    @Composable
    fun bottomNavigationItems() : List<HomeNavigationItem> {
        return listOf(
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_signature),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_signature),
                route = Screens.Signature.route,
                contentDescription = stringResource(id = R.string.main_home_navigation_signature_accessibility).lowercase()
            ),
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_crypto),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_crypto),
                route = Screens.Crypto.route,
                contentDescription = stringResource(id = R.string.main_home_navigation_crypto_accessibility).lowercase()
            ),
            HomeNavigationItem(
                label = stringResource(id = R.string.main_home_navigation_eid),
                icon = ImageVector.vectorResource(id = R.drawable.ic_icon_eid),
                route = Screens.eID.route,
                contentDescription = stringResource(id = R.string.main_home_navigation_eid_accessibility).lowercase()
            )
        )
    }
}