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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.HrefDynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.keyboard.keyboardScrollable
import ee.ria.DigiDoc.ui.theme.Dimensions.LINE_HEIGHT
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccessibilityScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val screenScrollState = rememberScrollState()

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("accessibilityScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_accessibility_title,
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
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag("scrollView")
                        .fillMaxWidth()
                        .verticalScroll(screenScrollState)
                        .keyboardScrollable(screenScrollState),
                horizontalAlignment = Alignment.Start,
            ) {
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction),
                )
                HrefDynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityLink"),
                    text1 = stringResource(R.string.main_accessibility_link_text),
                    text2 = ".",
                    linkText = stringResource(R.string.main_accessibility_link_tag),
                    linkUrl = stringResource(R.string.main_accessibility_link_url),
                    newLineBeforeText2 = false,
                    textStyle =
                        TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                            textAlign = TextAlign.Start,
                            lineHeight = TextUnit(LINE_HEIGHT, TextUnitType.Sp),
                        ),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroduction2"),
                    text = stringResource(R.string.main_accessibility_introduction_2),
                )
                Text(
                    modifier =
                        modifier
                            .padding(
                                start = SPadding,
                                top = MPadding,
                                end = SPadding,
                            ).semantics { heading() }
                            .testTag("mainAccessibilityIntroductionScreenReaderTitle"),
                    text = stringResource(id = R.string.main_accessibility_introduction_screen_reader_title),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroduction2"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_2),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionApps"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_apps),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionIos"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_ios),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenReaderIntroductionAndroid"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_reader_introduction_android),
                )
                Text(
                    modifier =
                        modifier
                            .padding(
                                start = SPadding,
                                top = MPadding,
                                end = SPadding,
                            ).semantics { heading() }
                            .testTag("mainAccessibilityIntroductionScreenMagnificationTitle"),
                    text = stringResource(id = R.string.main_accessibility_introduction_screen_magnification_title),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.titleLarge,
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationIntroduction"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_introduction),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenTools"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_screen_tools),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsIos"),
                    text =
                        stringResource(
                            R.string.main_accessibility_introduction_screen_magnification_screen_tools_ios,
                        ),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationScreenToolsAndroid"),
                    text =
                        stringResource(
                            R.string.main_accessibility_introduction_screen_magnification_screen_tools_android,
                        ),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationTools"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationToolsIos"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_ios),
                )
                DynamicText(
                    modifier =
                        modifier
                            .padding(
                                horizontal = SPadding,
                                vertical = SPadding,
                            ).testTag("mainAccessibilityIntroductionScreenMagnificationToolsAndroid"),
                    text = stringResource(R.string.main_accessibility_introduction_screen_magnification_tools_android),
                )
                InvisibleElement(modifier = modifier)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AccessibilityScreenPreview() {
    RIADigiDocTheme {
        AccessibilityScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
        )
    }
}
