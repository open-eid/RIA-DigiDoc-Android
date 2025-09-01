@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.SecurePinTextField
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.removeInvisibleElement
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.NFCViewModel
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
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
    isWebEidAuthenticating: Boolean = false,
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
    isValidToWebEidAuthenticate: (Boolean) -> Unit = {},
    showPinField: Boolean = true,
    isValidToAuthenticate: (Boolean) -> Unit = {},
    signAction: (() -> Unit) -> Unit = {},
    decryptAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
    cancelDecryptAction: (() -> Unit) -> Unit = {},
    authenticateWebEidAction: (() -> Unit) -> Unit = {},
    cancelWebEidAuthenticateAction: (() -> Unit) -> Unit = {},
    isAuthenticated: (Boolean, IdCardData) -> Unit,
    webEidViewModel: WebEidViewModel? = null,
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

    var pinCode = remember { mutableStateOf(byteArrayOf()) }

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

    val webEidAuth = webEidViewModel?.authPayload?.collectAsState()?.value
    val originString = webEidAuth?.origin ?: ""
    val challengeString = webEidAuth?.challenge ?: ""

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
                                errorState.first, errorState.second, errorState.third,
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

    LaunchedEffect(nfcViewModel.webEidAuthResult) {
        nfcViewModel.webEidAuthResult.asFlow().collect { result ->
            result?.let { (authCert, signingCert, signature) ->
                webEidViewModel?.handleWebEidAuthResult(authCert, signingCert, signature, activity)
                nfcViewModel.resetWebEidAuthResult()
                onSuccess()
            }
        }
    }

    LaunchedEffect(nfcViewModel.dialogError) {
        pinCode.value.fill(0)
        nfcViewModel.dialogError.asFlow()
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
                                }
                                .testTag("smartIdErrorContainer"),
                    ) {
                        HrefMessageDialog(
                            modifier = modifier,
                            text1 = dialogError,
                            text1Arg = text1Arg,
                            text2 = text2,
                            linkText = linkText,
                            linkUrl = linkUrl,
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
                }
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signatureUpdateNFC"),
    ) {
        if (isAddingRoleAndAddress) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else if (isSigning || isWebEidAuthenticating || isAuthenticating || isDecrypting) {
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
                            }
                            .testTag("signatureUpdateNFCNotFoundMessage"),
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
                    isValidToWebEidAuthenticate(isValid)
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
                        authenticateWebEidAction {
                            saveFormParams()
                            scope.launch(IO) {
                                nfcViewModel.performNFCWebEidAuthWorkRequest(
                                    activity = activity,
                                    context = context,
                                    canNumber = canNumber.text,
                                    pin1Code = pinCode.value,
                                    origin = originString,
                                    challenge = challengeString,
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
                        cancelWebEidAuthenticateAction {
                            nfcViewModel.handleBackButton()
                            nfcViewModel.cancelWebEidAuthWorkRequest()
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
                            }
                            .testTag("nfcViewContainer"),
                ) {
                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(top = XSPadding),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            label = {
                                Text(text = canNumberLabel)
                            },
                            value =
                                if (!isTalkBackEnabled(context)) {
                                    canNumber
                                } else {
                                    canNumberWithInvisibleSpaces.copy(
                                        selection = TextRange(canNumberWithInvisibleSpaces.text.length),
                                    )
                                },
                            singleLine = true,
                            onValueChange = {
                                canNumberTextEdited.value = true

                                if (!isTalkBackEnabled(context)) {
                                    canNumber = it.copy(selection = TextRange(it.text.length))
                                } else {
                                    val noInvisibleElement =
                                        TextFieldValue(removeInvisibleElement(it.text))
                                    canNumber =
                                        noInvisibleElement.copy(
                                            selection =
                                                TextRange(
                                                    noInvisibleElement.text.length,
                                                ),
                                        )
                                }
                            },
                            modifier =
                                modifier
                                    .focusRequester(canNumberFocusRequester)
                                    .then(
                                        if (showPinField) {
                                            modifier.focusProperties {
                                                next = pinNumberFocusRequester
                                            }
                                        } else {
                                            modifier
                                        },
                                    )
                                    .weight(1f)
                                    .semantics(mergeDescendants = true) {
                                        testTagsAsResourceId = true
                                        contentDescription = canNumberLocationText
                                    }
                                    .testTag("signatureUpdateNFCCAN"),
                            trailingIcon = {
                                if (!isTalkBackEnabled(context) && canNumber.text.isNotEmpty()) {
                                    IconButton(onClick = {
                                        canNumber = TextFieldValue("")
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
                                if (showPinField) {
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Number,
                                    )
                                } else {
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Number,
                                    )
                                },
                            isError =
                                canNumberTextEdited.value &&
                                    nfcViewModel.shouldShowCANNumberError(canNumber.text),
                        )
                        if (isTalkBackEnabled(context) && canNumber.text.isNotEmpty()) {
                            IconButton(
                                modifier =
                                    modifier
                                        .align(Alignment.CenterVertically),
                                onClick = {
                                    canNumber = TextFieldValue("")
                                    scope.launch(Main) {
                                        canNumberFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        canNumberFocusRequester.requestFocus()
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
                                            .testTag("nfcCanNumberRemoveIconButton"),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                    contentDescription = "$clearButtonText $buttonName",
                                )
                            }
                        }
                    }
                    Text(
                        text = canNumberLocationText,
                        modifier =
                            modifier
                                .padding(vertical = XSPadding)
                                .testTag("signatureInputMethodTitle")
                                .notAccessible(),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (canNumberErrorText.isNotEmpty()) {
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusable(enabled = true)
                                    .semantics {
                                        contentDescription = canNumberErrorText
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("nfcCANErrorText"),
                            text = canNumberErrorText,
                            color = Red500,
                        )
                    }
                    val pinCodeTextEdited = rememberSaveable { mutableStateOf(false) }
                    val pinCodeErrorText =
                        if (pinCodeTextEdited.value && pinCode.value.isNotEmpty()) {
                            if (nfcViewModel
                                    .shouldShowPINCodeError(
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
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(top = XSPadding),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SecurePinTextField(
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("nfcPinTextField"),
                                pin = pinCode,
                                pinCodeLabel = pinCodeLabel,
                                pinNumberFocusRequester = pinNumberFocusRequester,
                                previousFocusRequester = canNumberFocusRequester,
                                pinCodeTextEdited = pinCodeTextEdited,
                                trailingIconContentDescription = "$clearButtonText $buttonName",
                                isError =
                                    pinCodeTextEdited.value &&
                                        nfcViewModel.shouldShowPINCodeError(
                                            pinCode.value,
                                            codeType,
                                        ),
                            )
                            if (isTalkBackEnabled(context) && pinCode.value.isNotEmpty()) {
                                IconButton(
                                    modifier =
                                        modifier
                                            .align(Alignment.CenterVertically)
                                            .semantics {
                                                traversalIndex = 9f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("nfcPinRemoveButton"),
                                    onClick = {
                                        pinCode.value = byteArrayOf()
                                        scope.launch(Main) {
                                            pinNumberFocusRequester.requestFocus()
                                            focusManager.clearFocus()
                                            delay(200)
                                            pinNumberFocusRequester.requestFocus()
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
                                                .testTag("nfcPinRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        if (pinCodeErrorText.isNotEmpty()) {
                            Text(
                                modifier =
                                    modifier
                                        .padding(vertical = XSPadding)
                                        .fillMaxWidth()
                                        .focusable(enabled = true)
                                        .semantics {
                                            contentDescription = pinCodeErrorText
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("nfcPinErrorText"),
                                text = pinCodeErrorText,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
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
