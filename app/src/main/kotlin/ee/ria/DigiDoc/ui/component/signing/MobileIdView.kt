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
import androidx.compose.ui.graphics.RectangleShape
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
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
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
    isSigning: Boolean,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    isAddingRoleAndAddress: Boolean,
    rememberMe: Boolean,
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val dialogError by mobileIdViewModel.dialogError.asFlow().collectAsState(0)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }

    val focusManager = LocalFocusManager.current

    var countryCodeAndPhone by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPhoneNo(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPhoneNo().length),
            ),
        )
    }
    var personalCode by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getPersonalCode().length),
            ),
        )
    }

    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setPhoneNo(countryCodeAndPhone.text)
            sharedSettingsViewModel.dataStore.setPersonalCode(personalCode.text)
        } else {
            sharedSettingsViewModel.dataStore.setPhoneNo("372")
            sharedSettingsViewModel.dataStore.setPersonalCode("")
        }
    }

    val countryCodeAndPhoneEdited = rememberSaveable { mutableStateOf(false) }
    val countryCodeAndPhoneErrorText =
        if (countryCodeAndPhoneEdited.value && countryCodeAndPhone.text.isNotEmpty()) {
            if (mobileIdViewModel.isCountryCodeMissing(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_status_no_country_code)
            } else if (!mobileIdViewModel.isCountryCodeCorrect(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_country_code)
            } else if (!mobileIdViewModel.isPhoneNumberCorrect(countryCodeAndPhone.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_phone_number)
            } else {
                ""
            }
        } else {
            ""
        }

    val personalCodeEdited = rememberSaveable { mutableStateOf(false) }
    val personalCodeErrorText =
        if (personalCodeEdited.value && personalCode.text.isNotEmpty()) {
            if (!mobileIdViewModel.isPersonalCodeCorrect(personalCode.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)
            } else {
                ""
            }
        } else {
            ""
        }

    val phoneNumberFocusRequester = remember { FocusRequester() }
    val personalCodeFocusRequester = remember { FocusRequester() }

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    val phoneNumberWithInvisibleSpaces = TextFieldValue(addInvisibleElement(countryCodeAndPhone.text))
    val personalCodeWithInvisibleSpaces = TextFieldValue(addInvisibleElement(personalCode.text))

    BackHandler {
        onError()
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
                onSuccess()
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
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface),
                onDismissRequest = {
                    showErrorDialog.value = false
                    mobileIdViewModel.resetDialogErrorState()
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
                                showErrorDialog.value = false
                                mobileIdViewModel.resetDialogErrorState()
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
        if (isAddingRoleAndAddress) {
            RoleDataView(modifier, sharedSettingsViewModel, onError)
        } else if (isSigning) {
            MobileIdSignatureUpdateContainer(
                mobileIdViewModel = mobileIdViewModel,
                onError = onError,
            )
        } else {
            val isValid =
                countryCodeAndPhone.text.isNotEmpty() &&
                    personalCode.text.isNotEmpty() &&
                    mobileIdViewModel.isPersonalCodeCorrect(personalCode.text)

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
                            mobileIdViewModel.performMobileIdWorkRequest(
                                activity = activity,
                                context = context,
                                displayMessage = displayMessage,
                                container = signedContainer,
                                personalCode = personalCode.text,
                                phoneNumber = countryCodeAndPhone.text,
                                roleData = roleDataRequest,
                            )
                        }
                    }
                    cancelAction {
                        mobileIdViewModel.cancelMobileIdWorkRequest(signedContainer)
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
                            Text(
                                modifier = modifier.notAccessible(),
                                text = countryCodeAndPhoneNumberLabel,
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
                        value =
                            if (!isTalkBackEnabled(context)) {
                                countryCodeAndPhone
                            } else {
                                phoneNumberWithInvisibleSpaces.copy(
                                    selection = TextRange(phoneNumberWithInvisibleSpaces.text.length),
                                )
                            },
                        singleLine = true,
                        onValueChange = {
                            countryCodeAndPhoneEdited.value = true

                            if (!isTalkBackEnabled(context)) {
                                countryCodeAndPhone = it.copy(selection = TextRange(it.text.length))
                            } else {
                                val noInvisibleElement =
                                    TextFieldValue(removeInvisibleElement(it.text))
                                countryCodeAndPhone =
                                    noInvisibleElement.copy(selection = TextRange(noInvisibleElement.text.length))
                            }
                        },
                        modifier =
                            modifier
                                .focusRequester(phoneNumberFocusRequester)
                                .focusProperties {
                                    next = personalCodeFocusRequester
                                }
                                .weight(1f)
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }
                                .testTag("signatureUpdateMobileIdPhoneNo"),
                        shape = RectangleShape,
                        trailingIcon = {
                            if (!isTalkBackEnabled(context) && countryCodeAndPhone.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    countryCodeAndPhone = TextFieldValue("")
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        },
                        isError =
                            countryCodeAndPhoneEdited.value &&
                                !mobileIdViewModel.isPhoneNumberValid(countryCodeAndPhone.text),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                            ),
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Decimal,
                            ),
                    )
                    if (isTalkBackEnabled(context) && personalCode.text.isNotEmpty()) {
                        IconButton(
                            modifier =
                                modifier
                                    .align(Alignment.CenterVertically),
                            onClick = { personalCode = TextFieldValue("") },
                        ) {
                            Icon(
                                modifier =
                                    modifier
                                        .size(iconSizeXXS)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("smartIdPersonalCodeRemoveIconButton"),
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                }
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

                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(top = MPadding),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        label = {
                            Text(personalCodeLabel)
                        },
                        value =
                            if (!isTalkBackEnabled(context)) {
                                personalCode
                            } else {
                                personalCodeWithInvisibleSpaces.copy(
                                    selection = TextRange(personalCodeWithInvisibleSpaces.text.length),
                                )
                            },
                        singleLine = true,
                        onValueChange = {
                            personalCodeEdited.value = true

                            if (!isTalkBackEnabled(context)) {
                                personalCode = it.copy(selection = TextRange(it.text.length))
                            } else {
                                val noInvisibleElement = TextFieldValue(removeInvisibleElement(it.text))
                                personalCode =
                                    noInvisibleElement.copy(selection = TextRange(noInvisibleElement.text.length))
                            }
                        },
                        modifier =
                            modifier
                                .focusRequester(personalCodeFocusRequester)
                                .focusProperties {
                                    previous = phoneNumberFocusRequester
                                }
                                .weight(1f)
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }
                                .testTag("signatureUpdateMobileIdPersonalCode"),
                        trailingIcon = {
                            if (!isTalkBackEnabled(context) && personalCode.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    personalCode = TextFieldValue("")
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
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Number,
                            ),
                        shape = RectangleShape,
                        isError =
                            personalCodeEdited.value &&
                                !mobileIdViewModel.isPersonalCodeValid(
                                    personalCode.text,
                                ),
                    )
                    if (isTalkBackEnabled(context) && personalCode.text.isNotEmpty()) {
                        IconButton(
                            modifier =
                                modifier
                                    .align(Alignment.CenterVertically),
                            onClick = { personalCode = TextFieldValue("") },
                        ) {
                            Icon(
                                modifier =
                                    modifier
                                        .size(iconSizeXXS)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("smartIdPersonalCodeRemoveIconButton"),
                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                contentDescription = "$clearButtonText $buttonName",
                            )
                        }
                    }
                }
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
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        MobileIdView(
            activity = LocalActivity.current as Activity,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            isSigning = false,
            isAddingRoleAndAddress = false,
            rememberMe = true,
            isValidToSign = {},
        )
    }
}
