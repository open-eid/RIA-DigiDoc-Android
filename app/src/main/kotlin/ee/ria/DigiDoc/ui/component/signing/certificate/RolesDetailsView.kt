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

package ee.ria.DigiDoc.ui.component.signing.certificate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RolesDetailsView(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    val signature = sharedSignatureViewModel.signature.value

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = XSPadding)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("roleDetailsView"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (signature != null) {
            val roleTitle = stringResource(id = R.string.main_settings_role_title)
            val roleValue = signature.signerRoles.joinToString(", ")

            SignatureDataItem(
                modifier = modifier,
                icon = 0,
                isLink = false,
                testTag = "roleDetailsViewRole",
                detailKey = R.string.main_settings_role_title,
                detailValue = roleValue,
                certificate = null,
                contentDescription = "$roleTitle $roleValue",
                formatForAccessibility = false,
            )

            HorizontalDivider()

            val cityTitle = stringResource(id = R.string.main_settings_city_title)
            val cityValue = signature.city

            SignatureDataItem(
                modifier = modifier,
                icon = 0,
                isLink = false,
                testTag = "roleDetailsViewCity",
                detailKey = R.string.main_settings_city_title,
                detailValue = cityValue,
                certificate = null,
                contentDescription = "$cityTitle $cityValue",
                formatForAccessibility = false,
            )

            HorizontalDivider()

            val stateTitle = stringResource(id = R.string.main_settings_county_title)
            val stateValue = signature.stateOrProvince

            SignatureDataItem(
                modifier = modifier,
                icon = 0,
                isLink = false,
                testTag = "roleDetailsViewState",
                detailKey = R.string.main_settings_county_title,
                detailValue = stateValue,
                certificate = null,
                contentDescription = "$stateTitle $stateValue",
                formatForAccessibility = false,
            )

            HorizontalDivider()

            val countryTitle = stringResource(id = R.string.main_settings_country_title)
            val countryValue = signature.countryName

            SignatureDataItem(
                modifier = modifier,
                icon = 0,
                isLink = false,
                testTag = "roleDetailsViewCountry",
                detailKey = R.string.main_settings_country_title,
                detailValue = countryValue,
                certificate = null,
                contentDescription = "$countryTitle $countryValue",
                formatForAccessibility = false,
            )

            HorizontalDivider()

            val zipTitle = stringResource(id = R.string.main_settings_postal_code_title)
            val zipValue = signature.postalCode

            SignatureDataItem(
                modifier = modifier,
                icon = 0,
                isLink = false,
                testTag = "roleDetailsViewZip",
                detailKey = R.string.main_settings_postal_code_title,
                detailValue = zipValue,
                certificate = null,
                contentDescription = "$zipTitle $zipValue",
                formatForAccessibility = false,
            )

            HorizontalDivider()

            InvisibleElement(modifier = modifier)
        }
    }
}

private fun handleBackButtonClick(
    navController: NavController,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    sharedSignatureViewModel.resetSignature()
    navController.navigateUp()
}
