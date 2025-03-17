@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
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
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
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
    dismissDialog: () -> Unit = {},
    smartIdViewModel: SmartIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
    isValidToSign: (Boolean) -> Unit,
    fields: (Int, String) -> Unit,
    signAction: (() -> Unit) -> Unit = {},
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val dialogError by smartIdViewModel.dialogError.asFlow().collectAsState(0)
    val roleDataRequested by smartIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val focusManager = LocalFocusManager.current

    val countryOptions = stringArrayResource(id = R.array.smart_id_country)
    var country by remember { mutableStateOf(countryOptions.first()) }
    var selectedCountry by rememberSaveable { mutableIntStateOf(sharedSettingsViewModel.dataStore.getCountry()) }
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
    val rememberMeCheckedState = rememberSaveable { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    val saveFormParams = {
        if (rememberMeCheckedState.value) {
            sharedSettingsViewModel.dataStore.setSidPersonalCode(personalCode.text)
            sharedSettingsViewModel.dataStore.setCountry(selectedCountry)
        } else {
            sharedSettingsViewModel.dataStore.setSidPersonalCode("")
            sharedSettingsViewModel.dataStore.setCountry(0)
        }
    }

    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
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
                dismissDialog()
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
                SmartIdSignatureUpdateContainer(
                    smartIdViewModel = smartIdViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        smartIdViewModel.cancelSmartIdWorkRequest(signedContainer)
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
                            .verticalScroll(rememberScrollState())
                            .padding(SPadding),
                ) {
                    Column(
                        modifier =
                            modifier
                                .padding(SPadding)
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
                                smartIdViewModel.resetDialogErrorState()
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
                .testTag("signatureUpdateSmartId"),
    ) {
        if (getSettingsAskRoleAndAddress() && roleDataRequested == true) {
            RoleDataView(modifier, sharedSettingsViewModel)
        } else {
            val isValid =
                country.isNotEmpty() &&
                    personalCode.text.isNotEmpty() && smartIdViewModel.isPersonalCodeCorrect(personalCode.text)

            LaunchedEffect(isValid) {
                isValidToSign(isValid)
            }

            LaunchedEffect(Unit, fields) {
                fields(selectedCountry, personalCode.text)
            }

            LaunchedEffect(Unit, isValid) {
                if (isValid) {
                    signAction {
                        if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                            smartIdViewModel.setRoleDataRequested(true)
                        } else {
                            openSignatureUpdateContainerDialog.value = true
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
                Box(modifier = modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        label = {
                            Text(stringResource(R.string.signature_update_smart_id_country))
                        },
                        value = country,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onGloballyPositioned { coordinates ->
                                    textFieldSize = coordinates.size.toSize()
                                },
                        trailingIcon = {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_baseline_keyboard_arrow_down_24),
                                contentDescription = stringResource(R.string.signature_update_smart_id_country),
                                modifier = modifier.clickable { expanded = !expanded },
                            )
                        },
                    )

                    if (!expanded) {
                        Box(
                            modifier =
                                modifier
                                    .matchParentSize()
                                    .clickable(
                                        onClick = {
                                            expanded = true
                                        },
                                        interactionSource = interactionSource,
                                        indication = null,
                                    ),
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
                    ) {
                        countryOptions.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    country = selection
                                    expanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    label = {
                        Text(stringResource(R.string.signature_update_mobile_id_personal_code))
                    },
                    value = personalCode,
                    singleLine = true,
                    onValueChange = { personalCode = it },
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(top = MPadding),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    isError = !smartIdViewModel.isPersonalCodeValid(personalCode.text),
                )
                if (personalCode.text.isNotEmpty()) {
                    Text(
                        modifier =
                            modifier.fillMaxWidth()
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
    RIADigiDocTheme {
        SmartIdView(
            activity = LocalActivity.current as Activity,
            sharedContainerViewModel = sharedContainerViewModel,
            isValidToSign = {},
            fields = { _, _ -> },
        )
    }
}
