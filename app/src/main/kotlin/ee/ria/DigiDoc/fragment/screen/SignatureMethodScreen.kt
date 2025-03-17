@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.methods.SigningMethod
import ee.ria.DigiDoc.ui.component.signing.SignatureAddRadioItem
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureMethodScreen(
    modifier: Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val currentSigningMethod = sharedSettingsViewModel.dataStore.getSignatureAddMethod()
    var selectedOption by remember {
        mutableStateOf(
            SigningMethod.entries.find { it.methodName == currentSigningMethod },
        )
    }

    val signingMethodText = stringResource(id = R.string.signature_method)
    val signingMethodSelectedText = stringResource(id = R.string.signature_method_selected)

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                modifier = modifier,
                title = null,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MSPadding),
        ) {
            Text(
                modifier =
                    modifier
                        .padding(XSPadding)
                        .padding(bottom = SPadding),
                text = stringResource(R.string.signature_update_method_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )

            SignatureAddRadioItem().radioItems().forEachIndexed { _, option ->
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(start = XSPadding)
                            .clickable { selectedOption = option.method },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = option.label),
                        modifier = modifier.weight(1f),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                    this.contentDescription =
                                        if (option.method == selectedOption) {
                                            "${option.contentDescription} $signingMethodSelectedText"
                                        } else {
                                            "$signingMethodText ${option.contentDescription}"
                                        }
                                }
                                .testTag(option.testTag),
                        selected = selectedOption == option.method,
                        onClick = { selectedOption = option.method },
                    )
                }
                HorizontalDivider()
            }

            Button(
                onClick = {
                    sharedSettingsViewModel.dataStore.setSignatureAddMethod(
                        selectedOption?.methodName ?: SigningMethod.NFC.methodName,
                    )
                    navController.navigateUp()
                },
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = MSPadding),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    text = stringResource(R.string.signature_update_method_save_button),
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureMethodScreenPreview() {
    val navController = rememberNavController()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    RIADigiDocTheme {
        SignatureMethodScreen(
            modifier = Modifier,
            navController = navController,
            sharedSettingsViewModel = sharedSettingsViewModel,
        )
    }
}
