@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.RoleDataView
import ee.ria.DigiDoc.ui.component.shared.SelectionSpinner
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
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

    val roleDataRequested by smartIdViewModel.roleDataRequested.asFlow().collectAsState(null)
    val getSettingsAskRoleAndAddress = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress

    val smartIdCountryLabel = stringResource(id = R.string.signature_update_smart_id_country)
    val smartIdPersonalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

    val roleLabel = stringResource(id = R.string.main_settings_role_title)
    val cityLabel = stringResource(id = R.string.main_settings_city_title)
    val stateLabel = stringResource(id = R.string.main_settings_county_title)
    val countryLabel = stringResource(id = R.string.main_settings_country_title)
    val zipLabel = stringResource(id = R.string.main_settings_postal_code_title)

    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var roleAndAddressHeadingTextLoaded by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val countriesList = stringArrayResource(id = R.array.smart_id_country)
    var selectedCountry by remember { mutableIntStateOf(sharedSettingsViewModel.dataStore.getCountry()) }
    var personalCodeText by remember {
        mutableStateOf(
            TextFieldValue(
                text = sharedSettingsViewModel.dataStore.getSidPersonalCode(),
                selection = TextRange(sharedSettingsViewModel.dataStore.getSidPersonalCode().length),
            ),
        )
    }
    val rememberMeCheckedState = remember { mutableStateOf(true) }

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
    val itemSelectedTitle = stringResource(id = R.string.item_selected)
    val displayMessage = stringResource(id = R.string.signature_update_mobile_id_display_message)

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
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
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
    val openSignatureUpdateContainerDialog = rememberSaveable { mutableStateOf(false) }
    val dismissSignatureUpdateContainerDialog = {
        openSignatureUpdateContainerDialog.value = false
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
            val personalCodeErrorText =
                if (personalCodeText.text.isNotEmpty()) {
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
                },
                maxLines = 1,
                singleLine = true,
                isError = !smartIdViewModel.isPersonalCodeValid(personalCodeText.text),
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
                cancelButtonClick()
            },
            okButtonClick = {
                if (getSettingsAskRoleAndAddress() && roleDataRequested != true) {
                    smartIdViewModel.setRoleDataRequested(true)
                } else {
                    openSignatureUpdateContainerDialog.value = true
                    if (rememberMeCheckedState.value) {
                        sharedSettingsViewModel.dataStore.setSidPersonalCode(personalCodeText.text)
                        sharedSettingsViewModel.dataStore.setCountry(selectedCountry)
                    }
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
