@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import ee.ria.DigiDoc.fragment.InitFragment
import ee.ria.DigiDoc.fragment.MenuFragment
import ee.ria.DigiDoc.fragment.RecentDocumentsFragment
import ee.ria.DigiDoc.fragment.RootFragment
import ee.ria.DigiDoc.fragment.SettingsFragment
import ee.ria.DigiDoc.fragment.SettingsRightsFragment
import ee.ria.DigiDoc.fragment.SettingsSigningFragment
import ee.ria.DigiDoc.fragment.SignatureInputFragment
import ee.ria.DigiDoc.fragment.SigningFragment
import ee.ria.DigiDoc.fragment.screen.SignatureMethodFragment
import ee.ria.DigiDoc.ui.component.signing.certificate.CertificateDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.RolesDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.SignerDetailsView
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@Composable
fun RIADigiDocAppScreen(externalFileUris: List<Uri>) {
    val navController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedSignatureViewModel: SharedSignatureViewModel = hiltViewModel()
    val sharedCertificateViewModel: SharedCertificateViewModel = hiltViewModel()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()

    sharedContainerViewModel.setExternalFileUris(externalFileUris)

    var startDestination = Route.Init.route
    if (sharedSettingsViewModel.dataStore.getLocale() != null) {
        startDestination = Route.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(route = Route.Init.route) {
            InitFragment(
                navController = navController,
                externalFileUris = sharedContainerViewModel.externalFileUris.collectAsState().value,
            )
        }
        composable(route = Route.Home.route) {
            HomeFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                externalFileUris = sharedContainerViewModel.externalFileUris.collectAsState().value,
            )
        }
        composable(route = Route.SignatureInputScreen.route) {
            SignatureInputFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.SignatureMethodScreen.route) {
            SignatureMethodFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.Menu.route) {
            MenuFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
            )
        }
        composable(route = Route.Signing.route) {
            SigningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedSignatureViewModel = sharedSignatureViewModel,
            )
        }
        composable(route = Route.RolesDetail.route) {
            RolesDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSignatureViewModel = sharedSignatureViewModel,
            )
        }
        composable(route = Route.SignerDetail.route) {
            SignerDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSignatureViewModel = sharedSignatureViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.CertificateDetail.route) {
            CertificateDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.Accessibility.route) {
            AccessibilityFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
            )
        }
        composable(route = Route.Info.route) {
            InfoFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
            )
        }
        composable(route = Route.Diagnostics.route) {
            DiagnosticsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.RecentDocuments.route) {
            RecentDocumentsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.Settings.route) {
            SettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
            )
        }
        composable(route = Route.SettingsRights.route) {
            SettingsRightsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.SettingsSigning.route) {
            SettingsSigningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.FileChoosing.route) {
            FileOpeningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.RootScreen.route) {
            RootFragment(
                modifier = Modifier.safeDrawingPadding(),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RIADigiDocAppScreenPreview() {
    RIADigiDocTheme {
        RIADigiDocAppScreen(listOf())
    }
}
