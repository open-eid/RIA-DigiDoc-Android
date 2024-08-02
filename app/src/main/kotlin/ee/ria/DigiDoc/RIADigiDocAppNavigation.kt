@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.AccessibilityFragment
import ee.ria.DigiDoc.fragment.DiagnosticsFragment
import ee.ria.DigiDoc.fragment.FileOpeningFragment
import ee.ria.DigiDoc.fragment.HomeFragment
import ee.ria.DigiDoc.fragment.InfoFragment
import ee.ria.DigiDoc.fragment.MenuFragment
import ee.ria.DigiDoc.fragment.RecentDocumentsFragment
import ee.ria.DigiDoc.fragment.SettingsFragment
import ee.ria.DigiDoc.fragment.SettingsRightsFragment
import ee.ria.DigiDoc.fragment.SettingsSigningFragment
import ee.ria.DigiDoc.fragment.SigningFragment
import ee.ria.DigiDoc.ui.component.signing.certificate.CertificateDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.RolesDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.SignerDetailsView
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@Composable
fun RIADigiDocAppScreen() {
    val navController = rememberNavController()
    val navBarNavController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedSignatureViewModel: SharedSignatureViewModel = hiltViewModel()
    val sharedCertificateViewModel: SharedCertificateViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
    ) {
        composable(route = Route.Home.route) {
            HomeFragment(
                navController = navController,
                navBarNavController = navBarNavController,
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
                sharedSignatureViewModel = sharedSignatureViewModel,
            )
        }
        composable(route = Route.RolesDetail.route) {
            RolesDetailsView(
                navController = navController,
                sharedSignatureViewModel = sharedSignatureViewModel,
            )
        }
        composable(route = Route.SignerDetail.route) {
            SignerDetailsView(
                navController = navController,
                sharedSignatureViewModel = sharedSignatureViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.CertificateDetail.route) {
            CertificateDetailsView(
                navController = navController,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.Accessibility.route) {
            AccessibilityFragment(
                navController = navController,
            )
        }
        composable(route = Route.Info.route) {
            InfoFragment(
                navController = navController,
            )
        }
        composable(route = Route.Diagnostics.route) {
            DiagnosticsFragment(
                navController = navController,
            )
        }
        composable(route = Route.RecentDocuments.route) {
            RecentDocumentsFragment(
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
                sharedCertificateViewModel = sharedCertificateViewModel,
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
