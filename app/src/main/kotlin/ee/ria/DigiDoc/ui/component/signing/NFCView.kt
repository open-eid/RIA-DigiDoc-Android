@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.NFCConstants.CAN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN2_MIN_LENGTH
import ee.ria.DigiDoc.common.Constant.NFCConstants.PIN_MAX_LENGTH
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.NFCViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.Arrays
import java.util.stream.Collectors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NFCView(
    activity: Activity,
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    nfcViewModel: NFCViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current

    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    var nfcStatus by remember { mutableStateOf(nfcViewModel.getNFCStatus(activity)) }

    val roleDataRequested by nfcViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val canNumberLabel = stringResource(id = R.string.signature_update_nfc_can)
    val pin2CodeLabel = stringResource(id = R.string.signature_update_nfc_pin2)

    var canNumberText by remember {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getCanNumber(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getCanNumber().length),
            ),
        )
    }
    var pin2CodeText by remember {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            ),
        )
    }

    val roleLabel = stringResource(id = R.string.main_settings_role_title)
    val cityLabel = stringResource(id = R.string.main_settings_city_title)
    val stateLabel = stringResource(id = R.string.main_settings_county_title)
    val countryLabel = stringResource(id = R.string.main_settings_country_title)
    val zipLabel = stringResource(id = R.string.main_settings_postal_code_title)

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

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var roleAndAddressHeadingTextLoaded by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
    }

    var pin2CodeVisible by rememberSaveable { mutableStateOf(false) }

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
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
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
                    nfcViewModel = nfcViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        nfcViewModel.cancelNFCWorkRequest(signedContainer)
                    },
                )
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
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("signatureUpdateNFC"),
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            Text(
                text = stringResource(id = R.string.signature_update_signature_role_and_address_info_title),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(screenViewSmallPadding)
                        .semantics {
                            heading()
                        }
                        .focusRequester(focusRequester)
                        .focusable(enabled = true)
                        .focusTarget()
                        .focusProperties { canFocus = true }
                        .onGloballyPositioned {
                            if (!roleAndAddressHeadingTextLoaded) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    focusRequester.requestFocus()
                                    focusManager.clearFocus()
                                    delay(200)
                                    focusRequester.requestFocus()
                                    roleAndAddressHeadingTextLoaded = true
                                }
                            }
                        },
                textAlign = TextAlign.Center,
            )
            Text(
                text = roleLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                        .notAccessible()
                        .testTag("signatureUpdateRoleLabel"),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewSmallPadding)
                        .clearAndSetSemantics {
                            this.contentDescription =
                                "$roleLabel ${rolesAndResolutionsText.text}"
                        }
                        .testTag("signatureUpdateRoleText"),
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
                        .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                        .notAccessible()
                        .testTag("signatureUpdateRoleCityLabel"),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewSmallPadding)
                        .clearAndSetSemantics {
                            this.contentDescription =
                                "$cityLabel ${cityText.text}"
                        }
                        .testTag("signatureUpdateRoleCityText"),
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
                        .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                        .notAccessible()
                        .testTag("signatureUpdateRoleStateLabel"),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewSmallPadding)
                        .clearAndSetSemantics {
                            this.contentDescription =
                                "$stateLabel ${stateText.text}"
                        }
                        .testTag("signatureUpdateRoleStateText"),
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
                        .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                        .notAccessible()
                        .testTag("signatureUpdateRoleCountryLabel"),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewSmallPadding)
                        .clearAndSetSemantics {
                            this.contentDescription =
                                "$countryLabel ${countryText.text}"
                        }
                        .testTag("signatureUpdateRoleCountryText"),
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
                        .padding(top = screenViewSmallPadding, bottom = screenViewSmallPadding)
                        .notAccessible()
                        .testTag("signatureUpdateRoleZipLabel"),
            )
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewSmallPadding)
                        .clearAndSetSemantics {
                            this.contentDescription =
                                "$zipLabel ${formatNumbers(zipText.text)}"
                        }
                        .testTag("signatureUpdateRoleZipText"),
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
            if (nfcStatus !== NfcSmartCardReaderManager.NfcStatus.NFC_ACTIVE) {
                SignatureAddRadioGroup(
                    modifier = modifier,
                    navController = signatureAddController,
                    selectedRadioItem = sharedSettingsViewModel.dataStore.getSignatureAddMethod(),
                    sharedSettingsViewModel = sharedSettingsViewModel,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_icon_nfc),
                    contentDescription = null,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(screenViewLargePadding)
                            .notAccessible()
                            .testTag("signatureUpdateNFCIcon"),
                )
                val nfcStatusText =
                    if (nfcStatus === NfcSmartCardReaderManager.NfcStatus.NFC_NOT_SUPPORTED) {
                        stringResource(id = R.string.signature_update_nfc_adapter_missing)
                    } else {
                        stringResource(id = R.string.signature_update_nfc_turned_off)
                    }
                Text(
                    text = nfcStatusText,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .padding(screenViewLargePadding)
                            .semantics { heading() }
                            .testTag("signatureUpdateNFCNotFoundMessage"),
                    textAlign = TextAlign.Center,
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
                    modifier =
                        modifier
                            .padding(screenViewLargePadding)
                            .semantics { heading() }
                            .testTag("signatureUpdateNFCMessage"),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = canNumberLabel,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .padding(vertical = screenViewLargePadding)
                            .notAccessible()
                            .testTag("signatureUpdateNFCCANLabel"),
                )
                val canNumberTextEdited = remember { mutableStateOf(false) }
                val canNumberErrorText =
                    if (canNumberTextEdited.value && canNumberText.text.isNotEmpty()) {
                        if (!nfcViewModel.isCANNumberValid(canNumberText.text)) {
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
                TextField(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {
                                contentDescription =
                                    "$canNumberLabel " +
                                    "${formatNumbers(canNumberText.text)} "
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
                            text = stringResource(id = R.string.nfc_sign_can_location),
                            color = Blue500,
                        )
                    },
                    maxLines = 1,
                    singleLine = true,
                    isError =
                        canNumberTextEdited.value &&
                            !nfcViewModel.isCANNumberValid(canNumberText.text),
                    textStyle = MaterialTheme.typography.titleLarge,
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Decimal,
                        ),
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
                Text(
                    text = pin2CodeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    modifier =
                        modifier
                            .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                            .notAccessible()
                            .testTag("signatureUpdateNFCPIN2Label"),
                )
                val pin2CodeErrorText =
                    if (pin2CodeText.text.isNotEmpty()) {
                        if (!nfcViewModel
                                .isPIN2CodeValid(
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

                TextField(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(bottom = screenViewLargePadding)
                            .clearAndSetSemantics {
                                contentDescription = pin2CodeLabel
                            }
                            .testTag("signatureUpdateNFCPIN2"),
                    value = pin2CodeText,
                    shape = RectangleShape,
                    onValueChange = {
                        pin2CodeText = it
                    },
                    maxLines = 1,
                    singleLine = true,
                    isError =
                        !nfcViewModel
                            .isPIN2CodeValid(pin2CodeText.text.toByteArray(StandardCharsets.UTF_8)),
                    textStyle = MaterialTheme.typography.titleLarge,
                    visualTransformation =
                        if (pin2CodeVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.NumberPassword,
                        ),
                    trailingIcon = {
                        val image =
                            if (pin2CodeVisible) {
                                ImageVector.vectorResource(id = R.drawable.ic_visibility)
                            } else {
                                ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                            }
                        val description =
                            if (pin2CodeVisible) {
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
                                    .testTag("signatureUpdateNFCPIN2Visible"),
                            onClick = { pin2CodeVisible = !pin2CodeVisible },
                        ) {
                            Icon(imageVector = image, description)
                        }
                    },
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
        CancelAndOkButtonRow(
            okButtonTestTag = "signatureUpdateNFCSignButton",
            cancelButtonTestTag = "signatureUpdateNFCCancelSigningButton",
            okButtonEnabled =
                nfcViewModel.positiveButtonEnabled(
                    canNumberText.text,
                    pin2CodeText.text.toByteArray(StandardCharsets.UTF_8),
                ),
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
            cancelButtonClick =
                {
                    nfcViewModel.resetRoleDataRequested()
                    cancelButtonClick()
                },
            okButtonClick = {
                if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                    nfcViewModel.setRoleDataRequested(true)
                } else {
                    openSignatureUpdateContainerDialog.value = true
                    sharedSettingsViewModel.dataStore.setCanNumber(canNumberText.text)
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
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NFCViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        NFCView(
            activity = LocalContext.current as Activity,
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
