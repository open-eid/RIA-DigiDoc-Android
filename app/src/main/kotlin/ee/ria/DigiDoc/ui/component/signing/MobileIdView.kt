@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
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
    dismissDialog: () -> Unit = {},
    rememberMe: Boolean,
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val roleDataRequested by mobileIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    val dialogError by mobileIdViewModel.dialogError.asFlow().collectAsState(0)
    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    val focusManager = LocalFocusManager.current

    var countryCodeAndPhoneText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPhoneNo(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPhoneNo().length),
            ),
        )
    }
    var personalCodeText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPersonalCode().length),
            ),
        )
    }
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setPhoneNo(countryCodeAndPhoneText.text)
            sharedSettingsViewModel.dataStore.setPersonalCode(personalCodeText.text)
        } else {
            sharedSettingsViewModel.dataStore.setPhoneNo("372")
            sharedSettingsViewModel.dataStore.setPersonalCode("")
        }
    }

    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
    }

    val countryCodeAndPhoneTextEdited = rememberSaveable { mutableStateOf(false) }
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

    val personalCodeTextEdited = rememberSaveable { mutableStateOf(false) }
    val personalCodeErrorText =
        if (personalCodeTextEdited.value && personalCodeText.text.isNotEmpty()) {
            if (!mobileIdViewModel.isPersonalCodeCorrect(personalCodeText.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)
            } else {
                ""
            }
        } else {
            ""
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
                mobileIdViewModel.resetRoleDataRequested()
                dismissDialog()
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.dialogError) {
        mobileIdViewModel.dialogError.asFlow()
            .filterNotNull()
            .filterNot { it == 0 }
            .collect {
                withContext(Main) {
                    mobileIdViewModel.resetErrorState()
                    showErrorDialog.value = true
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
                shape = RoundedCornerShape(SPadding),
            ) {
                MobileIdSignatureUpdateContainer(
                    modifier = modifier,
                    mobileIdViewModel = mobileIdViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        mobileIdViewModel.cancelMobileIdWorkRequest(signedContainer)
                    },
                )
                InvisibleElement(modifier = modifier)
            }
        }
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
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface),
                onDismissRequest = { dismissDialog() },
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
                                .testTag("mobileIdErrorContainer"),
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
                                mobileIdViewModel.resetDialogErrorState()
                                dismissDialog()
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
                .testTag("signatureUpdateMobileId"),
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else {
            val isValid =
                countryCodeAndPhoneText.text.isNotEmpty() &&
                    personalCodeText.text.isNotEmpty() &&
                    mobileIdViewModel.isPersonalCodeCorrect(personalCodeText.text)

            LaunchedEffect(isValid) {
                isValidToSign(isValid)
            }

            LaunchedEffect(Unit, isValid) {
                if (isValid) {
                    signAction {
                        if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                            mobileIdViewModel.setRoleDataRequested(true)
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
                                mobileIdViewModel.performMobileIdWorkRequest(
                                    activity = activity,
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
                    }
                }
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
                    label = {
                        Text(
                            modifier = modifier.notAccessible(),
                            text = stringResource(id = R.string.signature_update_mobile_id_phone_no),
                        )
                    },
                    placeholder = {
                        Text(
                            modifier = modifier.notAccessible(),
                            text =
                                stringResource(
                                    id = R.string.mobile_id_country_code_and_phone_number_placeholder,
                                ),
                        )
                    },
                    value = countryCodeAndPhoneText,
                    singleLine = true,
                    onValueChange = {
                        countryCodeAndPhoneText = it
                        countryCodeAndPhoneTextEdited.value = true
                    },
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(top = SPadding, bottom = XSPadding)
                            .clearAndSetSemantics {
                                testTagsAsResourceId = true
                                contentDescription =
                                    "$countryCodeAndPhoneNumberLabel " +
                                    "${formatNumbers(countryCodeAndPhoneText.text)} "
                            }
                            .testTag("signatureUpdateMobileIdPhoneNo"),
                    shape = RectangleShape,
                    isError =
                        countryCodeAndPhoneTextEdited.value &&
                            !mobileIdViewModel.isPhoneNumberValid(countryCodeAndPhoneText.text),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Decimal,
                        ),
                )
                if (countryCodeAndPhoneErrorText.isNotEmpty()) {
                    Text(
                        modifier =
                            modifier.fillMaxWidth()
                                .focusable(enabled = true)
                                .semantics { contentDescription = countryCodeAndPhoneErrorText }
                                .testTag("mobileIdPhoneNoErrorText"),
                        text = countryCodeAndPhoneErrorText,
                        color = Red500,
                    )
                }

                OutlinedTextField(
                    label = {
                        Text(stringResource(R.string.signature_update_mobile_id_personal_code))
                    },
                    value = personalCodeText,
                    singleLine = true,
                    onValueChange = {
                        personalCodeText = it
                        personalCodeTextEdited.value = true
                    },
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(top = XSPadding, bottom = SPadding)
                            .clearAndSetSemantics {
                                testTagsAsResourceId = true
                                contentDescription =
                                    "$personalCodeLabel ${formatNumbers(personalCodeText.text)}"
                            }
                            .testTag("signatureUpdateMobileIdPersonalCode"),
                    keyboardOptions =
                        KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal,
                        ),
                    shape = RectangleShape,
                    isError =
                        personalCodeTextEdited.value &&
                            !mobileIdViewModel.isPersonalCodeValid(
                                personalCodeText.text,
                            ),
                )
                if (personalCodeErrorText.isNotEmpty()) {
                    Text(
                        modifier =
                            modifier.fillMaxWidth()
                                .focusable(enabled = true)
                                .semantics { contentDescription = personalCodeErrorText }
                                .testTag("signatureUpdateMobileIdPersonalCodeErrorText"),
                        text = personalCodeErrorText,
                        color = Red500,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MobileIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        MobileIdView(
            activity = LocalActivity.current as Activity,
            sharedContainerViewModel = sharedContainerViewModel,
            rememberMe = true,
            isValidToSign = {},
            signAction = {},
        )
    }
}
