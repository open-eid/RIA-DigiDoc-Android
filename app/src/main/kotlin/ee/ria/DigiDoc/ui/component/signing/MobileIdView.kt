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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.component.toast.ToastUtil
import ee.ria.DigiDoc.ui.theme.Blue500
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val roleDataRequested by mobileIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    val dialogError by mobileIdViewModel.dialogError.asFlow().collectAsState(0)
    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

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
    val rememberMeCheckedState = rememberSaveable { mutableStateOf(true) }
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

    val saveFormParams = {
        if (rememberMeCheckedState.value) {
            sharedSettingsViewModel.dataStore.setPhoneNo(countryCodeAndPhoneText.text)
            sharedSettingsViewModel.dataStore.setPersonalCode(personalCodeText.text)
        } else {
            sharedSettingsViewModel.dataStore.setPhoneNo("")
            sharedSettingsViewModel.dataStore.setPersonalCode("")
        }
    }

    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
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

    if (errorText.isNotEmpty()) {
        ToastUtil.DigiDocToast(errorText)
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
                        text1 = dialogError,
                        text1Arg = text1Arg,
                        text2 = text2,
                        linkText = linkText,
                        linkUrl = linkUrl,
                        cancelButtonClick = {},
                        okButtonClick = {
                            mobileIdViewModel.resetDialogErrorState()
                            dismissDialog()
                        },
                        showCancelButton = false,
                    )
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
                .testTag("signatureUpdateMobileId"),
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
            Text(
                text = stringResource(id = R.string.signature_update_mobile_id_message),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(screenViewLargePadding)
                        .semantics { heading() }
                        .testTag("signatureUpdateMobileIdMessage"),
                textAlign = TextAlign.Center,
            )
            Text(
                text = countryCodeAndPhoneNumberLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(vertical = screenViewLargePadding)
                        .focusable(false)
                        .testTag("signatureUpdateMobileIdPhoneNoLabel"),
            )
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
            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics {
                            testTagsAsResourceId = true
                            testTag = "signatureUpdateMobileIdPhoneNo"
                            contentDescription =
                                "$countryCodeAndPhoneNumberLabel " +
                                "${formatNumbers(countryCodeAndPhoneText.text)} "
                        },
                value = countryCodeAndPhoneText,
                shape = RectangleShape,
                onValueChange = {
                    countryCodeAndPhoneText = it
                    countryCodeAndPhoneTextEdited.value = true
                },
                maxLines = 1,
                singleLine = true,
                isError =
                    countryCodeAndPhoneTextEdited.value &&
                        !mobileIdViewModel.isPhoneNumberValid(countryCodeAndPhoneText.text),
                label = {
                    Text(
                        modifier = modifier.notAccessible(),
                        text = stringResource(id = R.string.mobile_id_country_code_and_phone_number_placeholder),
                        color = Blue500,
                    )
                },
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Decimal,
                    ),
            )
            if (countryCodeAndPhoneErrorText.isNotEmpty()) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .focusable(enabled = true)
                            .semantics { contentDescription = countryCodeAndPhoneErrorText }
                            .testTag("signatureUpdateMobileIdPhoneNoErrorText"),
                    text = countryCodeAndPhoneErrorText,
                    color = Red500,
                )
            }
            Text(
                text = personalCodeLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                        .focusable(false)
                        .testTag("signatureUpdateMobileIdPersonalCodeLabel"),
            )
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

            TextField(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(bottom = screenViewLargePadding)
                        .clearAndSetSemantics {
                            testTagsAsResourceId = true
                            testTag = "signatureUpdateMobileIdPersonalCode"
                            contentDescription =
                                "$personalCodeLabel ${formatNumbers(personalCodeText.text)}"
                        },
                value = personalCodeText,
                shape = RectangleShape,
                onValueChange = {
                    personalCodeText = it
                    personalCodeTextEdited.value = true
                },
                maxLines = 1,
                singleLine = true,
                isError = personalCodeTextEdited.value && !mobileIdViewModel.isPersonalCodeValid(personalCodeText.text),
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions =
                    KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Decimal,
                    ),
            )
            if (personalCodeErrorText.isNotEmpty()) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .focusable(enabled = true)
                            .semantics { contentDescription = personalCodeErrorText }
                            .testTag("signatureUpdateMobileIdPersonalCodeErrorText"),
                    text = personalCodeErrorText,
                    color = Red500,
                )
            }
            TextCheckBox(
                modifier =
                    modifier
                        .testTag("signatureUpdateMobileIdRememberMe"),
                checked = rememberMeCheckedState.value,
                onCheckedChange = { rememberMeCheckedState.value = it },
                title = stringResource(id = R.string.signature_update_remember_me),
                contentDescription = stringResource(id = R.string.signature_update_remember_me).lowercase(),
            )
        }
        CancelAndOkButtonRow(
            okButtonTestTag = "signatureUpdateMobileIdSignButton",
            cancelButtonTestTag = "signatureUpdateMobileIdCancelSigningButton",
            okButtonEnabled =
                mobileIdViewModel.positiveButtonEnabled(
                    countryCodeAndPhoneText.text,
                    personalCodeText.text,
                ),
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
            cancelButtonClick =
                {
                    mobileIdViewModel.resetRoleDataRequested()
                    saveFormParams()
                    cancelButtonClick()
                },
            okButtonClick = {
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
                    CoroutineScope(Dispatchers.IO).launch {
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
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MobileIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        MobileIdView(
            activity = LocalContext.current as Activity,
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
