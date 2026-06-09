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

package ee.ria.DigiDoc.ui.component.settings.advanced.signingservices

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.domain.model.settings.UUIDSetting
import ee.ria.DigiDoc.ui.component.settings.shared.SettingsRadioCard
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun MobileIdAndSmartIdServicesComponent(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val focusRequester = remember { FocusRequester() }

    val getSettingsUUID = sharedSettingsViewModel.dataStore::getSettingsUUID
    val getUuidSetting = sharedSettingsViewModel.dataStore::getUuidSetting
    val setSettingsUuid = sharedSettingsViewModel.dataStore::setSettingsUUID
    val setUuidSetting = sharedSettingsViewModel.dataStore::setUuidSetting
    val defaultUuid = getSettingsUUID()
    val settingsUuidChoice = rememberSaveable { mutableStateOf(getUuidSetting().name) }
    var settingsUuid by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = defaultUuid,
                selection = TextRange(defaultUuid.length),
            ),
        )
    }
    val useDefaultAccessText = stringResource(R.string.main_settings_siva_default_access_title)
    val useManualAccessText = stringResource(R.string.main_settings_siva_default_manual_access_title)
    val accessToMobileAndSmartIdServicesText = stringResource(R.string.main_settings_uuid_title)

    // Reset RPUUID when the user navigates away from this screen and has set default choice
    DisposableEffect(Unit) {
        onDispose {
            if (settingsUuidChoice.value == UUIDSetting.DEFAULT.name) {
                setSettingsUuid(DEFAULT_UUID_VALUE)
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SPadding)
                .padding(top = SPadding),
    ) {
        Text(
            text = accessToMobileAndSmartIdServicesText,
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
            label = useDefaultAccessText,
            selected = settingsUuidChoice.value == UUIDSetting.DEFAULT.name,
            onClick = {
                settingsUuidChoice.value = UUIDSetting.DEFAULT.name
                setUuidSetting(UUIDSetting.DEFAULT)
            },
        )

        SettingsRadioCard(
            modifier = modifier,
            label = useManualAccessText,
            selected = settingsUuidChoice.value == UUIDSetting.MANUAL.name,
            onClick = {
                settingsUuidChoice.value = UUIDSetting.MANUAL.name
                setUuidSetting(UUIDSetting.MANUAL)
            },
        ) {
            if (settingsUuidChoice.value == UUIDSetting.MANUAL.name) {
                PrimaryTextField(
                    modifier = Modifier.padding(vertical = LPadding),
                    focusRequester = focusRequester,
                    value = settingsUuid,
                    onValueChange = {
                        settingsUuid = it
                        setSettingsUuid(it.text)
                    },
                    singleLine = true,
                    enabled = settingsUuidChoice.value == UUIDSetting.MANUAL.name,
                    label = accessToMobileAndSmartIdServicesText,
                    isPasswordText = true,
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Password,
                        ),
                    testTag = "mobileIdAndSmartIdServicesComponentTextField",
                    removeIconTestTag = "mobileIdAndSmartIdServicesComponentRemoveIconButton",
                    showIconTestTag = "mobileIdAndSmartIdServicesComponentPasswordVisibleButton",
                )
            }
        }

        InvisibleElement(modifier = modifier)
    }
}
