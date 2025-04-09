@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
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
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.dialog.OptionChooserDialog
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
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.SmartIdViewModel
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
fun SmartIdView(
    activity: Activity,
    modifier: Modifier = Modifier,
    isSigning: Boolean,
    onError: () -> Unit = {},
    onSuccess: () -> Unit = {},
    rememberMe: Boolean,
    smartIdViewModel: SmartIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
    cancelAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val dialogError by smartIdViewModel.dialogError.asFlow().collectAsState(0)
    val roleDataRequested by smartIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val focusManager = LocalFocusManager.current

    val countryOptions = stringArrayResource(id = R.array.smart_id_country)
    var selectedCountry by rememberSaveable { mutableIntStateOf(sharedSettingsViewModel.dataStore.getCountry()) }
    var countryString by remember { mutableStateOf(countryOptions[selectedCountry]) }
    var openOptionChooserDialog by remember { mutableStateOf(false) }
    var shouldRememberMe by rememberSaveable { mutableStateOf(rememberMe) }
    var personalCode by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getSidPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getSidPersonalCode().length),
            ),
        )
    }
    val personalCodeErrorText =
        if (personalCode.text.isNotEmpty()) {
            if (!smartIdViewModel.isPersonalCodeCorrect(personalCode.text)) {
                stringResource(id = R.string.signature_update_mobile_id_invalid_personal_code)
            } else {
                ""
            }
        } else {
            ""
        }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val countryInteractionSource = remember { MutableInteractionSource() }
    val countryFocusRequester = remember { FocusRequester() }
    val personalCodeFocusRequester = remember { FocusRequester() }

    val countryTitleText = stringResource(R.string.signature_update_smart_id_country)
    val personalCodeTitleText = stringResource(R.string.signature_update_mobile_id_personal_code)
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    val saveFormParams = {
        if (shouldRememberMe) {
            sharedSettingsViewModel.dataStore.setSidPersonalCode(personalCode.text)
            sharedSettingsViewModel.dataStore.setCountry(selectedCountry)
        } else {
            sharedSettingsViewModel.dataStore.setSidPersonalCode("")
            sharedSettingsViewModel.dataStore.setCountry(0)
        }
    }

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    val personalCodeWithInvisibleSpaces = TextFieldValue(addInvisibleElement(personalCode.text))

    BackHandler {
        onError()
    }

    LaunchedEffect(smartIdViewModel.status) {
        smartIdViewModel.status.asFlow().collect { status ->
            status?.let {
                sharedContainerViewModel.setSignedSidStatus(status)
                smartIdViewModel.resetStatus()
            }
        }
    }

    LaunchedEffect(smartIdViewModel.errorState) {
        smartIdViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    if (errorState != "") {
                        errorText = errorState
                    }

                    smartIdViewModel.resetErrorState()
                }
            }
        }
    }

    LaunchedEffect(smartIdViewModel.signedContainer) {
        smartIdViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                smartIdViewModel.resetSignedContainer()
                smartIdViewModel.resetRoleDataRequested()
                onSuccess()
            }
        }
    }

    LaunchedEffect(smartIdViewModel.dialogError) {
        smartIdViewModel.dialogError.asFlow()
            .filterNotNull()
            .filterNot { it == 0 }
            .collect {
                withContext(Main) {
                    smartIdViewModel.resetErrorState()
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
            onError()
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface),
                onDismissRequest = {
                    showErrorDialog.value = false
                    smartIdViewModel.resetDialogErrorState()
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
                                smartIdViewModel.resetDialogErrorState()
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
                .testTag("signatureUpdateSmartId"),
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else if (isSigning) {
            SmartIdSignatureUpdateContainer(
                smartIdViewModel = smartIdViewModel,
                onError = onError,
            )
        } else {
            val isValid =
                countryString.isNotEmpty() &&
                    personalCode.text.isNotEmpty() &&
                    smartIdViewModel.isPersonalCodeCorrect(
                        personalCode.text,
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
                        if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                            smartIdViewModel.setRoleDataRequested(true)
                        } else {
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
                                smartIdViewModel.performSmartIdWorkRequest(
                                    activity = activity,
                                    context = context,
                                    displayMessage = displayMessage,
                                    container = signedContainer,
                                    personalCode = personalCode.text,
                                    country = selectedCountry,
                                    roleData = roleDataRequest,
                                )
                                smartIdViewModel.resetRoleDataRequested()
                            }
                        }
                    }
                    cancelAction {
                        smartIdViewModel.cancelSmartIdWorkRequest(signedContainer)
                    }
                }
            }

            Column(
                modifier =
                    modifier
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag("smartIdViewContainer"),
            ) {
                Box(
                    modifier =
                        modifier
                            .focusable(false)
                            .fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        label = {
                            Text(countryTitleText)
                        },
                        value = countryString,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier =
                            modifier
                                .focusRequester(countryFocusRequester)
                                .focusProperties {
                                    next = personalCodeFocusRequester
                                }
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    textFieldSize = coordinates.size.toSize()
                                },
                        trailingIcon = {
                            Icon(
                                imageVector =
                                    ImageVector.vectorResource(
                                        R.drawable.ic_baseline_keyboard_arrow_down_24,
                                    ),
                                contentDescription = "$countryTitleText $countryString",
                                modifier =
                                    modifier
                                        .clickable {
                                            openOptionChooserDialog = !openOptionChooserDialog
                                        },
                            )
                        },
                    )

                    if (!openOptionChooserDialog) {
                        Box(
                            modifier =
                                modifier
                                    .focusable(false)
                                    .matchParentSize()
                                    .clickable(
                                        onClick = {
                                            openOptionChooserDialog = true
                                        },
                                        interactionSource = countryInteractionSource,
                                        indication = null,
                                    )
                                    .semantics {
                                        contentDescription = "$countryTitleText $countryString"
                                    },
                        )
                    } else {
                        BasicAlertDialog(
                            modifier =
                                modifier
                                    .semantics {
                                        testTagsAsResourceId = true
                                    },
                            onDismissRequest = {
                                openOptionChooserDialog = false
                            },
                        ) {
                            Surface(
                                modifier =
                                    modifier
                                        .wrapContentHeight()
                                        .wrapContentWidth()
                                        .verticalScroll(rememberScrollState())
                                        .padding(XSPadding)
                                        .testTag("smartIdCountryDialog"),
                            ) {
                                OptionChooserDialog(
                                    modifier = modifier,
                                    title = R.string.choose_country_option,
                                    choices = countryOptions.map { it.toString() },
                                    selectedChoice = selectedCountry,
                                    cancelButtonClick = {
                                        openOptionChooserDialog = false
                                    },
                                    okButtonClick = { selectedId ->
                                        selectedCountry = selectedId
                                        countryString = countryOptions[selectedId]
                                        openOptionChooserDialog = false
                                    },
                                )
                                InvisibleElement(modifier = modifier)
                            }
                        }
                    }
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
                        enabled = countryString.isNotEmpty(),
                        label = {
                            Text(text = personalCodeTitleText)
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
                                    previous = countryFocusRequester
                                }
                                .weight(1f)
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }
                                .testTag("smartIdPersonalCodeTextField"),
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
                        isError = !smartIdViewModel.isPersonalCodeValid(personalCode.text),
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
                            modifier
                                .fillMaxWidth()
                                .focusable(true)
                                .semantics { contentDescription = personalCodeErrorText }
                                .testTag("smartIdPersonalCodeErrorText"),
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
fun SmartIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    RIADigiDocTheme {
        SmartIdView(
            activity = LocalActivity.current as Activity,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            isSigning = false,
            rememberMe = true,
            isValidToSign = {},
        )
    }
}
