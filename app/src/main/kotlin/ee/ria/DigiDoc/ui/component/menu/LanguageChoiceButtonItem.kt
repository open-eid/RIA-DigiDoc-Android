@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.Language

data class LanguageChoiceButtonItem(
    @StringRes val label: Int = 0,
    val icon: ImageVector = Icons.Filled.Home,
    val locale: String = "",
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun radioItems(): List<LanguageChoiceButtonItem> {
        return listOf(
            LanguageChoiceButtonItem(
                label = R.string.init_lang_locale_et,
                locale = Language.Estonian.locale,
                contentDescription =
                    stringResource(
                        id = R.string.init_lang_locale_et,
                    ).lowercase(),
                testTag = "languageScreenLocaleEt",
            ),
            LanguageChoiceButtonItem(
                label = R.string.init_lang_locale_ru,
                locale = Language.Russian.locale,
                contentDescription =
                    stringResource(
                        id = R.string.init_lang_locale_ru,
                    ).lowercase(),
                testTag = "languageScreenLocaleRu",
            ),
            LanguageChoiceButtonItem(
                label = R.string.init_lang_locale_en,
                locale = Language.English.locale,
                contentDescription =
                    stringResource(
                        id = R.string.init_lang_locale_en,
                    ).lowercase(),
                testTag = "languageScreenLocaleEn",
            ),
        )
    }
}
