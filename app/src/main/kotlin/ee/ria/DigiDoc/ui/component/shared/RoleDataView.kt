@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoleDataView(
    modifier: Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var roleAndAddressHeadingTextLoaded by remember { mutableStateOf(false) }

    val roleLabel = stringResource(id = R.string.main_settings_role_title)
    val cityLabel = stringResource(id = R.string.main_settings_city_title)
    val stateLabel = stringResource(id = R.string.main_settings_county_title)
    val countryLabel = stringResource(id = R.string.main_settings_country_title)
    val zipLabel = stringResource(id = R.string.main_settings_postal_code_title)

    var rolesAndResolutionsText by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoles()))
    }
    var cityText by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleCity()))
    }
    var stateText by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleState()))
    }
    var countryText by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleCountry()))
    }
    var zipText by remember {
        mutableStateOf(TextFieldValue(text = sharedSettingsViewModel.dataStore.getRoleZip()))
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        Text(
            text = stringResource(id = R.string.signature_update_signature_role_and_address_info_title),
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(screenViewSmallPadding)
                    .semantics {
                        heading()
                    }
                    .focusRequester(focusRequester)
                    .focusable(enabled = true)
                    .focusTarget()
                    .focusProperties { canFocus = true }
                    .onGloballyPositioned {
                        if (!roleAndAddressHeadingTextLoaded) {
                            CoroutineScope(Dispatchers.Main).launch {
                                focusRequester.requestFocus()
                                focusManager.clearFocus()
                                delay(200)
                                focusRequester.requestFocus()
                                roleAndAddressHeadingTextLoaded = true
                            }
                        }
                    },
            textAlign = TextAlign.Center,
        )
        Text(
            text = roleLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                    .notAccessible(),
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewSmallPadding)
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "signatureUpdateRoleText"
                        this.contentDescription =
                            "$roleLabel ${rolesAndResolutionsText.text}"
                    },
            value = rolesAndResolutionsText,
            shape = RectangleShape,
            onValueChange = { rolesValue ->
                rolesAndResolutionsText = rolesValue

                val roles =
                    rolesAndResolutionsText.text
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString(", ")
                sharedSettingsViewModel.dataStore.setRoles(roles)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        Text(
            text = cityLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                    .focusable(false)
                    .testTag("signatureUpdateRoleCityLabel"),
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewSmallPadding)
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "signatureUpdateRoleCityText"
                        this.contentDescription =
                            "$cityLabel ${cityText.text}"
                    },
            value = cityText,
            shape = RectangleShape,
            onValueChange = {
                cityText = it
                sharedSettingsViewModel.dataStore.setRoleCity(cityText.text)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        Text(
            text = stateLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                    .focusable(false)
                    .testTag("signatureUpdateRoleStateLabel"),
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewSmallPadding)
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "signatureUpdateRoleStateText"
                        this.contentDescription =
                            "$stateLabel ${stateText.text}"
                    },
            value = stateText,
            shape = RectangleShape,
            onValueChange = {
                stateText = it

                sharedSettingsViewModel.dataStore.setRoleState(stateText.text)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        Text(
            text = countryLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                    .focusable(false)
                    .testTag("signatureUpdateRoleCountryLabel"),
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewSmallPadding)
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "signatureUpdateRoleCountryText"
                        this.contentDescription =
                            "$countryLabel ${countryText.text}"
                    },
            value = countryText,
            shape = RectangleShape,
            onValueChange = {
                countryText = it

                sharedSettingsViewModel.dataStore.setRoleCountry(countryText.text)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Ascii,
                ),
        )
        Text(
            text = zipLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                    .focusable(false)
                    .testTag("signatureUpdateRoleZipLabel"),
        )
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewSmallPadding)
                    .clearAndSetSemantics {
                        testTagsAsResourceId = true
                        testTag = "signatureUpdateRoleZipText"
                        this.contentDescription =
                            "$zipLabel ${formatNumbers(zipText.text)}"
                    },
            value = zipText,
            shape = RectangleShape,
            onValueChange = {
                zipText = it

                sharedSettingsViewModel.dataStore.setRoleZip(zipText.text)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Ascii,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    },
                ),
        )
    }
}
