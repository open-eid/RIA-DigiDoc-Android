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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringArrayResource
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
import ee.ria.DigiDoc.ui.component.shared.SelectionSpinner
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.component.toast.ToastUtil
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.viewmodel.SmartIdViewModel
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
fun SmartIdView(
    activity: Activity,
    modifier: Modifier = Modifier,
    signatureAddController: NavHostController,
    dismissDialog: () -> Unit = {},
    cancelButtonClick: () -> Unit = {},
    smartIdViewModel: SmartIdViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)
    val dialogError by smartIdViewModel.dialogError.asFlow().collectAsState(0)
    val roleDataRequested by smartIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val smartIdCountryLabel = stringResource(id = R.string.signature_update_smart_id_country)
    val smartIdPersonalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

    val focusManager = LocalFocusManager.current

    val countriesList = stringArrayResource(id = R.array.smart_id_country)
    var selectedCountry by rememberSaveable { mutableIntStateOf(sharedSettingsViewModel.dataStore.getCountry()) }
    var personalCodeText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getSidPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getSidPersonalCode().length),
            ),
        )
    }
    val rememberMeCheckedState = rememberSaveable { mutableStateOf(true) }

    val itemSelectedTitle = stringResource(id = R.string.item_selected)
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)
    var errorText by remember { mutableStateOf("") }
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    val saveFormParams = {
        if (rememberMeCheckedState.value) {
            sharedSettingsViewModel.dataStore.setSidPersonalCode(personalCodeText.text)
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
                            smartIdViewModel.resetDialogErrorState()
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
                .testTag("signatureUpdateSmartId"),
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
                text = stringResource(id = R.string.signature_update_smart_id_message),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(screenViewLargePadding)
                        .semantics { heading() }
                        .testTag("signatureUpdateSmartIdMessage"),
                textAlign = TextAlign.Center,
            )
            Text(
                text = smartIdCountryLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(vertical = screenViewLargePadding)
                        .focusable(false)
                        .testTag("signatureUpdateSmartIdCountryLabel"),
            )
            SelectionSpinner(
                list = countriesList,
                preselected = selectedCountry,
                onSelectionChanged = {
                    selectedCountry = it
                },
                modifier =
                    modifier
                        .clearAndSetSemantics {
                            contentDescription =
                                "$smartIdCountryLabel ${countriesList[selectedCountry]} $itemSelectedTitle"
                        }
                        .testTag("signatureUpdateSmartIdCountry"),
            )
            Text(
                text = smartIdPersonalCodeLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(
                            top = screenViewExtraLargePadding,
                            bottom = screenViewLargePadding,
                        )
                        .focusable(false)
                        .testTag("signatureUpdateSmartIdPersonalCodeLabel"),
            )
            val personalCodeTextEdited = rememberSaveable { mutableStateOf(false) }
            val personalCodeErrorText =
                if (personalCodeTextEdited.value && personalCodeText.text.isNotEmpty()) {
                    if (!smartIdViewModel.isPersonalCodeCorrect(personalCodeText.text)) {
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
                            testTag = "signatureUpdateSmartIdPersonalCode"
                            this.contentDescription =
                                "$smartIdPersonalCodeLabel " +
                                "${formatNumbers(personalCodeText.text)} "
                        }
                        .testTag("signatureUpdateSmartIdPersonalCode"),
                value = personalCodeText,
                shape = RectangleShape,
                onValueChange = {
                    personalCodeText = it
                    personalCodeTextEdited.value = true
                },
                maxLines = 1,
                singleLine = true,
                isError = personalCodeTextEdited.value && !smartIdViewModel.isPersonalCodeValid(personalCodeText.text),
                textStyle = MaterialTheme.typography.titleLarge,
                keyboardOptions =
                    KeyboardOptions(
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
                            .testTag("signatureUpdateSmartIdPersonalCodeErrorText"),
                    text = personalCodeErrorText,
                    color = Red500,
                )
            }
            TextCheckBox(
                modifier =
                    modifier
                        .testTag("signatureUpdateSmartIdRememberMe"),
                checked = rememberMeCheckedState.value,
                onCheckedChange = { rememberMeCheckedState.value = it },
                title = stringResource(id = R.string.signature_update_remember_me),
                contentDescription = stringResource(id = R.string.signature_update_remember_me).lowercase(),
            )
        }
        CancelAndOkButtonRow(
            okButtonTestTag = "signatureUpdateSmartIdSignButton",
            cancelButtonTestTag = "signatureUpdateSmartIdCancelSigningButton",
            okButtonEnabled =
                smartIdViewModel.positiveButtonEnabled(
                    selectedCountry,
                    personalCodeText.text,
                ),
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button),
            okButtonContentDescription = stringResource(id = R.string.sign_button),
            cancelButtonClick = {
                smartIdViewModel.resetRoleDataRequested()
                saveFormParams()
                cancelButtonClick()
            },
            okButtonClick = {
                if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                    smartIdViewModel.setRoleDataRequested(true)
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
                        smartIdViewModel.performSmartIdWorkRequest(
                            activity = activity,
                            context = context,
                            displayMessage = displayMessage,
                            container = signedContainer,
                            personalCode = personalCodeText.text,
                            country = selectedCountry,
                            roleData = roleDataRequest,
                        )
                        smartIdViewModel.resetRoleDataRequested()
                    }
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SmartIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        SmartIdView(
            activity = LocalContext.current as Activity,
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
