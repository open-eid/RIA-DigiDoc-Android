@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN1_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.SecurePinTextField
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.IdCardViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IdCardView(
    activity: Activity,
    modifier: Modifier = Modifier,
    identityAction: IdentityAction,
    isSigning: Boolean = false,
    isDecrypting: Boolean = false,
    isAuthenticating: Boolean = false,
    isStarted: (Boolean) -> Unit = {},
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    isAddingRoleAndAddress: Boolean = false,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    idCardViewModel: IdCardViewModel = hiltViewModel(),
    isValidToSign: (Boolean) -> Unit = {},
    isAuthenticated: (Boolean, IdCardData) -> Unit,
    isValidToDecrypt: (Boolean) -> Unit = {},
    signAction: (() -> Unit) -> Unit = {},
    decryptAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
    cancelDecryptAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val loading by remember { mutableStateOf(true) }

    val idCardStatus by idCardViewModel.idCardStatus.asFlow().collectAsState(SmartCardReaderStatus.IDLE)
    val idCardData by idCardViewModel.userData.asFlow().collectAsState(null)
    val dialogError by idCardViewModel.dialogError.asFlow().collectAsState(null)

    val idCardStatusInitialMessage = stringResource(id = R.string.id_card_status_initial_message)
    val idCardStatusMessage = remember { mutableStateOf(idCardStatusInitialMessage) }
    val idCardStatusReaderDetectedMessage = stringResource(id = R.string.id_card_status_reader_detected_message)
    val idCardStatusCardDetectedMessage = stringResource(id = R.string.id_card_status_card_detected_message)
    val idCardStatusSigningMessage =
        if (identityAction == IdentityAction.SIGN) {
            stringResource(id = R.string.id_card_progress_message_signing)
        } else if (identityAction == IdentityAction.DECRYPT) {
            stringResource(id = R.string.id_card_progress_message_decrypt)
        } else {
            ""
        }
    val idCardStatusReadyToSignMessage =
        if (identityAction == IdentityAction.SIGN) {
            stringResource(R.string.id_card_sign_message)
        } else if (identityAction == IdentityAction.DECRYPT) {
            stringResource(R.string.id_card_decrypt_message)
        } else {
            ""
        }

    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    var isDataLoadingStarted by rememberSaveable { mutableStateOf(false) }
    var showLoadingIndicator by rememberSaveable { mutableStateOf(false) }

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val cryptoContainer by sharedContainerViewModel.cryptoContainer.asFlow().collectAsState(null)

    val pinType =
        if (identityAction == IdentityAction.SIGN) {
            stringResource(id = R.string.signature_id_card_pin2)
        } else {
            stringResource(id = R.string.signature_id_card_pin1)
        }

    val pinMinLength =
        if (identityAction == IdentityAction.SIGN) {
            PIN2_MIN_LENGTH
        } else {
            PIN1_MIN_LENGTH
        }

    val pinText = stringResource(R.string.id_card_identity_pin, pinType)
    var pinCode = remember { mutableStateOf(byteArrayOf()) }

    var roleDataRequest: RoleData? by remember { mutableStateOf(null) }
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    var errorText by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val statusMessageFocusRequester = remember { FocusRequester() }
    val readyToSignFocusRequester = remember { FocusRequester() }
    val pinCodeFocusRequester = remember { FocusRequester() }

    var isValid by rememberSaveable { mutableStateOf(false) }

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    BackHandler {
        if (isSigning || isDecrypting || isAuthenticating) {
            isDataLoadingStarted = false
            showLoadingIndicator = false
            onError()
        } else {
            onSuccess()
        }
    }

    LaunchedEffect(Unit) {
        pinCode.value = byteArrayOf()
        idCardViewModel.resetPINErrorState()
        idCardViewModel.resetPersonalUserData()
    }

    LaunchedEffect(idCardStatus) {
        idCardStatus?.let { status ->
            when (status) {
                SmartCardReaderStatus.IDLE -> {
                    showLoadingIndicator = false
                    isDataLoadingStarted = false
                    idCardStatusMessage.value = idCardStatusInitialMessage
                }

                SmartCardReaderStatus.READER_DETECTED -> {
                    showLoadingIndicator = false
                    isDataLoadingStarted = false
                    idCardStatusMessage.value = idCardStatusReaderDetectedMessage
                }

                SmartCardReaderStatus.CARD_DETECTED -> {
                    showLoadingIndicator = true
                    isDataLoadingStarted = true
                    withContext(IO) {
                        idCardViewModel.loadPersonalData()
                    }
                    idCardStatusMessage.value = idCardStatusCardDetectedMessage
                }
            }

            if (idCardStatus != SmartCardReaderStatus.CARD_DETECTED) {
                idCardViewModel.resetPersonalUserData()
                pinCode.value.fill(0)
            }
        } ?: run {
            idCardStatusMessage.value = idCardStatusInitialMessage
        }
    }

    LaunchedEffect(idCardViewModel.signStatus) {
        idCardViewModel.signStatus.asFlow()
            .filterNotNull()
            .collect { signStatus ->
                sharedContainerViewModel.setSignedIDCardStatus(signStatus)
                idCardViewModel.resetSignStatus()
                pinCode.value.fill(0)
                idCardViewModel.resetPINErrorState()
                pinCode.value = byteArrayOf()
            }
    }

    LaunchedEffect(idCardViewModel.decryptStatus) {
        idCardViewModel.decryptStatus.asFlow()
            .filterNotNull()
            .collect { decryptStatus ->
                sharedContainerViewModel.setDecryptIDCardStatus(decryptStatus)
                idCardViewModel.resetDecryptStatus()
                pinCode.value.fill(0)
                idCardViewModel.resetPINErrorState()
                pinCode.value = byteArrayOf()
            }
    }

    LaunchedEffect(idCardViewModel.errorState) {
        idCardViewModel.errorState.asFlow()
            .filterNotNull()
            .collect { errorState ->
                withContext(IO) {
                    idCardViewModel.resetPersonalUserData()
                    idCardViewModel.resetErrorState()
                }
                withContext(Main) {
                    if (errorState.first != 0) {
                        errorText =
                            context.getString(
                                errorState.first, errorState.second, errorState.third,
                            )
                    }

                    pinCode.value = byteArrayOf()

                    isDataLoadingStarted = false
                    showLoadingIndicator = false
                    idCardStatusMessage.value = idCardStatusInitialMessage
                    isValidToSign(false)
                    isValidToDecrypt(false)
                    onError()
                }
            }
    }

    LaunchedEffect(idCardViewModel.pinErrorState) {
        idCardViewModel.pinErrorState.asFlow()
            .filterNotNull()
            .collect { pinErrorState ->
                withContext(IO) {
                    idCardViewModel.resetErrorState()
                    idCardViewModel.resetDialogErrorState()
                }
                withContext(Main) {
                    pinCode.value = byteArrayOf()
                    pinErrorText =
                        context.getString(
                            pinErrorState.first, pinErrorState.second, pinErrorState.third,
                        )
                    onError()
                }
            }
    }

    LaunchedEffect(idCardViewModel.dialogError) {
        idCardViewModel.dialogError.asFlow()
            .filterNotNull()
            .collect {
                withContext(Main) {
                    pinCode.value.fill(0)
                    idCardViewModel.resetErrorState()
                    idCardViewModel.resetPINErrorState()
                    pinCode.value = byteArrayOf()
                    showErrorDialog.value = true
                }
            }
    }

    LaunchedEffect(idCardViewModel.signedContainer) {
        idCardViewModel.signedContainer.asFlow()
            .filterNotNull()
            .collect { signedContainer ->
                sharedContainerViewModel.setSignedContainer(signedContainer)
                idCardViewModel.resetSignedContainer()
                onSuccess()
            }
    }

    LaunchedEffect(idCardViewModel.cryptoContainer) {
        idCardViewModel.cryptoContainer.asFlow()
            .filterNotNull()
            .collect { cryptoContainer ->
                sharedContainerViewModel.setCryptoContainer(cryptoContainer, true)
                idCardViewModel.resetCryptoContainer()
                onSuccess()
            }
    }

    LaunchedEffect(Unit, idCardData, isValid, isSigning, isDecrypting, isAuthenticating, idCardStatusMessage) {
        if (idCardData?.personalData == null ||
            (isValid && isSigning) ||
            (isValid && isDecrypting) ||
            isAuthenticating
        ) {
            statusMessageFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit, idCardData, isSigning, isAuthenticating, isDecrypting) {
        if (idCardData?.personalData != null && !isSigning && !isAuthenticating && !isDecrypting) {
            delay(500)
            readyToSignFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit, idCardData, isAuthenticating) {
        if (idCardData?.personalData != null && isAuthenticating && !isSigning) {
            idCardData?.let { data ->
                isAuthenticated(true, data)
                idCardViewModel.resetPersonalUserData()
            }
        }
    }

    if (errorText.isNotEmpty()) {
        showMessage(errorText)
        errorText = ""
    }

    if (showErrorDialog.value) {
        var text1 = 0
        var text1Arg: Int? = null
        val text2 = null
        var linkText = 0
        var linkUrl = 0
        if (dialogError?.contains("Too Many Requests") == true) {
            text1 = R.string.too_many_requests_message
            text1Arg = R.string.id_card_conditional_speech
            linkText = R.string.additional_information
            linkUrl = R.string.too_many_requests_url
        } else if (dialogError?.contains("OCSP response not in valid time slot") == true) {
            text1 = R.string.invalid_time_slot_message
            linkText = R.string.additional_information
            linkUrl = R.string.invalid_time_slot_url
        }
        Box(modifier = modifier.fillMaxSize()) {
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface),
                onDismissRequest = {
                    showErrorDialog.value = false
                    idCardViewModel.resetDialogErrorState()
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
                                .testTag("idCardErrorContainer"),
                    ) {
                        HrefMessageDialog(
                            modifier = modifier,
                            text1 = text1,
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
                                idCardViewModel.resetDialogErrorState()
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
                .testTag("signatureUpdateIdCard"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isAddingRoleAndAddress) {
            RoleDataView(modifier, sharedSettingsViewModel, onError)
        } else {
            isValid = pinCode.value.size in pinMinLength..PIN_MAX_LENGTH

            LaunchedEffect(isValid) {
                isValidToSign(isValid)
            }

            LaunchedEffect(isValid) {
                isValidToDecrypt(isValid)
            }

            LaunchedEffect(Unit, isValid) {
                if (isValid) {
                    signAction {
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
                            idCardViewModel.sign(
                                activity,
                                signedContainer!!,
                                pinCode.value,
                                roleDataRequest,
                            )
                            pinCode.value.fill(0)
                        }
                    }
                    decryptAction {
                        scope.launch(IO) {
                            idCardViewModel.decrypt(
                                activity,
                                context,
                                cryptoContainer!!,
                                pinCode.value,
                            )
                            pinCode.value.fill(0)
                        }
                    }
                }
                cancelAction {
                    scope.launch(IO) {
                        signedContainer?.let { idCardViewModel.removePendingSignature(it) }
                    }
                }
                cancelDecryptAction {
                    scope.launch(IO) {
                        signedContainer?.let { idCardViewModel.removePendingSignature(it) }
                    }
                }
            }

            LaunchedEffect(isDataLoadingStarted) {
                if (isDataLoadingStarted) {
                    isStarted(true)
                }
            }

            if (idCardData?.personalData != null && isSigning) {
                idCardStatusMessage.value = idCardStatusSigningMessage
            }

            if (idCardData?.personalData != null && isDecrypting) {
                idCardStatusMessage.value = idCardStatusSigningMessage
            }

            if (idCardData?.personalData == null ||
                (isValid && isSigning) ||
                (isValid && isDecrypting) ||
                isAuthenticating
            ) {
                if (!showLoadingIndicator) {
                    Icon(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .size(iconSizeXXL)
                                .notAccessible(),
                        imageVector = ImageVector.vectorResource(R.drawable.ic_m3_smart_card_reader_48dp_wght400),
                        contentDescription = null,
                    )
                } else {
                    CircularProgressIndicator(
                        modifier =
                            modifier
                                .padding(vertical = LPadding)
                                .size(loadingBarSize)
                                .testTag("activityIndicator")
                                .notAccessible(),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }

                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = SPadding),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = idCardStatusMessage.value,
                        style = MaterialTheme.typography.titleLarge,
                        modifier =
                            modifier
                                .focusRequester(statusMessageFocusRequester)
                                .focusable()
                                .testTag("idCardStatusMessage"),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            if (idCardData?.personalData != null && !isSigning && !isAuthenticating && !isDecrypting) {
                Column(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(top = SPadding)
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("signatureUpdateIdCardContainer"),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(SPadding),
                ) {
                    Text(
                        modifier =
                            modifier
                                .zIndex(1f)
                                .focusRequester(readyToSignFocusRequester)
                                .focusable()
                                .semantics {
                                    traversalIndex = 1f
                                    testTagsAsResourceId = true
                                }
                                .testTag("idCardReadyToSignMessage"),
                        text = idCardStatusReadyToSignMessage,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val personalData = idCardData?.personalData

                    val nameText =
                        formatName(
                            "${personalData?.surname()},${personalData?.givenNames()},${personalData?.personalCode()}",
                        )

                    StyledNameText(
                        modifier =
                            modifier
                                .zIndex(2f)
                                .focusable(false)
                                .semantics {
                                    traversalIndex = 2f
                                    testTagsAsResourceId = true
                                    contentDescription = formatNumbers(nameText)
                                }
                                .testTag("idCardSignerNameText"),
                        nameText,
                    )

                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("idCardContainer"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(LPadding),
                    ) {
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(top = MSPadding),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SecurePinTextField(
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .zIndex(3f)
                                        .semantics {
                                            traversalIndex = 3f
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("idCardPinTextField"),
                                pin = pinCode,
                                pinCodeLabel = pinText,
                                pinNumberFocusRequester = pinCodeFocusRequester,
                                previousFocusRequester = readyToSignFocusRequester,
                                pinCodeTextEdited = null,
                                trailingIconContentDescription = "$clearButtonText $buttonName",
                                isError = pinErrorText.isNotEmpty(),
                            )
                            if (isTalkBackEnabled(context) && pinCode.value.isNotEmpty()) {
                                IconButton(
                                    modifier =
                                        modifier
                                            .zIndex(4f)
                                            .align(Alignment.CenterVertically)
                                            .semantics {
                                                traversalIndex = 4f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("idCardPinRemoveButton"),
                                    onClick = {
                                        pinCode.value = byteArrayOf()
                                        scope.launch(Main) {
                                            pinCodeFocusRequester.requestFocus()
                                            focusManager.clearFocus()
                                            delay(200)
                                            pinCodeFocusRequester.requestFocus()
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
                                                .testTag("idCardPinRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        if (pinErrorText.isNotEmpty()) {
                            Text(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .focusable(true)
                                        .semantics { contentDescription = pinErrorText }
                                        .testTag("idCardPinError"),
                                text = pinErrorText,
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    InvisibleElement(modifier = modifier)

    if (!loading) return
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun IdCardViewPreview() {
    RIADigiDocTheme {
        val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
        val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
        IdCardView(
            activity = LocalActivity.current as Activity,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            identityAction = IdentityAction.SIGN,
            isAuthenticating = false,
            isAuthenticated = { _, _ -> {} },
        )
    }
}
