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
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.advanced.AdvancedSettingComponent
import ee.ria.DigiDoc.ui.component.settings.advanced.AdvancedSettingComponentItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AdvancedSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    secureUtil: SecureUtil = SecureUtil(sharedSettingsViewModel.dataStore),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val checkedAskRoleAndAddress by sharedSettingsViewModel.enableRoleAsking.collectAsState()
    val checkedAllowScreenshots by sharedSettingsViewModel.allowScreenshots.collectAsState()
    val checkedAllowOpeningAllFileTypes by sharedSettingsViewModel.enableOpenAllFileTypes.collectAsState()

    val askRoleAndAddressTitleText = stringResource(R.string.main_settings_ask_role_and_address_title)
    val allowScreenshotsTitleText = stringResource(R.string.main_settings_allow_screenshots_title)
    val defaultSettingsButtonText = stringResource(R.string.main_settings_use_default_settings_button_title)
    val allowOpeningAllFileTypesButtonText = stringResource(R.string.main_settings_open_all_filetypes_title)
    val buttonName = stringResource(id = R.string.button_name)

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("advancedSettingsScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_menu_advanced,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
            isThirdButtonVisible = false,
        )

        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState())
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("scrollView"),
        ) {
            Text(
                modifier =
                    modifier
                        .semantics {
                            heading()
                        },
                text = stringResource(R.string.main_settings_general_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = modifier.height(XSPadding))
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable {
                            sharedSettingsViewModel.setSettingsAskRoleAndAddress(!checkedAskRoleAndAddress)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = askRoleAndAddressTitleText,
                    modifier =
                        modifier
                            .weight(1f)
                            .notAccessible(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Checkbox(
                    modifier =
                        modifier
                            .focusable(true)
                            .semantics {
                                contentDescription = askRoleAndAddressTitleText
                            },
                    checked = checkedAskRoleAndAddress,
                    onCheckedChange = {
                        sharedSettingsViewModel.setSettingsAskRoleAndAddress(it)
                    },
                )
            }

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable {
                            sharedSettingsViewModel.setAllowScreenshots(!checkedAllowScreenshots)
                            secureUtil.markAsSecure(activity)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = allowScreenshotsTitleText,
                    modifier =
                        modifier
                            .weight(1f)
                            .notAccessible(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Checkbox(
                    modifier =
                        modifier
                            .focusable(true)
                            .semantics {
                                contentDescription = allowScreenshotsTitleText
                            },
                    checked = checkedAllowScreenshots,
                    onCheckedChange = {
                        sharedSettingsViewModel.setAllowScreenshots(it)
                        secureUtil.markAsSecure(activity)
                    },
                )
            }

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable {
                            sharedSettingsViewModel.setSettingsOpenAllFileTypes(!checkedAllowOpeningAllFileTypes)
                            sharedSettingsViewModel.recreateActivity()
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = allowOpeningAllFileTypesButtonText,
                    modifier =
                        modifier
                            .weight(1f)
                            .notAccessible(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Checkbox(
                    modifier =
                        modifier
                            .focusable(true)
                            .semantics {
                                contentDescription = allowOpeningAllFileTypesButtonText
                            },
                    checked = checkedAllowOpeningAllFileTypes,
                    onCheckedChange = {
                        sharedSettingsViewModel.setSettingsOpenAllFileTypes(it)
                        sharedSettingsViewModel.recreateActivity()
                    },
                )
            }

            HorizontalDivider(modifier = modifier.padding(vertical = MPadding))

            Text(
                modifier =
                    modifier
                        .semantics {
                            heading()
                        },
                text = stringResource(R.string.main_settings_system_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = modifier.height(XSPadding))

            AdvancedSettingComponentItem().componentItems().forEach { item ->
                AdvancedSettingComponent(
                    modifier = modifier,
                    name = item.name,
                    testTag = item.testTag,
                ) {
                    navController.navigate(
                        item.route.route,
                    )
                }
            }

            Spacer(modifier = modifier.height(SPadding))

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = SPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    sharedSettingsViewModel.setSettingsAskRoleAndAddress(false)
                    sharedSettingsViewModel.setAllowScreenshots(false)
                    sharedSettingsViewModel.setSettingsOpenAllFileTypes(true)
                    sharedSettingsViewModel.resetToDefaultSettings()
                    secureUtil.markAsSecure(activity)
                    sharedSettingsViewModel.recreateActivity()
                    showMessage(context, R.string.main_settings_use_default_settings_message, SnackbarType.SUCCESS)
                }) {
                    Text(
                        modifier =
                            modifier
                                .padding(vertical = SPadding)
                                .semantics {
                                    contentDescription =
                                        "$defaultSettingsButtonText $buttonName"
                                    testTagsAsResourceId = true
                                }.testTag("mainSettingsRestoreDefaultSettingsButton"),
                        text = defaultSettingsButtonText,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AdvancedSettingsScreenPreview() {
    RIADigiDocTheme {
        AdvancedSettingsScreen(
            sharedMenuViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
