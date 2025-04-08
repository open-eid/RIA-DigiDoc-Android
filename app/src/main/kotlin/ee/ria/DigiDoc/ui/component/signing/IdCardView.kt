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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.removeInvisibleElement
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.viewmodel.IdCardViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
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
    isSigning: Boolean,
    isStarted: (Boolean) -> Unit,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    idCardViewModel: IdCardViewModel = hiltViewModel(),
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current

    val loading by remember { mutableStateOf(true) }

    val idCardStatus by idCardViewModel.idCardStatus.asFlow().collectAsState(SmartCardReaderStatus.IDLE)
    val personalData by idCardViewModel.userData.asFlow().collectAsState(null)
    val pinError by idCardViewModel.pinErrorState.asFlow().collectAsState(null)
    val dialogError by idCardViewModel.dialogError.asFlow().collectAsState(null)

    val idCardStatusInitialMessage = stringResource(id = R.string.id_card_status_initial_message)
    val idCardStatusMessage = remember { mutableStateOf(idCardStatusInitialMessage) }
    val idCardStatusReaderDetectedMessage = stringResource(id = R.string.id_card_status_reader_detected_message)
    val idCardStatusCardDetectedMessage = stringResource(id = R.string.id_card_status_card_detected_message)
    val idCardStatusSigningMessage = stringResource(id = R.string.id_card_progress_message_signing)
    val idCardStatusReadyToSignMessage = stringResource(R.string.id_card_sign_message)

    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    var isDataLoadingStarted by rememberSaveable { mutableStateOf(false) }
    var showLoadingIndicator by rememberSaveable { mutableStateOf(false) }

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val pin2Text = stringResource(R.string.id_card_sign_pin2)
    var pin2Code by remember { mutableStateOf(TextFieldValue("")) }

    var roleDataRequest: RoleData? by remember { mutableStateOf(null) }
    val roleDataRequested by idCardViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    var errorText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val statusMessageFocusRequester = remember { FocusRequester() }
    val readyToSignFocusRequester = remember { FocusRequester() }

    var isValid by rememberSaveable { mutableStateOf(false) }
    var pin2WithInvisibleSpaces = TextFieldValue(addInvisibleElement(pin2Code.text))

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    BackHandler {
        if (isSigning) {
            isDataLoadingStarted = false
            showLoadingIndicator = false
            onError()
        } else {
            onSuccess()
        }
    }

    LaunchedEffect(Unit) {
        pin2Code = TextFieldValue("")
        pin2WithInvisibleSpaces =
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            )
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
                        idCardViewModel.loadPersonalData(context)
                    }
                    idCardStatusMessage.value = idCardStatusCardDetectedMessage
                }
            }

            if (idCardStatus != SmartCardReaderStatus.CARD_DETECTED) {
                idCardViewModel.resetPersonalUserData()
                pin2Code = TextFieldValue("")
                pin2WithInvisibleSpaces =
                    TextFieldValue(
                        text = "",
                        selection = TextRange.Zero,
                    )
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
                pin2Code = TextFieldValue("")
                pin2WithInvisibleSpaces =
                    TextFieldValue(
                        text = "",
                        selection = TextRange.Zero,
                    )
            }
    }

    LaunchedEffect(idCardViewModel.errorState) {
        idCardViewModel.errorState.asFlow()
            .filterNotNull()
            .collect { errorState ->
                withContext(IO) {
                    idCardViewModel.resetPersonalUserData()
                    idCardViewModel.resetRoleDataRequested()
                    idCardViewModel.resetErrorState()
                }
                withContext(Main) {
                    if (errorState != "") {
                        errorText = errorState
                    }

                    pin2Code = TextFieldValue("")
                    pin2WithInvisibleSpaces =
                        TextFieldValue(
                            text = "",
                            selection = TextRange.Zero,
                        )
                }
            }
    }

    LaunchedEffect(idCardViewModel.pinErrorState) {
        idCardViewModel.pinErrorState.asFlow()
            .filterNotNull()
            .collect {
                withContext(Main) {
                    idCardViewModel.resetErrorState()
                    idCardViewModel.resetDialogErrorState()
                    pin2Code = TextFieldValue("")
                    pin2WithInvisibleSpaces =
                        TextFieldValue(
                            text = "",
                            selection = TextRange.Zero,
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
                    idCardViewModel.resetErrorState()
                    idCardViewModel.resetPINErrorState()
                    pin2Code = TextFieldValue("")
                    pin2WithInvisibleSpaces =
                        TextFieldValue(
                            text = "",
                            selection = TextRange.Zero,
                        )
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
                idCardViewModel.resetRoleDataRequested()
                onSuccess()
            }
    }

    LaunchedEffect(Unit, personalData, isValid, isSigning, idCardStatusMessage) {
        if (personalData == null || (isValid && isSigning)) {
            statusMessageFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(Unit, personalData, isSigning) {
        if (personalData != null && !isSigning) {
            delay(500)
            readyToSignFocusRequester.requestFocus()
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
                            .wrapContentWidth(),
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
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else {
            isValid = pin2Code.text.length in 5..12

            LaunchedEffect(isValid) {
                isValidToSign(isValid)
            }

            LaunchedEffect(Unit, isValid) {
                if (isValid) {
                    signAction {
                        if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                            idCardViewModel.setRoleDataRequested(true)
                        } else {
                            if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
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
                                idCardViewModel.sign(
                                    activity,
                                    context,
                                    signedContainer!!,
                                    pin2Code.text.toByteArray(),
                                    roleDataRequest,
                                )
                                pin2Code = TextFieldValue("")
                                pin2WithInvisibleSpaces =
                                    TextFieldValue(
                                        text = "",
                                        selection = TextRange.Zero,
                                    )
                                idCardViewModel.resetRoleDataRequested()
                            }
                        }
                    }
                    cancelAction {
                        CoroutineScope(IO).launch {
                            signedContainer?.let { idCardViewModel.removePendingSignature(it) }
                        }
                    }
                }
            }

            LaunchedEffect(isDataLoadingStarted) {
                if (isDataLoadingStarted) {
                    isStarted(true)
                }
            }

            if (personalData != null && isSigning) {
                idCardStatusMessage.value = idCardStatusSigningMessage
            }

            if (personalData == null || (isValid && isSigning)) {
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

                Text(
                    text = idCardStatusMessage.value,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .focusRequester(statusMessageFocusRequester)
                            .focusable()
                            .padding(vertical = SPadding)
                            .testTag("idCardStatusMessage"),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (personalData != null && !isSigning) {
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
                                .focusRequester(readyToSignFocusRequester)
                                .focusable()
                                .testTag("idCardReadyToSignMessage"),
                        text = idCardStatusReadyToSignMessage,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    val nameText =
                        formatName(
                            "${personalData?.surname()},${personalData?.givenNames()},${personalData?.personalCode()}",
                        )

                    StyledNameText(
                        modifier =
                            modifier
                                .focusable(false)
                                .semantics {
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
                                .testTag("signatureUpdateIdCardContainer"),
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
                            OutlinedTextField(
                                label = {
                                    Text(
                                        modifier = modifier.notAccessible(),
                                        text = pin2Text,
                                        color =
                                            if (!pinError.isNullOrEmpty()) {
                                                Red500
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                    )
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
                                onValueChange = {
                                    if (!isTalkBackEnabled(context)) {
                                        pin2Code =
                                            it.copy(selection = TextRange(it.text.length))
                                    } else {
                                        val noInvisibleElement =
                                            TextFieldValue(removeInvisibleElement(it.text))
                                        pin2Code =
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
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            testTagsAsResourceId = true
                                            contentDescription = pin2Text
                                        }
                                        .testTag("idCardPin2TextField"),
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
                                                .testTag("idCardPin2PasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                isError = !pinError.isNullOrEmpty(),
                                visualTransformation =
                                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                                                .testTag("idCardPin2CodeRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        if (!pinError.isNullOrEmpty()) {
                            Text(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .focusable(true)
                                        .semantics { contentDescription = pinError ?: "" }
                                        .testTag("idCardPin2Error"),
                                text = pinError ?: "",
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
            isStarted = {},
            isSigning = true,
            isValidToSign = {},
        )
    }
}
