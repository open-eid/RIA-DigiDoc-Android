@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays
import java.util.stream.Collectors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileIdView(
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    cancelButtonClick: () -> Unit = {},
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)

    val roleDataRequested by mobileIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

    val roleLabel = stringResource(id = R.string.main_settings_role_title)
    val cityLabel = stringResource(id = R.string.main_settings_city_title)
    val stateLabel = stringResource(id = R.string.main_settings_county_title)
    val countryLabel = stringResource(id = R.string.main_settings_country_title)
    val zipLabel = stringResource(id = R.string.main_settings_postal_code_title)

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var countryCodeAndPhoneText by remember {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPhoneNo(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPhoneNo().length),
            ),
        )
    }
    var personalCodeText by remember {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPersonalCode().length),
            ),
        )
    }
    val rememberMeCheckedState = remember { mutableStateOf(true) }

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

    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

    LaunchedEffect(mobileIdViewModel.status) {
        mobileIdViewModel.status.asFlow().collect { status ->
            status?.let {
                sharedContainerViewModel.setSignedMidStatus(status)
                mobileIdViewModel.resetStatus()
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.errorState) {
        mobileIdViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
                    mobileIdViewModel.resetErrorState()
                }
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.signedContainer) {
        mobileIdViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                mobileIdViewModel.resetSignedContainer()
                mobileIdViewModel.resetRoleDataRequested()
                cancelButtonClick()
            }
        }
    }
    val openSignatureUpdateContainerDialog = remember { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
    }
    if (openSignatureUpdateContainerDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissSignatureUpdateContainerDialog,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(screenViewLargePadding),
            ) {
                MobileIdSignatureUpdateContainer(
                    modifier = modifier,
                    mobileIdViewModel = mobileIdViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        mobileIdViewModel.cancelMobileIdWorkRequest(signedContainer)
                    },
                )
            }
        }
    }
    Column(
        modifier =
            modifier
                .padding(horizontal = screenViewLargePadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            Text(
                text = stringResource(id = R.string.signature_update_signature_role_and_address_info_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(screenViewLargePadding),
                textAlign = TextAlign.Center,
            )
            Text(
                text = roleLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .semantics {
                            contentDescription =
                                "$roleLabel ${formatNumbers(rolesAndResolutionsText.text)}"
                        },
                value = rolesAndResolutionsText,
                shape = RectangleShape,
                onValueChange = {
                    rolesAndResolutionsText = it
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
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .semantics {
                            contentDescription =
                                "$cityLabel ${formatNumbers(cityText.text)}"
                        },
                value = cityText,
                shape = RectangleShape,
                onValueChange = {
                    cityText = it
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
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .semantics {
                            contentDescription =
                                "$stateLabel ${formatNumbers(stateText.text)}"
                        },
                value = stateText,
                shape = RectangleShape,
                onValueChange = {
                    stateText = it
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
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .semantics {
                            contentDescription =
                                "$countryLabel ${formatNumbers(countryText.text)}"
                        },
                value = countryText,
                shape = RectangleShape,
                onValueChange = {
                    countryText = it
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
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .semantics {
                            contentDescription =
                                "$zipLabel ${formatNumbers(zipText.text)}"
                        },
                value = zipText,
                shape = RectangleShape,
                onValueChange = {
                    zipText = it
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
        } else {
            SignatureAddRadioGroup(
                modifier = modifier,
                navController = signatureAddController,
                selectedRadioItem = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
                sharedSettingsViewModel = sharedSettingsViewModel,
            )
            Text(
                text = stringResource(id = R.string.signature_update_mobile_id_message),
                style = MaterialTheme.typography.titleLarge,
                modifier = modifier.padding(screenViewLargePadding),
                textAlign = TextAlign.Center,
            )
            Text(
                text = countryCodeAndPhoneNumberLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(vertical = screenViewLargePadding)
                        .notAccessible(),
            )
            val countryCodeAndPhoneTextEdited = remember { mutableStateOf(false) }
            val countryCodeAndPhoneErrorText =
                if (countryCodeAndPhoneTextEdited.value && countryCodeAndPhoneText.text.isNotEmpty()) {
                    if (mobileIdViewModel.isCountryCodeMissing(countryCodeAndPhoneText.text)) {
                        stringResource(id = R.string.signature_update_mobile_id_status_no_country_code)
                    } else if (!mobileIdViewModel.isCountryCodeCorrect(countryCodeAndPhoneText.text)) {
                        stringResource(id = R.string.signature_update_mobile_id_invalid_country_code)
                    } else if (!mobileIdViewModel.isPhoneNumberCorrect(countryCodeAndPhoneText.text)) {
                        stringResource(id = R.string.signature_update_mobile_id_invalid_phone_number)
                    } else {
                        ""
                    }
                } else {
                    ""
                }
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            contentDescription =
                                "$countryCodeAndPhoneNumberLabel " +
                                "${formatNumbers(countryCodeAndPhoneText.text)} "
                        },
                value = countryCodeAndPhoneText,
                shape = RectangleShape,
                onValueChange = {
                    countryCodeAndPhoneText = it
                    countryCodeAndPhoneTextEdited.value = true
                },
                maxLines = 1,
                singleLine = true,
                isError =
                    countryCodeAndPhoneTextEdited.value &&
                        !mobileIdViewModel.isPhoneNumberValid(countryCodeAndPhoneText.text),
                label = {
                    Text(
                        modifier = modifier.notAccessible(),
                        text = stringResource(id = R.string.mobile_id_country_code_and_phone_number_placeholder),
                        color = Blue500,
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Decimal,
                    ),
            )
            if (countryCodeAndPhoneErrorText.isNotEmpty()) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .focusable(enabled = true)
                            .semantics { contentDescription = countryCodeAndPhoneErrorText },
                    text = countryCodeAndPhoneErrorText,
                    color = Red500,
                )
            }
            Text(
                text = personalCodeLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .notAccessible(),
            )
            val personalCodeErrorText =
                if (personalCodeText.text.isNotEmpty()) {
                    if (!mobileIdViewModel.isPersonalCodeCorrect(personalCodeText.text)) {
                        stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)
                    } else {
                        ""
                    }
                } else {
                    ""
                }

            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .clearAndSetSemantics {
                            contentDescription =
                                "$personalCodeLabel ${formatNumbers(personalCodeText.text)}"
                        },
                value = personalCodeText,
                shape = RectangleShape,
                onValueChange = {
                    personalCodeText = it
                },
                maxLines = 1,
                singleLine = true,
                isError = !mobileIdViewModel.isPersonalCodeValid(personalCodeText.text),
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Decimal,
                    ),
            )
            if (personalCodeErrorText.isNotEmpty()) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .focusable(enabled = true)
                            .semantics { contentDescription = personalCodeErrorText },
                    text = personalCodeErrorText,
                    color = Red500,
                )
            }
            TextCheckBox(
                checked = rememberMeCheckedState.value,
                onCheckedChange = { rememberMeCheckedState.value = it },
                title = stringResource(id = R.string.signature_update_remember_me),
                contentDescription = stringResource(id = R.string.signature_update_remember_me).lowercase(),
            )
        }
        CancelAndOkButtonRow(
            okButtonEnabled =
                mobileIdViewModel.positiveButtonEnabled(
                    countryCodeAndPhoneText.text,
                    personalCodeText.text,
                ),
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
            cancelButtonClick =
                {
                    mobileIdViewModel.resetRoleDataRequested()
                    cancelButtonClick()
                },
            okButtonClick = {
                if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                    mobileIdViewModel.setRoleDataRequested(true)
                } else {
                    openSignatureUpdateContainerDialog.value = true
                    if (rememberMeCheckedState.value) {
                        sharedSettingsViewModel.dataStore.setPhoneNo(countryCodeAndPhoneText.text)
                        sharedSettingsViewModel.dataStore.setPersonalCode(personalCodeText.text)
                    }

                    var roleDataRequest: RoleData? = null
                    if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
                        val roles =
                            Arrays.stream(
                                rolesAndResolutionsText.text.split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray(),
                            )
                                .map { obj: String -> obj.trim { it <= ' ' } }
                                .collect(Collectors.toList())

                        sharedSettingsViewModel.dataStore.setRoles(rolesAndResolutionsText.text)
                        sharedSettingsViewModel.dataStore.setRoleCity(cityText.text)
                        sharedSettingsViewModel.dataStore.setRoleState(stateText.text)
                        sharedSettingsViewModel.dataStore.setRoleCountry(countryText.text)
                        sharedSettingsViewModel.dataStore.setRoleZip(zipText.text)

                        roleDataRequest =
                            RoleData(
                                roles = roles,
                                city = cityText.text,
                                state = stateText.text,
                                country = countryText.text,
                                zip = zipText.text,
                            )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        mobileIdViewModel.performMobileIdWorkRequest(
                            context = context,
                            displayMessage = displayMessage,
                            container = signedContainer,
                            personalCode = personalCodeText.text,
                            phoneNumber = countryCodeAndPhoneText.text,
                            roleData = roleDataRequest,
                        )
                        mobileIdViewModel.resetRoleDataRequested()
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MobileIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        MobileIdView(
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
