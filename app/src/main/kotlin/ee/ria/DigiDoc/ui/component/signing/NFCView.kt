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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.CAN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN1_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.SecurePinTextField
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.removeInvisibleElement
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.pin.PinCodeUtil.shouldShowPINCodeError
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.NFCViewModel
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
fun NFCView(
    activity: Activity,
    modifier: Modifier = Modifier,
    identityAction: IdentityAction,
    isSigning: Boolean = false,
    isDecrypting: Boolean = false,
    isAuthenticating: Boolean = false,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    isAddingRoleAndAddress: Boolean = false,
    rememberMe: Boolean = true,
    nfcViewModel: NFCViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    isSupported: (Boolean) -> Unit = {},
    isValidToSign: (Boolean) -> Unit = {},
    isValidToDecrypt: (Boolean) -> Unit = {},
    showPinField: Boolean = true,
    isValidToAuthenticate: (Boolean) -> Unit = {},
    signAction: (() -> Unit) -> Unit = {},
    decryptAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
    cancelDecryptAction: (() -> Unit) -> Unit = {},
    isAuthenticated: (Boolean, IdCardData) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)
    var nfcStatus by remember { mutableStateOf(nfcViewModel.getNFCStatus(activity)) }
    var nfcImage by remember { mutableIntStateOf(R.drawable.ic_icon_nfc) }

    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val personalData by nfcViewModel.userData.asFlow().collectAsState(null)

    val dialogError by nfcViewModel.dialogError.asFlow().collectAsState(0)

    val canNumberLabel = stringResource(id = R.string.signature_update_nfc_can)
    val canNumberLocationText = stringResource(R.string.nfc_sign_can_location)

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    var canNumber by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getCanNumber(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getCanNumber().length),
            ),
        )
    }
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setCanNumber(canNumber.text)
        } else {
            sharedSettingsViewModel.dataStore.setCanNumber("")
        }
    }

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    val canNumberFocusRequester = remember { FocusRequester() }
    val pinNumberFocusRequester = remember { FocusRequester() }
    val canNumberWithInvisibleSpaces = TextFieldValue(addInvisibleElement(canNumber.text))

    val pinCode = remember { mutableStateOf(byteArrayOf()) }

    val pinType =
        if (identityAction == IdentityAction.SIGN) {
            stringResource(id = R.string.signature_id_card_pin2)
        } else {
            stringResource(id = R.string.signature_id_card_pin1)
        }

    val pinCodeLabel = stringResource(id = R.string.signature_update_nfc_pin, pinType)

    val pinMinLength =
        if (identityAction == IdentityAction.SIGN) {
            PIN2_MIN_LENGTH
        } else {
            PIN1_MIN_LENGTH
        }

    val codeType =
        if (identityAction == IdentityAction.SIGN) {
            CodeType.PIN2
        } else {
            CodeType.PIN1
        }

    BackHandler {
        nfcViewModel.handleBackButton()
        if (isSigning || isDecrypting || isAuthenticating) {
            onError()
        } else {
            onSuccess()
        }
    }

    LaunchedEffect(nfcViewModel.shouldResetPIN) {
        nfcViewModel.shouldResetPIN.asFlow().collect { bool ->
            bool.let {
                if (bool) {
                    pinCode.value.fill(0)
                    nfcViewModel.resetShouldResetPIN()
                    pinCode.value = byteArrayOf()
                }
            }
        }
    }

    LaunchedEffect(nfcViewModel.nfcStatus) {
        nfcViewModel.nfcStatus.asFlow().collect { status ->
            status?.let {
                nfcStatus = status
            }
        }
    }

    LaunchedEffect(nfcViewModel.signStatus) {
        nfcViewModel.signStatus.asFlow().collect { signStatus ->
            signStatus?.let {
                sharedContainerViewModel.setSignedNFCStatus(signStatus)
                nfcViewModel.resetSignStatus()
            }
        }
    }

    LaunchedEffect(nfcViewModel.decryptStatus) {
        nfcViewModel.decryptStatus.asFlow().collect { decryptStatus ->
            decryptStatus?.let {
                sharedContainerViewModel.setDecryptNFCStatus(decryptStatus)
                nfcViewModel.resetDecryptStatus()
            }
        }
    }

    LaunchedEffect(nfcViewModel.errorState) {
        nfcViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    pinCode.value.fill(0)
                    if (errorState.first != 0) {
                        errorText =
                            context.getString(
                                errorState.first,
                                errorState.second,
                                errorState.third,
                            )
                    }

                    nfcViewModel.resetErrorState()
                }
            }
        }
    }

    LaunchedEffect(nfcViewModel.signedContainer) {
        nfcViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                pinCode.value.fill(0)
                sharedContainerViewModel.setSignedContainer(it)
                nfcViewModel.resetSignedContainer()
                onSuccess()
            }
        }
    }

    LaunchedEffect(nfcViewModel.cryptoContainer) {
        nfcViewModel.cryptoContainer.asFlow().collect { cryptoContainer ->
            cryptoContainer?.let {
                sharedContainerViewModel.setCryptoContainer(it, true)
                nfcViewModel.resetCryptoContainer()
                onSuccess()
            }
        }
    }

    LaunchedEffect(nfcViewModel.dialogError) {
        pinCode.value.fill(0)
        nfcViewModel.dialogError
            .asFlow()
            .filterNotNull()
            .filterNot { it == 0 }
            .collect {
                withContext(Main) {
                    nfcViewModel.resetErrorState()
                    showErrorDialog.value = true
                }
            }
    }

    LaunchedEffect(Unit) {
        pinCode.value = byteArrayOf()
        nfcViewModel.checkNFCStatus(nfcViewModel.getNFCStatus(activity))
    }

    LaunchedEffect(Unit, isAuthenticating) {
        if (isAuthenticating) {
            saveFormParams()
            nfcViewModel.loadPersonalData(
                activity,
                canNumber.text,
            )
        }
    }

    LaunchedEffect(Unit, personalData, isAuthenticating) {
        if (personalData != null && isAuthenticating && !isSigning) {
            personalData?.let { data ->
                isAuthenticated(true, data)
                nfcViewModel.resetIdCardUserData()
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
        } else if (dialogError == R.string.sign_blocked_pin2_unchanged_message) {
            linkText = R.string.additional_information
            linkUrl = R.string.sign_blocked_pin2_unchanged_url
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
                    nfcViewModel.resetDialogErrorState()
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
                                }.testTag("smartIdErrorContainer"),
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
                                nfcViewModel.resetDialogErrorState()
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
                }.testTag("signatureUpdateNFC"),
    ) {
        if (isAddingRoleAndAddress) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else if (isSigning || isAuthenticating || isDecrypting) {
            NFCSignatureUpdateContainer(
                nfcViewModel = nfcViewModel,
                onError = onError,
            )
        } else {
            LaunchedEffect(Unit, isSupported) {
                isSupported(nfcStatus != NfcStatus.NFC_NOT_SUPPORTED)
            }

            if (nfcStatus !== NfcStatus.NFC_ACTIVE) {
                nfcImage = R.drawable.ic_icon_nfc_not_found

                Image(
                    painter = painterResource(id = nfcImage),
                    contentDescription = null,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .notAccessible()
                            .testTag("signatureUpdateNFCIcon"),
                )
                val nfcStatusText =
                    if (nfcStatus === NfcStatus.NFC_NOT_SUPPORTED) {
                        stringResource(id = R.string.signature_update_nfc_adapter_missing)
                    } else {
                        stringResource(id = R.string.signature_update_nfc_turned_off)
                    }
                Text(
                    text = nfcStatusText,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .semantics {
                                heading()
                                testTagsAsResourceId = true
                            }.testTag("signatureUpdateNFCNotFoundMessage"),
                    textAlign = TextAlign.Center,
                )
            } else {
                nfcImage = R.drawable.ic_icon_nfc

                val isValid =
                    nfcViewModel.positiveButtonEnabled(
                        canNumber.text,
                        pinCode.value,
                        codeType,
                    )

                val isValidForAuthenticating =
                    nfcViewModel.isCANLengthValid(canNumber.text)

                LaunchedEffect(isValid) {
                    isValidToSign(isValid)
                    isValidToDecrypt(isValid)
                }

                LaunchedEffect(Unit, rememberMe) {
                    shouldRememberMe = rememberMe
                }

                LaunchedEffect(isValidForAuthenticating) {
                    isValidToAuthenticate(isValidForAuthenticating)
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
                                nfcViewModel.performNFCSignWorkRequest(
                                    activity = activity,
                                    context = context,
                                    container = signedContainer,
                                    pin2Code = pinCode.value,
                                    canNumber = canNumber.text,
                                    roleData = roleDataRequest,
                                )
                            }
                        }
                        decryptAction {
                            saveFormParams()
                            scope.launch(IO) {
                                nfcViewModel.performNFCDecryptWorkRequest(
                                    activity = activity,
                                    context = context,
                                    container = cryptoContainer,
                                    pin1Code = pinCode.value,
                                    canNumber = canNumber.text,
                                )
                            }
                        }
                        cancelAction {
                            nfcViewModel.handleBackButton()
                            scope.launch(IO) {
                                signedContainer?.let { nfcViewModel.cancelNFCSignWorkRequest(it) }
                            }
                        }
                        cancelDecryptAction {
                            nfcViewModel.handleBackButton()
                            nfcViewModel.cancelNFCDecryptWorkRequest()
                        }
                    }
                }

                val canNumberTextEdited = rememberSaveable { mutableStateOf(false) }
                val canNumberErrorText =
                    if (canNumberTextEdited.value && canNumber.text.isNotEmpty()) {
                        if (nfcViewModel.shouldShowCANNumberError(canNumber.text)) {
                            String.format(
                                stringResource(id = R.string.nfc_sign_can_invalid_length),
                                CAN_LENGTH,
                            )
                        } else {
                            ""
                        }
                    } else {
                        ""
                    }

                Column(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("nfcViewContainer"),
                ) {
                    PrimaryTextField(
                        modifier =
                            Modifier
                                .padding(top = XSPadding)
                                .focusRequester(canNumberFocusRequester)
                                .testTag("nfcCanNumber"),
                        value =
                            if (!isTalkBackEnabled(context)) {
                                canNumber
                            } else {
                                canNumberWithInvisibleSpaces
                            },
                        onValueChange = {
                            canNumberTextEdited.value = true

                            canNumber =
                                if (!isTalkBackEnabled(context)) {
                                    it
                                } else {
                                    TextFieldValue(removeInvisibleElement(it.text))
                                }
                        },
                        singleLine = true,
                        label = canNumberLabel,
                        readDigitByDigit = true,
                        description = canNumberLocationText,
                        onDone = {
                            pinNumberFocusRequester.requestFocus()
                        },
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction =
                                    if (showPinField) {
                                        ImeAction.Next
                                    } else {
                                        ImeAction.Done
                                    },
                                keyboardType = KeyboardType.Number,
                            ),
                        isError =
                            canNumberTextEdited.value &&
                                nfcViewModel.shouldShowCANNumberError(canNumber.text),
                        errorText = canNumberErrorText,
                        testTag = "nfcCanNumberTextField",
                        removeIconTestTag = "nfcCanNumberRemoveIconButton",
                        descriptionTestTag = "nfcCanNumberLocationText",
                        errorTestTag = "nfcCanErrorText",
                    )

                    val pinCodeTextEdited = rememberSaveable { mutableStateOf(false) }
                    val pinCodeErrorText =
                        if (pinCodeTextEdited.value && pinCode.value.isNotEmpty()) {
                            if (shouldShowPINCodeError(
                                    pinCode.value,
                                    codeType,
                                )
                            ) {
                                String.format(
                                    stringResource(id = R.string.id_card_sign_pin_invalid_length),
                                    pinType,
                                    pinMinLength,
                                    PIN_MAX_LENGTH.toString(),
                                )
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }

                    if (showPinField) {
                        SecurePinTextField(
                            modifier =
                                Modifier
                                    .focusRequester(pinNumberFocusRequester)
                                    .focusProperties {
                                        previous = canNumberFocusRequester
                                    }.semantics {
                                        testTagsAsResourceId = true
                                    }.testTag("nfcPinTextField"),
                            pin = pinCode,
                            label = pinCodeLabel,
                            pinCodeTextEdited = pinCodeTextEdited,
                            isError =
                                pinCodeTextEdited.value &&
                                    shouldShowPINCodeError(
                                        pinCode.value,
                                        codeType,
                                    ),
                            errorText = pinCodeErrorText,
                            removeIconTestTag = "nfcPinRemoveButton",
                            errorTestTag = "nfcPinError",
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NFCViewPreview() {
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        NFCView(
            activity = LocalActivity.current as Activity,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            identityAction = IdentityAction.SIGN,
            isSigning = false,
            isAuthenticating = false,
            isAddingRoleAndAddress = false,
            rememberMe = true,
            isSupported = {},
            isValidToSign = {},
            isValidToAuthenticate = {},
            isAuthenticated = { _, _ -> {} },
        )
    }
}
