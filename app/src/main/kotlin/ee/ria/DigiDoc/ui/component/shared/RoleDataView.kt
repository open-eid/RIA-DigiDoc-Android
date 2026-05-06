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

package ee.ria.DigiDoc.ui.component.shared

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoleDataView(
    modifier: Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    onDismiss: () -> Unit = {},
) {
    val roleAndAddressTitleFocusRequester = remember { FocusRequester() }
    val roleFocusRequester = remember { FocusRequester() }
    val cityFocusRequester = remember { FocusRequester() }
    val stateFocusRequester = remember { FocusRequester() }
    val countryFocusRequester = remember { FocusRequester() }
    val zipFocusRequester = remember { FocusRequester() }

    val roleLabel = stringResource(id = R.string.main_settings_role_title)
    val cityLabel = stringResource(id = R.string.main_settings_city_title)
    val stateLabel = stringResource(id = R.string.main_settings_county_title)
    val countryLabel = stringResource(id = R.string.main_settings_country_title)
    val zipLabel = stringResource(id = R.string.main_settings_postal_code_title)

    var rolesAndResolutions by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoles()))
    }
    var city by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleCity()))
    }
    var state by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleState()))
    }
    var country by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleCountry()))
    }
    var zip by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleZip()))
    }

    BackHandler {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        roleAndAddressTitleFocusRequester.requestFocus()
    }

    Column(
        modifier =
            modifier
                .padding(vertical = SPadding)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("roleAndAddressViewContainer"),
    ) {
        Text(
            text = stringResource(id = R.string.signature_update_signature_role_and_address_info_title),
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .focusRequester(roleAndAddressTitleFocusRequester)
                    .padding(vertical = SPadding)
                    .semantics {
                        heading()
                    }.focusable(enabled = true)
                    .focusTarget()
                    .focusProperties { canFocus = true },
            textAlign = TextAlign.Start,
        )

        PrimaryTextField(
            modifier =
                Modifier
                    .padding(top = MPadding)
                    .focusRequester(roleFocusRequester),
            value = rolesAndResolutions,
            onValueChange = { rolesValue ->
                rolesAndResolutions = rolesValue

                val roles =
                    rolesAndResolutions.text
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString(", ")
                sharedSettingsViewModel.dataStore.setRoles(roles)
            },
            singleLine = true,
            label = roleLabel,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                ),
            onDone = {
                cityFocusRequester.requestFocus()
            },
            testTag = "roleAndAddressRoleTextField",
            removeIconTestTag = "roleAndAddressRoleRemoveIconButton",
        )

        PrimaryTextField(
            modifier = Modifier.padding(top = MPadding),
            value = city,
            onValueChange = {
                city = it
                sharedSettingsViewModel.dataStore.setRoleCity(city.text)
            },
            singleLine = true,
            label = cityLabel,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                ),
            onDone = {
                stateFocusRequester.requestFocus()
            },
            testTag = "roleAndAddressCityTextField",
            removeIconTestTag = "roleAndAddressCityRemoveIconButton",
        )

        PrimaryTextField(
            modifier = Modifier.padding(top = MPadding),
            value = state,
            onValueChange = {
                state = it
                sharedSettingsViewModel.dataStore.setRoleState(state.text)
            },
            singleLine = true,
            label = stateLabel,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                ),
            onDone = {
                countryFocusRequester.requestFocus()
            },
            testTag = "roleAndAddressStateTextField",
            removeIconTestTag = "roleAndAddressStateRemoveIconButton",
        )

        PrimaryTextField(
            modifier = Modifier.padding(top = MPadding),
            value = country,
            onValueChange = {
                country = it
                sharedSettingsViewModel.dataStore.setRoleCountry(country.text)
            },
            singleLine = true,
            label = countryLabel,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Text,
                ),
            onDone = {
                zipFocusRequester.requestFocus()
            },
            testTag = "roleAndAddressCountryTextField",
            removeIconTestTag = "roleAndAddressCountryRemoveIconButton",
        )

        PrimaryTextField(
            modifier = Modifier.padding(top = MPadding),
            value = zip,
            onValueChange = {
                zip = it
                sharedSettingsViewModel.dataStore.setRoleZip(zip.text)
            },
            singleLine = true,
            label = zipLabel,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Ascii,
                ),
            testTag = "roleAndAddressZipTextField",
            removeIconTestTag = "roleAndAddressZipRemoveIconButton",
        )
    }
}
