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

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.removeInvisibleElement
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MobileIdView(
    activity: Activity,
    modifier: Modifier = Modifier,
    isSigning: Boolean,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    isAddingRoleAndAddress: Boolean,
    rememberMe: Boolean,
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val dialogError by mobileIdViewModel.dialogError.asFlow().collectAsState(0)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    val focusManager = LocalFocusManager.current

    var countryCodeAndPhone by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPhoneNo(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPhoneNo().length),
            ),
        )
    }
    var personalCode by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPersonalCode().length),
            ),
        )
    }

    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setPhoneNo(countryCodeAndPhone.text)
            sharedSettingsViewModel.dataStore.setPersonalCode(personalCode.text)
        } else {
            sharedSettingsViewModel.dataStore.setPhoneNo("372")
            sharedSettingsViewModel.dataStore.setPersonalCode("")
        }
    }

    val countryCodeAndPhoneEdited = rememberSaveable { mutableStateOf(false) }
    val countryCodeAndPhoneErrorText =
        if (countryCodeAndPhoneEdited.value && countryCodeAndPhone.text.isNotEmpty()) {
            if (mobileIdViewModel.isCountryCodeMissing(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_status_no_country_code)
            } else if (!mobileIdViewModel.isCountryCodeCorrect(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_country_code)
            } else if (!mobileIdViewModel.isPhoneNumberCorrect(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_phone_number)
            } else {
                ""
            }
        } else {
            ""
        }

    val personalCodeEdited = rememberSaveable { mutableStateOf(false) }
    val personalCodeErrorText =
        if (personalCodeEdited.value && personalCode.text.isNotEmpty()) {
            if (!PersonalCodeValidator.isPersonalCodeValid(personalCode.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)
            } else {
                ""
            }
        } else {
            ""
        }

    val phoneNumberFocusRequester = remember { FocusRequester() }
    val personalCodeFocusRequester = remember { FocusRequester() }

    val phoneNumberWithInvisibleSpaces = TextFieldValue(addInvisibleElement(countryCodeAndPhone.text))
    val personalCodeWithInvisibleSpaces = TextFieldValue(addInvisibleElement(personalCode.text))

    BackHandler {
        if (isSigning) {
            onError()
        } else {
            onSuccess()
        }
    }

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
                withContext(Main) {
                    if (errorState != "") {
                        errorText = errorState
                    }

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
                onSuccess()
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.dialogError) {
        mobileIdViewModel.dialogError
            .asFlow()
            .filterNotNull()
            .filterNot { it == 0 }
            .collect {
                withContext(Main) {
                    mobileIdViewModel.resetErrorState()
                    showErrorDialog.value = true
                }
            }
    }

    if (errorText.isNotEmpty()) {
        showMessage(errorText)
        errorText = ""
    }

    if (showErrorDialog.value) {
        var text1Arg: Int? = null
        val text2 = null
        var linkText = 0
        var linkUrl = 0
        if (dialogError == R.string.too_many_requests_message) {
            text1Arg = R.string.id_card_conditional_speech
            linkText = R.string.additional_information
            linkUrl = R.string.too_many_requests_url
        } else if (dialogError == R.string.invalid_time_slot_message) {
            linkText = R.string.additional_information
            linkUrl = R.string.invalid_time_slot_url
        }
        Box(modifier = modifier.fillMaxSize()) {
            onError()
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface),
                onDismissRequest = {
                    showErrorDialog.value = false
                    mobileIdViewModel.resetDialogErrorState()
                },
            ) {
                Surface(
                    modifier =
                        modifier
                            .padding(SPadding)
                            .wrapContentHeight()
                            .wrapContentWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Column(
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("mobileIdErrorContainer"),
                    ) {
                        HrefMessageDialog(
                            modifier = modifier,
                            text1 = dialogError,
                            text1Arg = text1Arg,
                            text2 = text2,
                            linkText = linkText,
                            linkUrl = linkUrl,
                            newLineBeforeLink = true,
                        )

                        CancelAndOkButtonRow(
                            okButtonTestTag = "hrefMessageDialogOkButton",
                            cancelButtonTestTag = "hrefMessageDialogCancelButton",
                            cancelButtonClick = {},
                            okButtonClick = {
                                showErrorDialog.value = false
                                mobileIdViewModel.resetDialogErrorState()
                            },
                            cancelButtonTitle = R.string.cancel_button,
                            okButtonTitle = R.string.ok_button,
                            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
                            okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
                            showCancelButton = false,
                        )
                    }
                }
            }
            InvisibleElement(modifier = modifier)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }.semantics {
                    testTagsAsResourceId = true
                }.testTag("signatureUpdateMobileId"),
    ) {
        if (isAddingRoleAndAddress) {
            RoleDataView(modifier, sharedSettingsViewModel, onError)
        } else if (isSigning) {
            MobileIdSignatureUpdateContainer(
                mobileIdViewModel = mobileIdViewModel,
                onError = onError,
            )
        } else {
            val isValid =
                countryCodeAndPhone.text.isNotEmpty() &&
                    personalCode.text.isNotEmpty() &&
                    PersonalCodeValidator.isPersonalCodeValid(personalCode.text)

            LaunchedEffect(isValid) {
                isValidToSign(isValid)
            }

            LaunchedEffect(Unit, rememberMe) {
                shouldRememberMe = rememberMe
            }

            LaunchedEffect(Unit, isValid) {
                if (isValid) {
                    signAction {
                        saveFormParams()
                        var roleDataRequest: RoleData? = null
                        if (getSettingsAskRoleAndAddress()) {
                            val roles = sharedSettingsViewModel.dataStore.getRoles()
                            val rolesList =
                                roles
                                    .split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                    .toList()
                            val city = sharedSettingsViewModel.dataStore.getRoleCity()
                            val state = sharedSettingsViewModel.dataStore.getRoleState()
                            val country = sharedSettingsViewModel.dataStore.getRoleCountry()
                            val zip = sharedSettingsViewModel.dataStore.getRoleZip()

                            roleDataRequest =
                                RoleData(
                                    roles = rolesList,
                                    city = city,
                                    state = state,
                                    country = country,
                                    zip = zip,
                                )
                        }
                        scope.launch(IO) {
                            mobileIdViewModel.performMobileIdWorkRequest(
                                activity = activity,
                                context = context,
                                displayMessage = displayMessage,
                                container = signedContainer,
                                personalCode = personalCode.text,
                                phoneNumber = countryCodeAndPhone.text,
                                roleData = roleDataRequest,
                            )
                        }
                    }
                    cancelAction {
                        mobileIdViewModel.cancelMobileIdWorkRequest(signedContainer)
                    }
                }
            }

            Column(
                modifier =
                    modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag("mobileIdViewContainer"),
            ) {
                PrimaryTextField(
                    modifier =
                        modifier
                            .padding(top = XSPadding)
                            .focusRequester(phoneNumberFocusRequester)
                            .semantics(mergeDescendants = true) {
                                testTagsAsResourceId = true
                            }.testTag("signatureUpdateMobileIdPhoneNo"),
                    value =
                        if (!isTalkBackEnabled(context)) {
                            countryCodeAndPhone
                        } else {
                            phoneNumberWithInvisibleSpaces
                        },
                    onValueChange = {
                        countryCodeAndPhoneEdited.value = true

                        countryCodeAndPhone =
                            if (!isTalkBackEnabled(context)) {
                                it
                            } else {
                                TextFieldValue(removeInvisibleElement(it.text))
                            }
                    },
                    singleLine = true,
                    label = countryCodeAndPhoneNumberLabel,
                    placeholder =
                        stringResource(
                            id = R.string.mobile_id_country_code_and_phone_number_placeholder,
                        ),
                    readDigitByDigit = true,
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Decimal,
                        ),
                    onDone = {
                        personalCodeFocusRequester.requestFocus()
                    },
                    isError =
                        countryCodeAndPhoneEdited.value &&
                            !mobileIdViewModel.isPhoneNumberValid(countryCodeAndPhone.text),
                    errorText = countryCodeAndPhoneErrorText,
                    testTag = "signatureUpdateMobileIdPhoneNo",
                    removeIconTestTag = "mobileIdCountryCodeAndPhoneNumberRemoveIconButton",
                    errorTestTag = "mobileIdPhoneNoErrorText",
                )

                PrimaryTextField(
                    modifier =
                        Modifier
                            .padding(top = MSPadding)
                            .focusRequester(personalCodeFocusRequester)
                            .focusProperties {
                                previous = phoneNumberFocusRequester
                            },
                    value =
                        if (!isTalkBackEnabled(context)) {
                            personalCode
                        } else {
                            personalCodeWithInvisibleSpaces
                        },
                    onValueChange = {
                        personalCodeEdited.value = true

                        personalCode =
                            if (!isTalkBackEnabled(context)) {
                                it
                            } else {
                                TextFieldValue(removeInvisibleElement(it.text))
                            }
                    },
                    singleLine = true,
                    label = personalCodeLabel,
                    readDigitByDigit = true,
                    isError =
                        personalCodeEdited.value &&
                            !mobileIdViewModel.isPersonalCodeValid(
                                personalCode.text,
                            ),
                    errorText = personalCodeErrorText,
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number,
                        ),
                    testTag = "mobileIdPersonalCode",
                    removeIconTestTag = "mobileIdPersonalCodeRemoveIconButton",
                    errorTestTag = "mobileIdPersonalCodeErrorText",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MobileIdViewPreview() {
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        MobileIdView(
            activity = LocalActivity.current as Activity,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            isSigning = false,
            isAddingRoleAndAddress = false,
            rememberMe = true,
            isValidToSign = {},
        )
    }
}
