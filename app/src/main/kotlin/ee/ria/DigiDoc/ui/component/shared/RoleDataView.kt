@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
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
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RoleDataView(
    modifier: Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    onDismiss: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

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

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

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
                }
                .testTag("roleAndAddressViewContainer"),
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
                    }
                    .focusable(enabled = true)
                    .focusTarget()
                    .focusProperties { canFocus = true },
            textAlign = TextAlign.Start,
        )

        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = MPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                label = {
                    Text(text = roleLabel)
                },
                value = rolesAndResolutions,
                singleLine = true,
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
                modifier =
                    modifier
                        .focusRequester(roleFocusRequester)
                        .focusProperties {
                            next = cityFocusRequester
                        }
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        }
                        .testTag("roleAndAddressRoleTextField"),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && rolesAndResolutions.text.isNotEmpty()) {
                        IconButton(onClick = {
                            rolesAndResolutions = TextFieldValue("")
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                    ),
            )
            if (isTalkBackEnabled(context) && rolesAndResolutions.text.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .align(Alignment.CenterVertically),
                    onClick = {
                        rolesAndResolutions = TextFieldValue("")
                        scope.launch(Main) {
                            roleFocusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            roleFocusRequester.requestFocus()
                        }
                    },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("roleAndAddressRoleRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "$clearButtonText $buttonName",
                    )
                }
            }
        }
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = MPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                label = {
                    Text(text = cityLabel)
                },
                value = city,
                singleLine = true,
                onValueChange = {
                    city = it
                    sharedSettingsViewModel.dataStore.setRoleCity(city.text)
                },
                modifier =
                    modifier
                        .focusRequester(cityFocusRequester)
                        .focusProperties {
                            previous = roleFocusRequester
                            next = stateFocusRequester
                        }
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        }
                        .testTag("roleAndAddressCityTextField"),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && city.text.isNotEmpty()) {
                        IconButton(onClick = {
                            city = TextFieldValue("")
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                    ),
            )
            if (isTalkBackEnabled(context) && city.text.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .align(Alignment.CenterVertically),
                    onClick = {
                        city = TextFieldValue("")
                        scope.launch(Main) {
                            cityFocusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            cityFocusRequester.requestFocus()
                        }
                    },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("roleAndAddressCityRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "$clearButtonText $buttonName",
                    )
                }
            }
        }
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = MPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                label = {
                    Text(text = stateLabel)
                },
                value = state,
                singleLine = true,
                onValueChange = {
                    state = it
                    sharedSettingsViewModel.dataStore.setRoleState(state.text)
                },
                modifier =
                    modifier
                        .focusRequester(stateFocusRequester)
                        .focusProperties {
                            previous = cityFocusRequester
                            next = countryFocusRequester
                        }
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        }
                        .testTag("roleAndAddressStateTextField"),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && state.text.isNotEmpty()) {
                        IconButton(onClick = {
                            state = TextFieldValue("")
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                    ),
            )
            if (isTalkBackEnabled(context) && state.text.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .align(Alignment.CenterVertically),
                    onClick = {
                        state = TextFieldValue("")
                        scope.launch(Main) {
                            stateFocusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            stateFocusRequester.requestFocus()
                        }
                    },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("roleAndAddressStateRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "$clearButtonText $buttonName",
                    )
                }
            }
        }
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = MPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                label = {
                    Text(text = countryLabel)
                },
                value = country,
                singleLine = true,
                onValueChange = {
                    country = it
                    sharedSettingsViewModel.dataStore.setRoleCountry(country.text)
                },
                modifier =
                    modifier
                        .focusRequester(countryFocusRequester)
                        .focusProperties {
                            previous = stateFocusRequester
                            next = zipFocusRequester
                        }
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        }
                        .testTag("roleAndAddressCountryTextField"),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && country.text.isNotEmpty()) {
                        IconButton(onClick = {
                            country = TextFieldValue("")
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text,
                    ),
            )
            if (isTalkBackEnabled(context) && country.text.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .align(Alignment.CenterVertically),
                    onClick = {
                        country = TextFieldValue("")
                        scope.launch(Main) {
                            countryFocusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            countryFocusRequester.requestFocus()
                        }
                    },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("roleAndAddressCountryRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "$clearButtonText $buttonName",
                    )
                }
            }
        }
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = MPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                label = {
                    Text(text = zipLabel)
                },
                value = zip,
                singleLine = true,
                onValueChange = {
                    zip = it
                    sharedSettingsViewModel.dataStore.setRoleZip(zip.text)
                },
                modifier =
                    modifier
                        .focusRequester(zipFocusRequester)
                        .focusProperties {
                            previous = countryFocusRequester
                        }
                        .weight(1f)
                        .semantics(mergeDescendants = true) {
                            testTagsAsResourceId = true
                        }
                        .testTag("roleAndAddressZipTextField"),
                trailingIcon = {
                    if (!isTalkBackEnabled(context) && zip.text.isNotEmpty()) {
                        IconButton(onClick = {
                            zip = TextFieldValue("")
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    ),
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Ascii,
                    ),
            )
            if (isTalkBackEnabled(context) && zip.text.isNotEmpty()) {
                IconButton(
                    modifier =
                        modifier
                            .align(Alignment.CenterVertically),
                    onClick = {
                        zip = TextFieldValue("")
                        scope.launch(Main) {
                            zipFocusRequester.requestFocus()
                            focusManager.clearFocus()
                            delay(200)
                            zipFocusRequester.requestFocus()
                        }
                    },
                ) {
                    Icon(
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("roleAndAddressZipRemoveIconButton"),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                        contentDescription = "$clearButtonText $buttonName",
                    )
                }
            }
        }
    }
}
