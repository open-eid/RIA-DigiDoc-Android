@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebEidScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: WebEidViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel = hiltViewModel(),
    sharedMenuViewModel: SharedMenuViewModel,
) {
    val noAuthLabel = stringResource(id = R.string.web_eid_auth_no_payload)
    val activity = LocalActivity.current as Activity
    val authRequest = viewModel.authRequest.collectAsState().value
    var isWebEidAuthenticating by rememberSaveable { mutableStateOf(false) }
    var webEidAuthenticateAction by remember { mutableStateOf<() -> Unit>({}) }
    var cancelWebEidAuthenticateAction by remember { mutableStateOf<() -> Unit>({}) }
    var isValidToWebEidAuthenticate by remember { mutableStateOf(false) }
    var nfcSupported by remember { mutableStateOf(false) }

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()
    val messages by SnackBarManager.messages.collectAsState(emptyList())
    val dialogError by viewModel.dialogError.asFlow().collectAsState(0)
    val showErrorDialog = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    LaunchedEffect(viewModel.dialogError) {
        viewModel.dialogError
            .asFlow()
            .filterNotNull()
            .filterNot { it == 0 }
            .collect {
                withContext(Main) {
                    showErrorDialog.value = true
                }
            }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                modifier = modifier.padding(vertical = SPadding),
                hostState = snackBarHostState,
            )
        },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = null,
                showNavigationIcon = false,
                onLeftButtonClick = {},
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )

        if (showErrorDialog.value) {
            BasicAlertDialog(
                modifier =
                    modifier
                        .clip(buttonRoundCornerShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .semantics {
                            testTagsAsResourceId = true
                        }.testTag("webEidErrorDialog"),
                onDismissRequest = {},
            ) {
                Surface(
                    modifier =
                        modifier
                            .padding(SPadding)
                            .wrapContentHeight()
                            .wrapContentWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Column {
                        Box(
                            modifier = modifier.fillMaxWidth(),
                        ) {
                            Text(
                                modifier =
                                    modifier
                                        .padding(horizontal = SPadding)
                                        .padding(top = XSPadding),
                                text = stringResource(id = R.string.web_eid_request_error),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        DynamicText(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .padding(SPadding),
                            text = stringResource(dialogError),
                        )
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TextButton(onClick = {
                                activity.finishAndRemoveTask()
                            }) {
                                Text(
                                    modifier =
                                        modifier
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }.testTag("webEidRequestErrorCloseButton"),
                                    text = stringResource(R.string.close_button),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    InvisibleElement(modifier = modifier)
                }
            }
        }

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
                text = stringResource(R.string.web_eid_auth_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            if (authRequest != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = authRequest.origin,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.web_eid_requests_authentication),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }

                NFCView(
                    activity = activity,
                    identityAction = IdentityAction.AUTH,
                    isSigning = false,
                    isDecrypting = false,
                    isWebEidAuthenticating = isWebEidAuthenticating,
                    onError = {
                        isWebEidAuthenticating = false
                        cancelWebEidAuthenticateAction()
                    },
                    onSuccess = {
                        isWebEidAuthenticating = false
                        navController.navigateUp()
                    },
                    sharedSettingsViewModel = sharedSettingsViewModel,
                    sharedContainerViewModel = sharedContainerViewModel,
                    isSupported = { supported ->
                        nfcSupported = supported
                    },
                    isValidToWebEidAuthenticate = { isValid ->
                        isValidToWebEidAuthenticate = isValid
                    },
                    authenticateWebEidAction = { action ->
                        webEidAuthenticateAction = action
                    },
                    cancelWebEidAuthenticateAction = { action ->
                        cancelWebEidAuthenticateAction = action
                    },
                    isValidToSign = {},
                    isValidToDecrypt = {},
                    isAuthenticated = { _, _ -> },
                    webEidViewModel = viewModel,
                )
            } else {
                Text(noAuthLabel)
            }

            if (!isWebEidAuthenticating && nfcSupported) {
                Button(
                    onClick = {
                        isWebEidAuthenticating = true
                        webEidAuthenticateAction()
                    },
                    enabled = isValidToWebEidAuthenticate,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.web_eid_authenticate),
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    isWebEidAuthenticating = false
                    activity.finishAndRemoveTask()
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.web_eid_ignore),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WebEidScreenPreview() {
    RIADigiDocTheme {
        WebEidScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
        )
    }
}
