@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.BlueBackground
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LanguageChoiceButtonGroup(
    modifier: Modifier = Modifier,
    onClickAction: () -> Unit = {},
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val languageChanged = stringResource(id = R.string.language_changed)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .wrapContentHeight()
                .padding(
                    horizontal = SPadding,
                    vertical = MPadding,
                )
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("initScreenLocale"),
    ) {
        LanguageChoiceButtonItem().radioItems().forEachIndexed { _, languageItem ->
            LanguageButton(
                modifier =
                    modifier
                        .testTag(languageItem.testTag),
                testTag = languageItem.testTag,
                label = languageItem.label,
                contentDescription = "${stringResource(
                    id = R.string.menu_language,
                )} ${languageItem.contentDescription}",
                onClickItem = {
                    val locale = Locale(languageItem.locale)
                    sharedSettingsViewModel.dataStore.setLocale(locale)
                    sharedSettingsViewModel.recreateActivity()
                    AccessibilityUtil.sendAccessibilityEvent(
                        context,
                        AccessibilityEvent.TYPE_ANNOUNCEMENT,
                        languageChanged,
                    )
                    onClickAction()
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LanguageChoiceButtonGroupPreview() {
    RIADigiDocTheme {
        Surface(color = BlueBackground) {
            LanguageChoiceButtonGroup()
        }
    }
}
