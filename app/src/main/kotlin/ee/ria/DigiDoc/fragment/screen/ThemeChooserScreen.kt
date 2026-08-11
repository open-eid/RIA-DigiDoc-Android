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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.theme.ThemeSetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.menu.ThemeChoiceButtonItem
import ee.ria.DigiDoc.ui.component.shared.RadioButtonItem
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ThemeChooserScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val context = LocalContext.current
    val themeChanged = stringResource(id = R.string.theme_changed)
    val themeSelected = stringResource(id = R.string.menu_theme_selected)
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    var currentTheme by remember { mutableStateOf(sharedSettingsViewModel.dataStore.getThemeSetting()) }
    val selectedOption by remember { mutableStateOf(currentTheme.mode) }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("themeChooserScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_menu_appearance,
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
            isSecondButtonVisible = false,
        )

        Column(
            modifier =
                modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ThemeChoiceButtonItem().radioItems().forEachIndexed { _, themeItem ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = SPadding, vertical = XSPadding)
                            .clickable {
                                selectedOption == themeItem.setting.mode
                                setThemeSetting(sharedSettingsViewModel, themeItem.setting)
                                sendAccessibilityEvent(
                                    context,
                                    themeChanged,
                                )
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButtonItem(
                        modifier = modifier,
                        title = themeItem.label,
                        changedLabel = R.string.theme_changed,
                        contentDescription =
                            if (themeItem.setting.mode == currentTheme.mode) {
                                "${themeItem.contentDescription} $themeSelected"
                            } else {
                                "$themeItem ${themeItem.contentDescription}"
                            },
                        testTag = themeItem.testTag,
                        isSelected = selectedOption == themeItem.setting.mode,
                    ) {
                        setThemeSetting(sharedSettingsViewModel, themeItem.setting)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

private fun setThemeSetting(
    sharedSettingsViewModel: SharedSettingsViewModel,
    themeSetting: ThemeSetting,
) {
    val themeSetting = ThemeSetting.fromMode(themeSetting.mode)
    sharedSettingsViewModel.dataStore.setThemeSetting(themeSetting)
    sharedSettingsViewModel.recreateActivity()
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ThemeChooserScreenPreview() {
    RIADigiDocTheme {
        val navController = rememberNavController()
        val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
        val sharedMenuViewModel: SharedMenuViewModel = hiltViewModel()
        ThemeChooserScreen(
            navController = navController,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedMenuViewModel = sharedMenuViewModel,
        )
    }
}
