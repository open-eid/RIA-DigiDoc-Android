@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.toast.ToastUtil
import ee.ria.DigiDoc.ui.theme.Blue300
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.IdCardViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun IdCardView(
    activity: Activity,
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    cancelButtonClick: () -> Unit = {},
    dismissDialog: () -> Unit = {},
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    idCardViewModel: IdCardViewModel = hiltViewModel(),
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

    val isSigning = remember { mutableStateOf(false) }
    val showErrorDialog = remember { mutableStateOf(false) }

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)

    val pin2Text = stringResource(R.string.id_card_sign_pin2)
    var pin2Value by remember { mutableStateOf(TextFieldValue("")) }
    var isSigningEnabled by remember { mutableStateOf(false) }
    var roleDataRequest: RoleData? by remember { mutableStateOf(null) }
    val roleDataRequested by idCardViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    var errorText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        pin2Value = TextFieldValue("")
        idCardViewModel.resetPINErrorState()
        idCardViewModel.resetPersonalUserData()
    }

    LaunchedEffect(idCardStatus) {
        idCardStatus?.let { status ->
            when (status) {
                SmartCardReaderStatus.IDLE -> {
                    idCardStatusMessage.value = idCardStatusInitialMessage
                }

                SmartCardReaderStatus.READER_DETECTED -> {
                    idCardStatusMessage.value = idCardStatusReaderDetectedMessage
                }

                SmartCardReaderStatus.CARD_DETECTED -> {
                    withContext(IO) {
                        idCardViewModel.loadPersonalData(context)
                    }
                    idCardStatusMessage.value = idCardStatusCardDetectedMessage
                }
            }

            if (idCardStatus != SmartCardReaderStatus.CARD_DETECTED) {
                idCardViewModel.resetPersonalUserData()
                pin2Value = TextFieldValue("")
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
                pin2Value = TextFieldValue("")
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

                    pin2Value = TextFieldValue("")
                    dismissDialog()
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
                    isSigning.value = false
                    pin2Value = TextFieldValue("")
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
                    isSigning.value = false
                    pin2Value = TextFieldValue("")
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
                dismissDialog()
            }
    }
    if (errorText.isNotEmpty()) {
        ToastUtil.DigiDocToast(errorText)
    }

    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .imePadding()
                .padding(horizontal = screenViewLargePadding)
                .padding(bottom = screenViewExtraExtraLargePadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("idCardView"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else {
            SignatureAddRadioGroup(
                modifier = modifier,
                navController = signatureAddController,
                selectedRadioItem = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
                sharedSettingsViewModel = sharedSettingsViewModel,
            )

            if (isSigning.value) {
                idCardStatusMessage.value = idCardStatusSigningMessage
                isSigningEnabled = false
            }

            if (personalData == null || isSigning.value) {
                Text(
                    text = idCardStatusMessage.value,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .padding(vertical = screenViewLargePadding)
                            .testTag("idCardStatusMessage"),
                    textAlign = TextAlign.Center,
                )

                CircularProgressIndicator(
                    modifier =
                        modifier
                            .size(loadingBarSize)
                            .testTag("activityIndicator"),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (personalData != null && !isSigning.value) {
                Column(
                    modifier =
                        modifier
                            .padding(vertical = screenViewSmallPadding)
                            .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier =
                            modifier.padding(
                                horizontal = screenViewLargePadding,
                            )
                                .testTag("idCardSignMessage"),
                        text = stringResource(R.string.id_card_sign_message),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        modifier =
                            modifier.padding(
                                horizontal = screenViewLargePadding,
                            )
                                .testTag("idCardSignData")
                                .padding(vertical = screenViewLargePadding),
                        text =
                            stringResource(
                                R.string.id_card_sign_data,
                                personalData?.givenNames() ?: "",
                                personalData?.surname() ?: "",
                                personalData?.personalCode() ?: "",
                            ),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                    )

                    TextField(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .clearAndSetSemantics {
                                    testTagsAsResourceId = true
                                    testTag = "idCardPin2"
                                    contentDescription = pin2Text
                                }
                                .testTag("idCardPin2"),
                        shape = RectangleShape,
                        label = {
                            Text(
                                modifier = modifier.notAccessible(),
                                text = stringResource(id = R.string.id_card_sign_pin2),
                                color = if (!pinError.isNullOrEmpty()) Red500 else Blue300,
                            )
                        },
                        value = pin2Value,
                        onValueChange = {
                            pin2Value = it
                            val pin2ValueLength = pin2Value.text.length
                            isSigningEnabled = pin2ValueLength in 5..12
                        },
                        maxLines = 1,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleSmall,
                        isError = !pinError.isNullOrEmpty(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )

                    if (!pinError.isNullOrEmpty()) {
                        Text(
                            modifier =
                                modifier.padding(vertical = screenViewLargePadding)
                                    .fillMaxWidth()
                                    .focusable(enabled = true)
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

        CancelAndOkButtonRow(
            okButtonTestTag = "idCardViewSignButton",
            cancelButtonTestTag = "idCardViewCancelButton",
            cancelButtonClick = {
                cancelButtonClick()
                CoroutineScope(IO).launch {
                    signedContainer?.let { idCardViewModel.removePendingSignature(it) }
                }
            },
            okButtonClick = {
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

                    isSigning.value = true

                    CoroutineScope(IO).launch {
                        idCardViewModel.sign(
                            activity,
                            context,
                            signedContainer!!,
                            pin2Value.text.toByteArray(),
                            roleDataRequest,
                        )
                        pin2Value = TextFieldValue("")
                        idCardViewModel.resetRoleDataRequested()
                    }
                }
            },
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            okButtonEnabled = isSigningEnabled,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
        )
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
                onDismissRequest = { dismissDialog() },
            ) {
                Surface(
                    modifier =
                        modifier
                            .wrapContentHeight()
                            .wrapContentWidth()
                            .padding(screenViewLargePadding)
                            .verticalScroll(rememberScrollState()),
                ) {
                    HrefMessageDialog(
                        modifier = modifier.align(Alignment.Center),
                        text1 = text1,
                        text1Arg = text1Arg,
                        text2 = text2,
                        linkText = linkText,
                        linkUrl = linkUrl,
                        cancelButtonClick = {},
                        okButtonClick = {
                            idCardViewModel.resetDialogErrorState()
                            dismissDialog()
                        },
                        showCancelButton = false,
                    )
                }
            }
            InvisibleElement(modifier = modifier)
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
        IdCardView(
            activity = LocalContext.current as Activity,
            signatureAddController = rememberNavController(),
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
