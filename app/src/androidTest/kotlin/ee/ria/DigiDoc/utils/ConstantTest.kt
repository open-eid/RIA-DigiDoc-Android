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

import org.junit.Assert.assertEquals
import org.junit.Test

class ConstantTest {
    @Test
    fun constantTest_Languages_success() {
        assertEquals("et", Constant.Languages.ESTONIAN_LANGUAGE)
        assertEquals("en", Constant.Languages.ENGLISH_LANGUAGE)
    }

    @Test
    fun constantTest_Routes_success() {
        assertEquals("init_route", Constant.Routes.INIT_SCREEN)
        assertEquals("home_route", Constant.Routes.HOME_SCREEN)
        assertEquals("signature_route", Constant.Routes.SIGNATURE_SCREEN)
        assertEquals("crypto_route", Constant.Routes.CRYPTO_SCREEN)
        assertEquals("eid_route", Constant.Routes.EID_SCREEN)
        assertEquals("all_file_choosing_route", Constant.Routes.ALL_FILE_CHOOSING_SCREEN)
        assertEquals("file_choosing_route", Constant.Routes.SIGNING_FILE_CHOOSING_SCREEN)
        assertEquals("crypto_file_choosing_route", Constant.Routes.CRYPTO_FILE_CHOOSING_SCREEN)
        assertEquals("signing_route", Constant.Routes.SIGNING_SCREEN)
        assertEquals("encrypt_route", Constant.Routes.ENCRYPT_SCREEN)
        assertEquals("decrypt_route", Constant.Routes.DECRYPT_SCREEN)
        assertEquals("accessibility_route", Constant.Routes.ACCESSIBILITY_SCREEN)
        assertEquals("info_route", Constant.Routes.INFO_SCREEN)
        assertEquals("diagnostics_route", Constant.Routes.DIAGNOSTICS_SCREEN)
        assertEquals("signer_detail_route", Constant.Routes.SIGNER_DETAIL_SCREEN)
        assertEquals("certificate_detail_route", Constant.Routes.CERTIFICATE_DETAIL_SCREEN)
        assertEquals("recipient_detail_route", Constant.Routes.RECIPIENT_DETAIL_SCREEN)
        assertEquals("recent_documents_route", Constant.Routes.RECENT_DOCUMENTS_SCREEN)
        assertEquals("recent_documents_from_encrypt_route", Constant.Routes.RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN)
        assertEquals("settings_route", Constant.Routes.SETTINGS_SCREEN)
        assertEquals("settings_language_chooser_route", Constant.Routes.SETTINGS_LANGUAGE_CHOOSER_SCREEN)
        assertEquals("settings_theme_chooser_route", Constant.Routes.SETTINGS_THEME_CHOOSER_SCREEN)
        assertEquals("root_screen_route", Constant.Routes.ROOT_SCREEN)
        assertEquals("signature_input_route", Constant.Routes.SIGNATURE_INPUT_SCREEN)
        assertEquals("signature_method_route", Constant.Routes.SIGNATURE_METHOD_SCREEN)
        assertEquals("encrypt_recipient_route", Constant.Routes.ENCRYPT_RECIPIENT_SCREEN)
        assertEquals("signing_services_route", Constant.Routes.SIGNING_SERVICES_SCREEN)
        assertEquals("validation_services_route", Constant.Routes.VALIDATION_SERVICES_SCREEN)
        assertEquals("encryption_services_route", Constant.Routes.ENCRYPTION_SERVICES_SCREEN)
        assertEquals("proxy_services_route", Constant.Routes.PROXY_SERVICES_SCREEN)
        assertEquals("container_notifications_route", Constant.Routes.CONTAINER_NOTIFICATIONS_SCREEN)
        assertEquals("myeid_identification_route", Constant.Routes.MYEID_IDENTIFICATION_SCREEN)
        assertEquals("myeid_screen_route", Constant.Routes.MYEID_SCREEN)
        assertEquals("myeid_pin_screen_route", Constant.Routes.MYEID_PIN_SCREEN)
    }
}
