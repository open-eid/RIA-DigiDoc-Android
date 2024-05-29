@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.MobileIdViewModel
import ee.ria.DigiDoc.viewmodel.SettingsViewModel
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileIdView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
    mobileIdViewModel: MobileIdViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)

    val countryCodeAndPhoneNumberLabel = stringResource(id = R.string.signature_update_mobile_id_phone_no)
    val personalCodeLabel = stringResource(id = R.string.signature_update_mobile_id_personal_code)

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(mobileIdViewModel.status) {
        mobileIdViewModel.status.asFlow().collect { status ->
            status?.let {
                if (status == MobileCreateSignatureProcessStatus.OK) {
                    sharedContainerViewModel.setSignedMidStatus(status)
                    mobileIdViewModel.resetStatus()
                }
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.errorState) {
        mobileIdViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(mobileIdViewModel.signedContainer) {
        mobileIdViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                mobileIdViewModel.resetSignedContainer()
                cancelButtonClick()
            }
        }
    }
    val openSignatureUpdateContainerDialog = remember { mutableStateOf(false) }
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
                MobileIdSignatureUpdateContainer(
                    mobileIdViewModel = mobileIdViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        mobileIdViewModel.cancelMobileIdWorkRequest()
                    },
                )
            }
        }
    }
    Column(
        modifier = modifier.padding(horizontal = screenViewLargePadding),
    ) {
        Text(
            text = stringResource(id = R.string.signature_update_mobile_id_message),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(screenViewLargePadding),
            textAlign = TextAlign.Center,
        )
        Text(
            text = countryCodeAndPhoneNumberLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(vertical = screenViewLargePadding)
                    .notAccessible(),
        )
        var countryCodeAndPhoneText by remember {
            mutableStateOf(TextFieldValue(text = settingsViewModel.dataStore.getPhoneNo()))
        }

        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "$countryCodeAndPhoneNumberLabel ${formatNumbers(countryCodeAndPhoneText.text)}"
                    },
            value = countryCodeAndPhoneText,
            shape = RectangleShape,
            onValueChange = {
                countryCodeAndPhoneText = it
            },
            maxLines = 1,
            singleLine = true,
            label = {
                Text(
                    modifier = modifier.notAccessible(),
                    text = stringResource(id = R.string.mobile_id_country_code_and_phone_number_placeholder),
                )
            },
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Decimal,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = {
                        focusRequester.requestFocus()
                        countryCodeAndPhoneText =
                            countryCodeAndPhoneText.copy(
                                text = countryCodeAndPhoneText.text,
                                selection = TextRange(countryCodeAndPhoneText.text.length),
                            )
                    },
                ),
        )

        Text(
            text = personalCodeLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                modifier
                    .padding(top = screenViewExtraLargePadding, bottom = screenViewLargePadding)
                    .notAccessible(),
        )
        var personalCodeText by remember {
            mutableStateOf(TextFieldValue(text = settingsViewModel.dataStore.getPersonalCode()))
        }
        TextField(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(bottom = screenViewLargePadding)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            personalCodeText =
                                personalCodeText.copy(
                                    text = personalCodeText.text,
                                    selection = TextRange(personalCodeText.text.length),
                                )
                        }
                    }
                    .semantics {
                        contentDescription =
                            "$personalCodeLabel ${formatNumbers(personalCodeText.text)}"
                    },
            value = personalCodeText,
            shape = RectangleShape,
            onValueChange = {
                personalCodeText = it
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Decimal,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                    },
                ),
        )
        val rememberMeCheckedState = remember { mutableStateOf(true) }
        TextCheckBox(
            checked = rememberMeCheckedState.value,
            onCheckedChange = { rememberMeCheckedState.value = it },
            title = stringResource(id = R.string.signature_update_remember_me),
            contentDescription = stringResource(id = R.string.signature_update_remember_me).lowercase(),
        )
        CancelAndOkButtonRow(
            okButtonEnabled =
                mobileIdViewModel.positiveButtonEnabled(
                    countryCodeAndPhoneText.text,
                    personalCodeText.text,
                ),
            cancelButtonTitle = R.string.cancel_button,
            okButtonTitle = R.string.sign_button,
            cancelButtonContentDescription = stringResource(id = R.string.cancel_button).lowercase(),
            okButtonContentDescription = stringResource(id = R.string.sign_button).lowercase(),
            cancelButtonClick = cancelButtonClick,
            okButtonClick = {
                openSignatureUpdateContainerDialog.value = true
                if (rememberMeCheckedState.value) {
                    settingsViewModel.dataStore.setPhoneNo(countryCodeAndPhoneText.text)
                    settingsViewModel.dataStore.setPersonalCode(personalCodeText.text)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    mobileIdViewModel.performMobileIdWorkRequest(
                        container = signedContainer,
                        personalCode = personalCodeText.text,
                        phoneNumber = countryCodeAndPhoneText.text,
                        roleData = null,
                    )
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
    RIADigiDocTheme {
        MobileIdView(
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
