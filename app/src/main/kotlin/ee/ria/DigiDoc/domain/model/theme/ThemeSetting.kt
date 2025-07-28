@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.theme

enum class ThemeSetting(
    val mode: String,
) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system"),
    ;

    companion object {
        fun fromMode(mode: String): ThemeSetting = entries.find { it.mode == mode } ?: SYSTEM
    }
}
