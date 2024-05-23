@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.SelectionSpinner
import ee.ria.DigiDoc.ui.component.shared.TextCheckBox
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.SettingsViewModel
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.SmartIdViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartIdView(
    modifier: Modifier = Modifier,
    cancelButtonClick: () -> Unit = {},
    smartIdViewModel: SmartIdViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val signedContainer by sharedContainerViewModel.signedContainer.asFlow().collectAsState(null)

    LaunchedEffect(smartIdViewModel.status) {
        smartIdViewModel.status.asFlow().collect { status ->
            status?.let {
                if (status == SessionStatusResponseProcessStatus.OK) {
                    sharedContainerViewModel.setSignedSidStatus(status)
                    smartIdViewModel.resetStatus()
                }
            }
        }
    }

    LaunchedEffect(smartIdViewModel.errorState) {
        smartIdViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(smartIdViewModel.signedContainer) {
        smartIdViewModel.signedContainer.asFlow().collect { signedContainer ->
            signedContainer?.let {
                sharedContainerViewModel.setSignedContainer(it)
                smartIdViewModel.resetSignedContainer()
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
                        .verticalScroll(rememberScrollState())
                        .padding(Dimensions.alertDialogOuterPadding),
            ) {
                SmartIdSignatureUpdateContainer(
                    smartIdViewModel = smartIdViewModel,
                    onCancelButtonClick = {
                        dismissSignatureUpdateContainerDialog()
                        smartIdViewModel.cancelMobileIdWorkRequest()
                    },
                )
            }
        }
    }
    Column {
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_message),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(id = R.string.signature_update_smart_id_country),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        val countriesList = stringArrayResource(id = R.array.smart_id_country)
        var selectedCountry by remember { mutableIntStateOf(settingsViewModel.dataStore.getCountry()) }
        SelectionSpinner(
            list = countriesList,
            preselected = selectedCountry,
            onSelectionChanged = {
                selectedCountry = it
            },
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
        )
        Text(
            text = stringResource(id = R.string.signature_update_mobile_id_personal_code),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(vertical = textVerticalPadding),
        )
        var personalCodeText by remember {
            mutableStateOf(
                TextFieldValue(
                    text = settingsViewModel.dataStore.getSidPersonalCode(),
                ),
            )
        }
        TextField(
            modifier = modifier.fillMaxWidth().height(Dimensions.textFieldHeight),
            value = personalCodeText,
            shape = RectangleShape,
            onValueChange = {
                personalCodeText = it
            },
            maxLines = 1,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        val rememberMeCheckedState = remember { mutableStateOf(true) }
        TextCheckBox(
            checked = rememberMeCheckedState.value,
            onCheckedChange = { rememberMeCheckedState.value = it },
            title = stringResource(id = R.string.signature_update_mobile_id_remember_me),
            contentDescription = stringResource(id = R.string.signature_update_mobile_id_remember_me).lowercase(),
        )
        CancelAndOkButtonRow(
            okButtonEnabled =
                smartIdViewModel.positiveButtonEnabled(
                    selectedCountry,
                    personalCodeText.text,
                ),
            cancelButtonTitle = stringResource(id = R.string.cancel_button),
            okButtonTitle = stringResource(id = R.string.sign_button),
            cancelButtonContentDescription = "",
            okButtonContentDescription = "",
            cancelButtonClick = cancelButtonClick,
            okButtonClick = {
                openSignatureUpdateContainerDialog.value = true
                if (rememberMeCheckedState.value) {
                    settingsViewModel.dataStore.setSidPersonalCode(personalCodeText.text)
                    settingsViewModel.dataStore.setCountry(selectedCountry)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    smartIdViewModel.performMobileIdWorkRequest(
                        container = signedContainer,
                        personalCode = personalCodeText.text,
                        country = selectedCountry,
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
fun SmartIdViewPreview() {
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        SmartIdView(
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
