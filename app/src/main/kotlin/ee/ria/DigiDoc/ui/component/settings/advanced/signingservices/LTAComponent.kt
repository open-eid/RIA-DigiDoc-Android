// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings.advanced.signingservices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.settings.shared.SettingsRadioCard
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun LTAComponent(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val getDefaultLTA = sharedSettingsViewModel.dataStore::getSettingsDefaultLTA
    val setDefaultLTA = sharedSettingsViewModel.dataStore::setSettingsDefaultLTA

    val isEnabled = rememberSaveable { mutableStateOf(getDefaultLTA()) }
    // No DisposableEffect reset needed: the boolean requires no URL cleanup on navigation away.

    val titleText = stringResource(R.string.main_settings_default_lta_tab_title)
    val disabledText = stringResource(R.string.main_settings_default_lta_disabled)
    val enabledText = stringResource(R.string.main_settings_default_lta_enabled)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SPadding)
                .padding(top = SPadding),
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                Modifier
                    .padding(bottom = SPadding)
                    .semantics {
                        heading()
                    },
        )

        SettingsRadioCard(
            modifier = modifier,
            label = disabledText,
            selected = !isEnabled.value,
            onClick = {
                isEnabled.value = false
                setDefaultLTA(false)
            },
        )

        SettingsRadioCard(
            modifier = modifier,
            label = enabledText,
            selected = isEnabled.value,
            onClick = {
                isEnabled.value = true
                setDefaultLTA(true)
            },
        )

        InvisibleElement(modifier = modifier)
    }
}
