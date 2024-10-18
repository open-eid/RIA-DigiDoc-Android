@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RolesDetailsView(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val signature = sharedSignatureViewModel.signature.value

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    Scaffold { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("rolesDetailsView"),
        ) {
            Column {
                TopBar(
                    modifier = modifier,
                    title = R.string.signature_update_signature_role_and_address_title_accessibility,
                    onBackButtonClick = {
                        handleBackButtonClick(navController, sharedSignatureViewModel)
                    },
                )
                if (signature != null) {
                    Column(
                        modifier =
                            modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .testTag("scrollView"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val roleTitle = stringResource(id = R.string.main_settings_role_title)
                        val roleValue = signature.signerRoles.joinToString(", ")
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        this.contentDescription = "$roleTitle $roleValue"
                                    }
                                    .testTag("signatureUpdateRoleDetailLabel"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = modifier.padding(screenViewLargePadding),
                            ) {
                                Text(
                                    text = roleTitle,
                                    modifier = modifier.notAccessible(),
                                )
                                Text(
                                    text = roleValue,
                                    modifier =
                                        modifier
                                            .graphicsLayer(alpha = 0.7f)
                                            .notAccessible(),
                                )
                            }
                        }

                        val cityTitle = stringResource(id = R.string.main_settings_city_title)
                        val cityValue = signature.city
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        this.contentDescription = "$cityTitle $cityValue"
                                    }
                                    .testTag("signatureUpdateRoleCityDetailLabel"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = modifier.padding(screenViewLargePadding),
                            ) {
                                Text(
                                    text = cityTitle,
                                    modifier = modifier.notAccessible(),
                                )
                                Text(
                                    text = cityValue,
                                    modifier =
                                        modifier
                                            .graphicsLayer(alpha = 0.7f)
                                            .notAccessible(),
                                )
                            }
                        }

                        val stateTitle = stringResource(id = R.string.main_settings_county_title)
                        val stateValue = signature.stateOrProvince
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        this.contentDescription = "$stateTitle $stateValue"
                                    }
                                    .testTag("signatureUpdateRoleStateDetailLabel"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = modifier.padding(screenViewLargePadding),
                            ) {
                                Text(
                                    text = stateTitle,
                                    modifier = modifier.notAccessible(),
                                )
                                Text(
                                    text = stateValue,
                                    modifier =
                                        modifier
                                            .graphicsLayer(alpha = 0.7f)
                                            .notAccessible(),
                                )
                            }
                        }

                        val countryTitle = stringResource(id = R.string.main_settings_country_title)
                        val countryValue = signature.countryName
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        this.contentDescription = "$countryTitle $countryValue"
                                    }
                                    .testTag("signatureUpdateRoleCountryDetailLabel"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = modifier.padding(screenViewLargePadding),
                            ) {
                                Text(
                                    text = countryTitle,
                                    modifier = modifier.notAccessible(),
                                )
                                Text(
                                    text = countryValue,
                                    modifier =
                                        modifier
                                            .graphicsLayer(alpha = 0.7f)
                                            .notAccessible(),
                                )
                            }
                        }

                        val zipTitle = stringResource(id = R.string.main_settings_postal_code_title)
                        val zipValue = signature.postalCode
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics(mergeDescendants = true) {
                                        this.contentDescription = "$zipTitle ${formatNumbers(zipValue)}"
                                    }
                                    .testTag("signatureUpdateRoleZipDetailLabel"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = modifier.padding(screenViewLargePadding),
                            ) {
                                Text(
                                    text = zipTitle,
                                    modifier = modifier.notAccessible(),
                                )
                                Text(
                                    text = zipValue,
                                    modifier =
                                        modifier
                                            .graphicsLayer(alpha = 0.7f)
                                            .notAccessible(),
                                )
                            }
                        }
                        InvisibleElement(modifier = modifier)
                    }
                }
            }
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
