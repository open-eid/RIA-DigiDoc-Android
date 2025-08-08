@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

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
    val authPayload = viewModel.authPayload.collectAsState().value
    var isWebEidAuthenticating by rememberSaveable { mutableStateOf(false) }
    var webEidAuthenticateAction by remember { mutableStateOf<() -> Unit>({}) }
    var cancelWebEidAuthenticateAction by remember { mutableStateOf<() -> Unit>({}) }
    var isValidToWebEidAuthenticate by remember { mutableStateOf(false) }
    var nfcSupported by remember { mutableStateOf(false) }

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()
    val messages by SnackBarManager.messages.collectAsState(emptyList())

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
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
                leftIconContentDescription =
                    if (isWebEidAuthenticating) {
                        R.string.signing_cancel
                    } else {
                        R.string.back
                    },
                onLeftButtonClick = {
                    if (isWebEidAuthenticating) {
                        cancelWebEidAuthenticateAction()
                        isWebEidAuthenticating = false
                    } else {
                        navController.navigateUp()
                    }
                },
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
            if (authPayload != null) {
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
                ) {
                    Text(
                        text = stringResource(R.string.web_eid_authenticate),
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
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
