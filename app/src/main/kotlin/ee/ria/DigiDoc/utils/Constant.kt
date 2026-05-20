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

object Constant {
    // ee.ria.DigiDoc.utils.Locale
    object Languages {
        const val ESTONIAN_LANGUAGE = "et"
        const val ENGLISH_LANGUAGE = "en"
    }

    // ee.ria.DigiDoc.utils.Route
    object Routes {
        const val INIT_SCREEN = "init_route"
        const val HOME_SCREEN = "home_route"
        const val SIGNATURE_SCREEN = "signature_route"
        const val CRYPTO_SCREEN = "crypto_route"
        const val EID_SCREEN = "eid_route"
        const val ALL_FILE_CHOOSING_SCREEN = "all_file_choosing_route"
        const val SIGNING_FILE_CHOOSING_SCREEN = "file_choosing_route"
        const val CRYPTO_FILE_CHOOSING_SCREEN = "crypto_file_choosing_route"
        const val SIGNING_SCREEN = "signing_route"
        const val ENCRYPT_SCREEN = "encrypt_route"
        const val DECRYPT_SCREEN = "decrypt_route"
        const val ACCESSIBILITY_SCREEN = "accessibility_route"
        const val INFO_SCREEN = "info_route"
        const val DIAGNOSTICS_SCREEN = "diagnostics_route"
        const val SIGNER_DETAIL_SCREEN = "signer_detail_route"
        const val CERTIFICATE_DETAIL_SCREEN = "certificate_detail_route"
        const val RECIPIENT_DETAIL_SCREEN = "recipient_detail_route"
        const val RECENT_DOCUMENTS_SCREEN = "recent_documents_route"
        const val RECENT_DOCUMENTS_SCREEN_FROM_SIGNING_SCREEN = "recent_documents_route_from_signing_screen"
        const val RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN = "recent_documents_from_encrypt_route"
        const val SETTINGS_SCREEN = "settings_route"
        const val SETTINGS_LANGUAGE_CHOOSER_SCREEN = "settings_language_chooser_route"
        const val SETTINGS_THEME_CHOOSER_SCREEN = "settings_theme_chooser_route"
        const val ROOT_SCREEN = "root_screen_route"
        const val SIGNATURE_INPUT_SCREEN = "signature_input_route"
        const val SIGNATURE_METHOD_SCREEN = "signature_method_route"
        const val ENCRYPT_RECIPIENT_SCREEN = "encrypt_recipient_route"
        const val SIGNING_SERVICES_SCREEN = "signing_services_route"
        const val VALIDATION_SERVICES_SCREEN = "validation_services_route"
        const val ENCRYPTION_SERVICES_SCREEN = "encryption_services_route"
        const val PROXY_SERVICES_SCREEN = "proxy_services_route"
        const val CONTAINER_NOTIFICATIONS_SCREEN = "container_notifications_route"
        const val MYEID_IDENTIFICATION_SCREEN = "myeid_identification_route"
        const val MYEID_SCREEN = "myeid_screen_route"
        const val MYEID_PIN_SCREEN = "myeid_pin_screen_route"
    }
}
