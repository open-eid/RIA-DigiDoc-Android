@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.theme.ThemeSetting

data class ThemeChoiceButtonItem(
    @param:StringRes val label: Int = 0,
    val setting: ThemeSetting = ThemeSetting.SYSTEM,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<ThemeChoiceButtonItem> =
        listOf(
            ThemeChoiceButtonItem(
                label = R.string.main_settings_theme_system,
                setting = ThemeSetting.SYSTEM,
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_theme_system,
                    ).lowercase(),
                testTag = "themeScreenSystemSetting",
            ),
            ThemeChoiceButtonItem(
                label = R.string.main_settings_theme_light,
                setting = ThemeSetting.LIGHT,
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_theme_light,
                    ).lowercase(),
                testTag = "themeScreenLightSetting",
            ),
            ThemeChoiceButtonItem(
                label = R.string.main_settings_theme_dark,
                setting = ThemeSetting.DARK,
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_theme_dark,
                    ).lowercase(),
                testTag = "themeScreenDarkSetting",
            ),
        )
}
