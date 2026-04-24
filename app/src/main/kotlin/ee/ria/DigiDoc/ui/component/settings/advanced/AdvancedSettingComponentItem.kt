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

package ee.ria.DigiDoc.ui.component.settings.advanced

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Route

data class AdvancedSettingComponentItem(
    @param:StringRes val name: Int = 0,
    val testTag: String = "",
    val route: Route = Route.Settings,
) {
    @Composable
    fun componentItems(): List<AdvancedSettingComponentItem> =
        listOf(
            AdvancedSettingComponentItem(
                name = R.string.main_settings_signing_services_title,
                testTag = "advancedSettingSigningServices",
                route = Route.SigningServicesScreen,
            ),
            AdvancedSettingComponentItem(
                name = R.string.main_settings_validation_services_title,
                testTag = "advancedSettingValidationServices",
                route = Route.ValidationServicesScreen,
            ),
            AdvancedSettingComponentItem(
                name = R.string.main_settings_crypto_services_title,
                testTag = "advancedSettingCryptoServices",
                route = Route.EncryptionServicesScreen,
            ),
            AdvancedSettingComponentItem(
                name = R.string.main_settings_proxy_title,
                testTag = "advancedSettingProxyServices",
                route = Route.ProxyServicesScreen,
            ),
        )
}
