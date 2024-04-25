@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.FileOpeningFragment
import ee.ria.DigiDoc.fragment.HomeFragment
import ee.ria.DigiDoc.fragment.MenuFragment
import ee.ria.DigiDoc.fragment.SettingsFragment
import ee.ria.DigiDoc.fragment.SettingsRightsFragment
import ee.ria.DigiDoc.fragment.SettingsSigningFragment
import ee.ria.DigiDoc.fragment.SigningFragment
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel

@Composable
fun RIADigiDocAppScreen() {
    val navController = rememberNavController()
    val navBarNavController = rememberNavController()
    val signatureAddController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
    ) {
        composable(route = Route.Home.route) {
            HomeFragment(
                navController = navController,
                navBarNavController = navBarNavController,
                signatureAddController = signatureAddController,
            )
        }
        composable(route = Route.Menu.route) {
            MenuFragment(
                navController = navController,
            )
        }
        composable(route = Route.Signing.route) {
            SigningFragment(
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.Settings.route) {
            SettingsFragment(
                navController = navController,
            )
        }
        composable(route = Route.SettingsRights.route) {
            SettingsRightsFragment(
                navController = navController,
            )
        }
        composable(route = Route.SettingsSigning.route) {
            SettingsSigningFragment(
                navController = navController,
            )
        }
        composable(route = Route.FileChoosing.route) {
            FileOpeningFragment(
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RIADigiDocAppScreenPreview() {
    RIADigiDocTheme {
        RIADigiDocAppScreen()
    }
}
