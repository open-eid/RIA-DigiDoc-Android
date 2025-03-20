@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
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
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.NFCViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NFCView(
    activity: Activity,
    modifier: Modifier = Modifier,
    dismissDialog: () -> Unit = {},
    rememberMe: Boolean,
    nfcViewModel: NFCViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    isSupported: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    var nfcStatus by remember { mutableStateOf(nfcViewModel.getNFCStatus(activity)) }
    var nfcImage by remember { mutableIntStateOf(R.drawable.ic_icon_nfc) }

    val roleDataRequested by nfcViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val canNumberLabel = stringResource(id = R.string.signature_update_nfc_can)
    val canNumberLocationText = stringResource(R.string.nfc_sign_can_location)
    val pin2CodeLabel = stringResource(id = R.string.signature_update_nfc_pin2)

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    var canNumberText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getCanNumber(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getCanNumber().length),
            ),
        )
    }
    var pin2CodeText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            ),
        )
    }
    var errorText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
    }
    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setCanNumber(canNumberText.text)
        } else {
            sharedSettingsViewModel.dataStore.setCanNumber("")
        }
    }

    LaunchedEffect(nfcViewModel.shouldResetPIN2) {
        nfcViewModel.shouldResetPIN2.asFlow().collect { bool ->
            bool?.let {
                if (bool) {
                    pin2CodeText =
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
                nfcViewModel.resetRoleDataRequested()
                dismissDialog()
            }
        }
    }

    LaunchedEffect(Unit, rememberMe) {
        shouldRememberMe = rememberMe
    }

    if (errorText.isNotEmpty()) {
        showMessage(errorText)
        errorText = ""
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
                NFCSignatureUpdateContainer(
                    modifier = modifier,
                    nfcImage = nfcImage,
                    nfcViewModel = nfcViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        CoroutineScope(IO).launch {
                            signedContainer?.let { nfcViewModel.cancelNFCWorkRequest(it) }
                        }
                    },
                )
                InvisibleElement(modifier = modifier)
            }
        }
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
                .onGloballyPositioned {
                    CoroutineScope(Main).launch {
                        nfcViewModel.checkNFCStatus(nfcViewModel.getNFCStatus(activity))
                    }
                }
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signatureUpdateNFC"),
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
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
                        canNumberText.text,
                        pin2CodeText.text.toByteArray(StandardCharsets.UTF_8),
                    )

                LaunchedEffect(isValid) {
                    isValidToSign(isValid)
                }

                LaunchedEffect(Unit, isValid) {
                    if (isValid) {
                        signAction {
                            if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                                nfcViewModel.setRoleDataRequested(true)
                            } else {
                                openSignatureUpdateContainerDialog.value = true
                                saveFormParams()
                                var roleDataRequest: RoleData? = null
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
                                    nfcViewModel.performNFCWorkRequest(
                                        activity = activity,
                                        context = context,
                                        container = signedContainer,
                                        pin2Code = pin2CodeText.text.toByteArray(StandardCharsets.UTF_8),
                                        canNumber = canNumberText.text,
                                        roleData = roleDataRequest,
                                    )
                                    nfcViewModel.resetRoleDataRequested()
                                }
                            }
                        }
                    }
                }

                val canNumberTextEdited = rememberSaveable { mutableStateOf(false) }
                val canNumberErrorText =
                    if (canNumberTextEdited.value && canNumberText.text.isNotEmpty()) {
                        if (nfcViewModel.shouldShowCANNumberError(canNumberText.text)) {
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
                            .testTag("mobileIdViewContainer"),
                ) {
                    OutlinedTextField(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .clearAndSetSemantics {
                                    testTagsAsResourceId = true
                                    testTag = "signatureUpdateNFCCAN"
                                    contentDescription =
                                        "$canNumberLabel " +
                                        "${formatNumbers(canNumberText.text)}. $canNumberLocationText"
                                }
                                .testTag("signatureUpdateNFCCAN"),
                        value = canNumberText,
                        shape = RectangleShape,
                        onValueChange = {
                            canNumberText = it
                            canNumberTextEdited.value = true
                        },
                        label = {
                            Text(
                                modifier = modifier.notAccessible(),
                                text = canNumberLabel,
                            )
                        },
                        singleLine = true,
                        isError =
                            canNumberTextEdited.value &&
                                nfcViewModel.shouldShowCANNumberError(canNumberText.text),
                        textStyle = MaterialTheme.typography.titleLarge,
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Decimal,
                            ),
                    )
                    Text(
                        text = canNumberLocationText,
                        modifier =
                            modifier
                                .padding(vertical = XSPadding)
                                .focusable(false)
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
                                    .semantics { contentDescription = canNumberErrorText }
                                    .testTag("signatureUpdateNFCCANErrorText"),
                            text = canNumberErrorText,
                            color = Red500,
                        )
                    }

                    val pin2CodeTextEdited = rememberSaveable { mutableStateOf(false) }
                    val pin2CodeErrorText =
                        if (pin2CodeTextEdited.value && pin2CodeText.text.isNotEmpty()) {
                            if (nfcViewModel
                                    .shouldShowPIN2CodeError(
                                        pin2CodeText.text.toByteArray(StandardCharsets.UTF_8),
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

                    OutlinedTextField(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(top = XSPadding)
                                .clearAndSetSemantics {
                                    testTagsAsResourceId = true
                                    testTag = "signatureUpdateNFCPIN2"
                                    contentDescription = pin2CodeLabel
                                },
                        label = {
                            Text(
                                modifier = modifier.notAccessible(),
                                text = pin2CodeLabel,
                            )
                        },
                        value = pin2CodeText,
                        shape = RectangleShape,
                        onValueChange = {
                            pin2CodeText = it
                            pin2CodeTextEdited.value = true
                        },
                        maxLines = 1,
                        singleLine = true,
                        isError =
                            pin2CodeTextEdited.value &&
                                nfcViewModel
                                    .shouldShowPIN2CodeError(
                                        pin2CodeText.text.toByteArray(
                                            StandardCharsets.UTF_8,
                                        ),
                                    ),
                        textStyle = MaterialTheme.typography.titleLarge,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.NumberPassword,
                            ),
                    )

                    if (pin2CodeErrorText.isNotEmpty()) {
                        Text(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .focusable(enabled = true)
                                    .semantics { contentDescription = pin2CodeErrorText }
                                    .testTag("signatureUpdateNFCPIN2ErrorText"),
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
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        NFCView(
            activity = LocalActivity.current as Activity,
            sharedContainerViewModel = sharedContainerViewModel,
            rememberMe = true,
            isValidToSign = {},
            isSupported = {},
        )
    }
}
