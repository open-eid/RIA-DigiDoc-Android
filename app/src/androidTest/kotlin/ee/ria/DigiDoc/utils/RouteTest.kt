/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.ACCESSIBILITY_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ALL_FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CERTIFICATE_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CONTAINER_NOTIFICATIONS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DECRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DIAGNOSTICS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPTION_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPT_RECIPIENT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INFO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INIT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_IDENTIFICATION_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_PIN_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.PROXY_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_SCREEN_FROM_SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECIPIENT_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ROOT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_LANGUAGE_CHOOSER_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_THEME_CHOOSER_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_INPUT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNER_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.VALIDATION_SERVICES_SCREEN
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {
    @Test
    fun testRoutes() {
        assertEquals(INIT_SCREEN, Route.Init.route)
        assertEquals(HOME_SCREEN, Route.Home.route)
        assertEquals(SIGNATURE_SCREEN, Route.Signature.route)
        assertEquals(CRYPTO_SCREEN, Route.Crypto.route)
        assertEquals(EID_SCREEN, Route.EID.route)
        assertEquals(ALL_FILE_CHOOSING_SCREEN, Route.AllFilesChoosing.route)
        assertEquals(SIGNING_FILE_CHOOSING_SCREEN, Route.SigningFileChoosing.route)
        assertEquals(CRYPTO_FILE_CHOOSING_SCREEN, Route.CryptoFileChoosing.route)
        assertEquals(SIGNING_SCREEN, Route.Signing.route)
        assertEquals(ENCRYPT_SCREEN, Route.Encrypt.route)
        assertEquals(DECRYPT_SCREEN, Route.DecryptScreen.route)
        assertEquals(ACCESSIBILITY_SCREEN, Route.Accessibility.route)
        assertEquals(INFO_SCREEN, Route.Info.route)
        assertEquals(DIAGNOSTICS_SCREEN, Route.Diagnostics.route)
        assertEquals(SIGNER_DETAIL_SCREEN, Route.SignerDetail.route)
        assertEquals(CERTIFICATE_DETAIL_SCREEN, Route.CertificateDetail.route)
        assertEquals(RECIPIENT_DETAIL_SCREEN, Route.RecipientDetail.route)
        assertEquals(RECENT_DOCUMENTS_SCREEN, Route.RecentDocuments.route)
        assertEquals(RECENT_DOCUMENTS_SCREEN_FROM_SIGNING_SCREEN, Route.RecentDocumentsFromSigning.route)
        assertEquals(RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN, Route.RecentDocumentsFromEncrypt.route)
        assertEquals(SETTINGS_SCREEN, Route.Settings.route)
        assertEquals(SETTINGS_LANGUAGE_CHOOSER_SCREEN, Route.SettingsLanguageChooser.route)
        assertEquals(SETTINGS_THEME_CHOOSER_SCREEN, Route.SettingsThemeChooser.route)
        assertEquals(ROOT_SCREEN, Route.RootScreen.route)
        assertEquals(SIGNATURE_INPUT_SCREEN, Route.SignatureInputScreen.route)
        assertEquals(SIGNATURE_METHOD_SCREEN, Route.SignatureMethodScreen.route)
        assertEquals(ENCRYPT_RECIPIENT_SCREEN, Route.EncryptRecipientScreen.route)
        assertEquals(SIGNING_SERVICES_SCREEN, Route.SigningServicesScreen.route)
        assertEquals(VALIDATION_SERVICES_SCREEN, Route.ValidationServicesScreen.route)
        assertEquals(ENCRYPTION_SERVICES_SCREEN, Route.EncryptionServicesScreen.route)
        assertEquals(PROXY_SERVICES_SCREEN, Route.ProxyServicesScreen.route)
        assertEquals(CONTAINER_NOTIFICATIONS_SCREEN, Route.ContainerNotificationsScreen.route)
        assertEquals(MYEID_IDENTIFICATION_SCREEN, Route.MyEidIdentificationScreen.route)
        assertEquals(MYEID_SCREEN, Route.MyEidScreen.route)
        assertEquals(MYEID_PIN_SCREEN, Route.MyEidPinScreen.route)
    }
}
