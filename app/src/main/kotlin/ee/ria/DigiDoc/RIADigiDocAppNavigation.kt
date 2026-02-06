/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.common.model.FileOpeningMethod
import ee.ria.DigiDoc.fragment.AccessibilityFragment
import ee.ria.DigiDoc.fragment.AdvancedSettingsFragment
import ee.ria.DigiDoc.fragment.CryptoFileOpeningFragment
import ee.ria.DigiDoc.fragment.DecryptFragment
import ee.ria.DigiDoc.fragment.DecryptMethodChooserFragment
import ee.ria.DigiDoc.fragment.DiagnosticsFragment
import ee.ria.DigiDoc.fragment.EncryptFragment
import ee.ria.DigiDoc.fragment.EncryptRecipientFragment
import ee.ria.DigiDoc.fragment.EncryptionServicesSettingsFragment
import ee.ria.DigiDoc.fragment.FileOpeningFragment
import ee.ria.DigiDoc.fragment.HomeFragment
import ee.ria.DigiDoc.fragment.InfoFragment
import ee.ria.DigiDoc.fragment.InitFragment
import ee.ria.DigiDoc.fragment.LanguageChooserFragment
import ee.ria.DigiDoc.fragment.MyEidFragment
import ee.ria.DigiDoc.fragment.MyEidIdentificationFragment
import ee.ria.DigiDoc.fragment.MyEidIdentificationMethodChooserFragment
import ee.ria.DigiDoc.fragment.MyEidPinFragment
import ee.ria.DigiDoc.fragment.ProxyServicesSettingsFragment
import ee.ria.DigiDoc.fragment.RecentDocumentsFragment
import ee.ria.DigiDoc.fragment.RootFragment
import ee.ria.DigiDoc.fragment.SignatureInputFragment
import ee.ria.DigiDoc.fragment.SignatureMethodFragment
import ee.ria.DigiDoc.fragment.SigningFragment
import ee.ria.DigiDoc.fragment.SigningServicesSettingsFragment
import ee.ria.DigiDoc.fragment.ThemeChooserFragment
import ee.ria.DigiDoc.fragment.ValidationServicesSettingsFragment
import ee.ria.DigiDoc.fragment.WebEidFragment
import ee.ria.DigiDoc.ui.component.crypto.recipient.RecipientDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.CertificateDetailsView
import ee.ria.DigiDoc.ui.component.signing.certificate.SignerDetailsView
import ee.ria.DigiDoc.ui.component.signing.notifications.ContainerNotificationsFragment
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMyEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedRecipientViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@Composable
fun RIADigiDocAppScreen(
    externalFileUris: List<Uri>,
    webEidUri: Uri? = null,
) {
    val navController = rememberNavController()
    val sharedMenuViewModel: SharedMenuViewModel = hiltViewModel()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedRecipientViewModel: SharedRecipientViewModel = hiltViewModel()
    val sharedSignatureViewModel: SharedSignatureViewModel = hiltViewModel()
    val sharedCertificateViewModel: SharedCertificateViewModel = hiltViewModel()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    val sharedMyEidViewModel: SharedMyEidViewModel = hiltViewModel()

    sharedContainerViewModel.setExternalFileUris(externalFileUris)

    val startDestination =
        when {
            webEidUri != null -> Route.WebEidScreen.route
            sharedSettingsViewModel.dataStore.getLocale() != null -> Route.Home.route
            else -> Route.Init.route
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
                sharedMenuViewModel = sharedMenuViewModel,
                externalFileUris = sharedContainerViewModel.externalFileUris.collectAsState().value,
            )
        }
        composable(route = Route.SignatureInputScreen.route) {
            SignatureInputFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.SignatureMethodScreen.route) {
            SignatureMethodFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.Signing.route) {
            SigningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedSignatureViewModel = sharedSignatureViewModel,
            )
        }
        composable(route = Route.SignerDetail.route) {
            SignerDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSignatureViewModel = sharedSignatureViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.CertificateDetail.route) {
            CertificateDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.RecipientDetail.route) {
            RecipientDetailsView(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedRecipientViewModel = sharedRecipientViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.EncryptRecipientScreen.route) {
            EncryptRecipientFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedRecipientViewModel = sharedRecipientViewModel,
            )
        }
        composable(route = Route.Encrypt.route) {
            EncryptFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedRecipientViewModel = sharedRecipientViewModel,
            )
        }
        composable(route = Route.DecryptScreen.route) {
            DecryptFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
            )
        }
        composable(route = Route.DecryptMethodScreen.route) {
            DecryptMethodChooserFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.Accessibility.route) {
            AccessibilityFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.Info.route) {
            InfoFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.Diagnostics.route) {
            DiagnosticsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.RecentDocuments.route) {
            RecentDocumentsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                fileOpeningMethod = FileOpeningMethod.ALL,
            )
        }
        composable(route = Route.RecentDocumentsFromSigning.route) {
            RecentDocumentsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                fileOpeningMethod = FileOpeningMethod.SIGNING,
            )
        }
        composable(route = Route.RecentDocumentsFromEncrypt.route) {
            RecentDocumentsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                fileOpeningMethod = FileOpeningMethod.CRYPTO,
            )
        }
        composable(route = Route.Settings.route) {
            AdvancedSettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.SigningServicesScreen.route) {
            SigningServicesSettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.ValidationServicesScreen.route) {
            ValidationServicesSettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.EncryptionServicesScreen.route) {
            EncryptionServicesSettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedCertificateViewModel = sharedCertificateViewModel,
            )
        }
        composable(route = Route.ProxyServicesScreen.route) {
            ProxyServicesSettingsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
        }
        composable(route = Route.SettingsLanguageChooser.route) {
            LanguageChooserFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.SettingsThemeChooser.route) {
            ThemeChooserFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.AllFilesChoosing.route) {
            FileOpeningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
                fileOpeningMethod = FileOpeningMethod.ALL,
            )
        }
        composable(route = Route.SigningFileChoosing.route) {
            FileOpeningFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
                fileOpeningMethod = FileOpeningMethod.SIGNING,
            )
        }
        composable(route = Route.CryptoFileChoosing.route) {
            CryptoFileOpeningFragment(
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
        composable(route = Route.MyEidIdentificationScreen.route) {
            MyEidIdentificationFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedMyEidViewModel = sharedMyEidViewModel,
            )
        }
        composable(route = Route.MyEidIdentificationMethodScreen.route) {
            MyEidIdentificationMethodChooserFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.ContainerNotificationsScreen.route) {
            ContainerNotificationsFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedContainerViewModel = sharedContainerViewModel,
                sharedMenuViewModel = sharedMenuViewModel,
            )
        }
        composable(route = Route.MyEidScreen.route) {
            MyEidFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedMyEidViewModel = sharedMyEidViewModel,
            )
        }
        composable(route = Route.MyEidPinScreen.route) {
            MyEidPinFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                sharedMenuViewModel = sharedMenuViewModel,
                sharedMyEidViewModel = sharedMyEidViewModel,
            )
        }
        composable(route = Route.WebEidScreen.route) {
            WebEidFragment(
                modifier = Modifier.safeDrawingPadding(),
                navController = navController,
                webEidUri = webEidUri,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RIADigiDocAppScreenPreview() {
    RIADigiDocTheme {
        RIADigiDocAppScreen(
            listOf(),
            webEidUri = null,
        )
    }
}
