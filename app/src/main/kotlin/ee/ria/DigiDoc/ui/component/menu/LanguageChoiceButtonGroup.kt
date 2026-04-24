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

package ee.ria.DigiDoc.ui.component.menu

import android.content.res.Configuration
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.BlueBackground
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

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
                ).semantics {
                    testTagsAsResourceId = true
                }.testTag("initScreenLocale"),
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
                    val locale = getLocale(languageItem.locale)

                    sharedSettingsViewModel.dataStore.setLocale(locale)
                    sharedSettingsViewModel.recreateActivity()
                    sendAccessibilityEvent(
                        context,
                        getAccessibilityEventType(),
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
