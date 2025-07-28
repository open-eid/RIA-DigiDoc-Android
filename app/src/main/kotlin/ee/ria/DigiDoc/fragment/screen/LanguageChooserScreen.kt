@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.LanguageChoiceButtonItem
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.RadioButtonItem
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Language
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LanguageChooserScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    LocalContext.current
    val languageText = stringResource(id = R.string.menu_language)
    val languageSelected = stringResource(id = R.string.menu_language_selected)
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(sharedSettingsViewModel.dataStore.getLocale()) }
    val selectedOption by remember { mutableStateOf(currentLanguage?.language ?: Language.English.locale) }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("languageChooserScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_menu_language,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
                sharedMenuViewModel = sharedMenuViewModel,
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
            isFirstButtonVisible = false,
        )

        Column(
            modifier =
                modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LanguageChoiceButtonItem().radioItems().forEachIndexed { _, languageItem ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = SPadding, vertical = XSPadding)
                            .clickable {
                                selectedOption == languageItem.locale
                                setLanguageSetting(sharedSettingsViewModel, languageItem.locale)
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButtonItem(
                        modifier = modifier,
                        title = languageItem.label,
                        changedLabel = R.string.language_changed,
                        contentDescription =
                            if (languageItem.locale == currentLanguage?.language) {
                                "${languageItem.contentDescription} $languageSelected"
                            } else {
                                "$languageText ${languageItem.contentDescription}"
                            },
                        testTag = languageItem.testTag,
                        isSelected = selectedOption == languageItem.locale,
                    ) {
                        setLanguageSetting(sharedSettingsViewModel, languageItem.locale)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

private fun setLanguageSetting(
    sharedSettingsViewModel: SharedSettingsViewModel,
    chosenLocale: String,
) {
    val locale = getLocale(chosenLocale)
    sharedSettingsViewModel.dataStore.setLocale(locale)
    sharedSettingsViewModel.recreateActivity()
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LanguageChooserScreenPreview() {
    RIADigiDocTheme {
        val navController = rememberNavController()
        val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
        val sharedMenuViewModel: SharedMenuViewModel = hiltViewModel()
        LanguageChooserScreen(
            navController = navController,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedMenuViewModel = sharedMenuViewModel,
        )
    }
}
