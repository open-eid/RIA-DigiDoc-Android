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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.CAN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager.NfcStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
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
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NFCView(
    activity: Activity,
    modifier: Modifier = Modifier,
    isSigning: Boolean,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    isAddingRoleAndAddress: Boolean,
    rememberMe: Boolean,
    nfcViewModel: NFCViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    isSupported: (Boolean) -> Unit,
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    var nfcStatus by remember { mutableStateOf(nfcViewModel.getNFCStatus(activity)) }
    var nfcImage by remember { mutableIntStateOf(R.drawable.ic_icon_nfc) }

    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val dialogError by nfcViewModel.dialogError.asFlow().collectAsState(0)

    val canNumberLabel = stringResource(id = R.string.signature_update_nfc_can)
    val canNumberLocationText = stringResource(R.string.nfc_sign_can_location)
    val pin2CodeLabel = stringResource(id = R.string.signature_update_nfc_pin2)

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    var canNumber by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getCanNumber(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getCanNumber().length),
            ),
        )
    }
    var pin2Code by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
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

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val canNumberFocusRequester = remember { FocusRequester() }
    val pin2NumberFocusRequester = remember { FocusRequester() }
    val canNumberWithInvisibleSpaces = TextFieldValue(addInvisibleElement(canNumber.text))
    var pin2WithInvisibleSpaces = TextFieldValue(addInvisibleElement(pin2Code.text))

    BackHandler {
        nfcViewModel.handleBackButton()
        onError()
    }

    LaunchedEffect(nfcViewModel.shouldResetPIN2) {
        nfcViewModel.shouldResetPIN2.asFlow().collect { bool ->
            bool?.let {
                if (bool) {
                    pin2Code =
                        TextFieldValue(
                            text = "",
                            selection = TextRange.Zero,
                        )
                    pin2WithInvisibleSpaces =
                        TextFieldValue(
                            text = "",
                            selection = TextRange.Zero,
                        )
                    nfcViewModel.resetShouldResetPIN2()
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

    LaunchedEffect(nfcViewModel.errorState) {
        nfcViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    if (errorState != "") {
                        errorText = errorState
                    }

                    nfcViewModel.resetErrorState()
                }
            }
        }
    }

    LaunchedEffect(nfcViewModel.signedContainer) {
        nfcViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                nfcViewModel.resetSignedContainer()
                onSuccess()
            }
        }
    }

    LaunchedEffect(nfcViewModel.dialogError) {
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
        nfcViewModel.checkNFCStatus(nfcViewModel.getNFCStatus(activity))
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
        } else if (isSigning) {
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
                            .semantics { heading() }
                            .testTag("signatureUpdateNFCNotFoundMessage"),
                    textAlign = TextAlign.Center,
                )
            } else {
                nfcImage = R.drawable.ic_icon_nfc

                val isValid =
                    nfcViewModel.positiveButtonEnabled(
                        canNumber.text,
                        pin2Code.text.toByteArray(StandardCharsets.UTF_8),
                    )

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
                            CoroutineScope(IO).launch {
                                nfcViewModel.performNFCWorkRequest(
                                    activity = activity,
                                    context = context,
                                    container = signedContainer,
                                    pin2Code = pin2Code.text.toByteArray(StandardCharsets.UTF_8),
                                    canNumber = canNumber.text,
                                    roleData = roleDataRequest,
                                )
                            }
                        }
                        cancelAction {
                            nfcViewModel.handleBackButton()
                            CoroutineScope(IO).launch {
                                signedContainer?.let { nfcViewModel.cancelNFCWorkRequest(it) }
                            }
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
                                    val noInvisibleElement = TextFieldValue(removeInvisibleElement(it.text))
                                    canNumber =
                                        noInvisibleElement.copy(selection = TextRange(noInvisibleElement.text.length))
                                }
                            },
                            modifier =
                                modifier
                                    .focusRequester(canNumberFocusRequester)
                                    .focusProperties {
                                        next = pin2NumberFocusRequester
                                    }
                                    .weight(1f)
                                    .semantics(mergeDescendants = true) {
                                        testTagsAsResourceId = true
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
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Number,
                                ),
                            isError =
                                canNumberTextEdited.value &&
                                    nfcViewModel.shouldShowCANNumberError(canNumber.text),
                        )
                        if (isTalkBackEnabled(context) && canNumber.text.isNotEmpty()) {
                            IconButton(
                                modifier =
                                    modifier
                                        .align(Alignment.CenterVertically),
                                onClick = { canNumber = TextFieldValue("") },
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
                                .testTag("signatureInputMethodTitle"),
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
                                    .semantics { contentDescription = canNumberErrorText }
                                    .testTag("nfcCANErrorText"),
                            text = canNumberErrorText,
                            color = Red500,
                        )
                    }

                    val pin2CodeTextEdited = rememberSaveable { mutableStateOf(false) }
                    val pin2CodeErrorText =
                        if (pin2CodeTextEdited.value && pin2Code.text.isNotEmpty()) {
                            if (nfcViewModel
                                    .shouldShowPIN2CodeError(
                                        pin2Code.text.toByteArray(StandardCharsets.UTF_8),
                                    )
                            ) {
                                String.format(
                                    stringResource(id = R.string.id_card_sign_pin_invalid_length),
                                    stringResource(id = R.string.signature_id_card_pin2),
                                    PIN2_MIN_LENGTH.toString(),
                                    PIN_MAX_LENGTH.toString(),
                                )
                            } else {
                                ""
                            }
                        } else {
                            ""
                        }

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
                                Text(text = pin2CodeLabel)
                            },
                            value =
                                when {
                                    !isTalkBackEnabled(context) -> pin2Code
                                    passwordVisible ->
                                        pin2WithInvisibleSpaces.copy(
                                            selection = TextRange(pin2WithInvisibleSpaces.text.length),
                                        )
                                    else -> pin2Code
                                },
                            singleLine = true,
                            modifier =
                                modifier
                                    .focusRequester(pin2NumberFocusRequester)
                                    .focusProperties {
                                        previous = canNumberFocusRequester
                                    }
                                    .weight(1f)
                                    .semantics(mergeDescendants = true) {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("nfcPin2TextField"),
                            onValueChange = {
                                pin2Code = it.copy(selection = TextRange(it.text.length))
                                pin2CodeTextEdited.value = true
                            },
                            trailingIcon = {
                                val image =
                                    if (passwordVisible) {
                                        ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                    } else {
                                        ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                    }
                                val description =
                                    if (passwordVisible) {
                                        stringResource(
                                            id = R.string.hide_password,
                                        )
                                    } else {
                                        stringResource(id = R.string.show_password)
                                    }
                                IconButton(
                                    modifier =
                                        modifier
                                            .semantics { traversalIndex = 9f }
                                            .testTag("nfcPin2PasswordVisibleButton"),
                                    onClick = { passwordVisible = !passwordVisible },
                                ) {
                                    Icon(imageVector = image, description)
                                }
                            },
                            visualTransformation =
                                if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError =
                                pin2CodeTextEdited.value &&
                                    nfcViewModel
                                        .shouldShowPIN2CodeError(
                                            pin2Code.text.toByteArray(
                                                StandardCharsets.UTF_8,
                                            ),
                                        ),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.NumberPassword,
                                ),
                        )
                        if (isTalkBackEnabled(context) && pin2Code.text.isNotEmpty()) {
                            IconButton(
                                modifier =
                                    modifier
                                        .align(Alignment.CenterVertically),
                                onClick = { pin2Code = TextFieldValue("") },
                            ) {
                                Icon(
                                    modifier =
                                        modifier
                                            .size(iconSizeXXS)
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("nfcPin2RemoveIconButton"),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                    contentDescription = "$clearButtonText $buttonName",
                                )
                            }
                        }
                    }

                    if (pin2CodeErrorText.isNotEmpty()) {
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusable(enabled = true)
                                    .semantics { contentDescription = pin2CodeErrorText }
                                    .testTag("nfcPin2ErrorText"),
                            text = pin2CodeErrorText,
                            color = Red500,
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
            isSigning = false,
            isAddingRoleAndAddress = false,
            rememberMe = true,
            isSupported = {},
            isValidToSign = {},
        )
    }
}
