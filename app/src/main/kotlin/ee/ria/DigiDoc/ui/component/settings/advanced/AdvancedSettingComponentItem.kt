@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.settings.advanced

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Route

data class AdvancedSettingComponentItem(
    @StringRes val name: Int = 0,
    val testTag: String = "",
    val route: Route = Route.Settings,
) {
    @Composable
    fun componentItems(): List<AdvancedSettingComponentItem> {
        return listOf(
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
}
