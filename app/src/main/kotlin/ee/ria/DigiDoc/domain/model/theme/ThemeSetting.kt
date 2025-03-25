@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.theme

import ee.ria.DigiDoc.domain.model.theme.ThemeSetting.entries

enum class ThemeSetting(val mode: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system"),
    ;

    companion object {
        fun fromMode(mode: String): ThemeSetting {
            return entries.find { it.mode == mode } ?: SYSTEM
        }
    }
}
