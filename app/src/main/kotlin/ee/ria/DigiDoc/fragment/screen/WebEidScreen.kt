/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthRequest
import kotlinx.coroutines.launch

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

    val certificateRequest = viewModel.certificateRequest.collectAsState().value
    val isCertificateFlow = certificateRequest != null
    val signRequest = viewModel.signRequest.collectAsState().value
    var webEidSignAction by remember { mutableStateOf<() -> Unit>({}) }
    var cancelWebEidSignAction by remember { mutableStateOf<() -> Unit>({}) }
    var nfcSupported by remember { mutableStateOf(false) }

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val messages by SnackBarManager.messages.collectAsState(emptyList())
    val dialogError by viewModel.dialogError.collectAsState()
    var rememberMe by rememberSaveable {
        mutableStateOf(sharedSettingsViewModel.dataStore.getWebEidRememberMe())
    }
    val hasStoredCanNumber =
        sharedSettingsViewModel.dataStore.getCanNumber().isNotEmpty() ||
            sharedSettingsViewModel.dataStore.getTemporaryCanNumber().isNotEmpty()

    LaunchedEffect(messages) {
        messages.forEach { message ->
            scope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    LaunchedEffect(authRequest, certificateRequest) {
        if (authRequest != null || certificateRequest != null) {
            if (!sharedSettingsViewModel.dataStore.isWebEidSessionActive()) {
                sharedSettingsViewModel.dataStore.clearTemporaryCanNumber()
            }
            sharedSettingsViewModel.dataStore.setWebEidSessionActive(true)
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

        if (dialogError != 0) {
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

        if (dialogError == 0) {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(SPadding)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(XSPadding),
            ) {
                val title =
                    when {
                        authRequest != null -> stringResource(R.string.web_eid_auth_title)
                        isCertificateFlow -> stringResource(R.string.web_eid_certificate_title)
                        signRequest != null -> stringResource(R.string.web_eid_sign_title)
                        else -> stringResource(R.string.web_eid_auth_title)
                    }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.semantics { heading() },
                )
                if (authRequest != null) {
                    if (!isWebEidAuthenticating) {
                        WebEidAuthInfo(authRequest = authRequest)
                    }

                    NFCView(
                        activity = activity,
                        identityAction = IdentityAction.AUTH,
                        rememberMe = rememberMe,
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

                    if (!isWebEidAuthenticating) {
                        WebEidRememberMe(
                            rememberMe = rememberMe,
                            onRememberMeChange = { isRememberMeEnabled ->
                                rememberMe = isRememberMeEnabled
                                sharedSettingsViewModel.dataStore.setWebEidRememberMe(isRememberMeEnabled)
                                if (!isRememberMeEnabled) {
                                    sharedSettingsViewModel.dataStore.setSigningCertificate("")
                                }
                            },
                        )
                    }
                } else if (isCertificateFlow || signRequest != null) {
                    if (!isWebEidAuthenticating) {
                        val origin =
                            when {
                                isCertificateFlow -> certificateRequest.origin
                                signRequest != null -> signRequest.origin
                                else -> ""
                            }
                        val signingPersonInfo =
                            signRequest?.personalData?.let {
                                "${it.givenNames} ${it.surname}, ${it.personalCode}"
                            }
                        WebEidSignOrCertificateInfo(
                            origin = origin,
                            isCertificateFlow = isCertificateFlow,
                            signingPersonInfo = signingPersonInfo,
                        )
                    }

                    if (isCertificateFlow) {
                        NFCView(
                            activity = activity,
                            identityAction = IdentityAction.CERTIFICATE,
                            rememberMe = rememberMe,
                            isCertificate = true,
                            showPinField = false,
                            isSigning = false,
                            isDecrypting = false,
                            isWebEidAuthenticating = isWebEidAuthenticating,
                            onError = {
                                isWebEidAuthenticating = false
                                cancelWebEidSignAction()
                            },
                            onSuccess = {
                                isWebEidAuthenticating = false
                                navController.navigateUp()
                            },
                            sharedSettingsViewModel = sharedSettingsViewModel,
                            sharedContainerViewModel = sharedContainerViewModel,
                            isSupported = { supported -> nfcSupported = supported },
                            isValidToWebEidAuthenticate = { isValid -> isValidToWebEidAuthenticate = isValid },
                            signWebEidAction = { action -> webEidSignAction = action },
                            cancelWebEidSignAction = { action -> cancelWebEidSignAction = action },
                            isValidToSign = {},
                            isValidToDecrypt = {},
                            isAuthenticated = { _, _ -> },
                            webEidViewModel = viewModel,
                        )

                        if (!isWebEidAuthenticating) {
                            WebEidRememberMe(
                                rememberMe = rememberMe,
                                onRememberMeChange = { isRememberMeEnabled ->
                                    rememberMe = isRememberMeEnabled
                                    sharedSettingsViewModel.dataStore.setWebEidRememberMe(isRememberMeEnabled)
                                    if (!isRememberMeEnabled) {
                                        sharedSettingsViewModel.dataStore.setSigningCertificate("")
                                    }
                                },
                            )
                        }
                    } else {
                        NFCView(
                            activity = activity,
                            identityAction = IdentityAction.SIGN,
                            rememberMe = rememberMe,
                            isCertificate = false,
                            isSigning = false,
                            isDecrypting = false,
                            isWebEidAuthenticating = isWebEidAuthenticating,
                            isCanNumberReadOnly = hasStoredCanNumber,
                            onError = {
                                isWebEidAuthenticating = false
                                cancelWebEidSignAction()
                            },
                            onSuccess = {
                                sharedSettingsViewModel.dataStore.clearTemporaryCanNumber()
                                sharedSettingsViewModel.dataStore.setWebEidSessionActive(false)
                                if (!rememberMe) sharedSettingsViewModel.dataStore.setSigningCertificate("")
                                isWebEidAuthenticating = false
                                navController.navigateUp()
                            },
                            sharedSettingsViewModel = sharedSettingsViewModel,
                            sharedContainerViewModel = sharedContainerViewModel,
                            isSupported = { supported -> nfcSupported = supported },
                            isValidToWebEidAuthenticate = { isValid ->
                                isValidToWebEidAuthenticate = isValid
                            },
                            signWebEidAction = { action -> webEidSignAction = action },
                            cancelWebEidSignAction = { action -> cancelWebEidSignAction = action },
                            isValidToSign = {},
                            isValidToDecrypt = {},
                            isAuthenticated = { _, _ -> },
                            webEidViewModel = viewModel,
                        )
                    }
                } else {
                    Text(noAuthLabel)
                }

                if (!isWebEidAuthenticating && nfcSupported) {
                    if (authRequest != null) {
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
                            Text(text = stringResource(R.string.web_eid_authenticate))
                        }
                    } else if (isCertificateFlow || signRequest != null) {
                        Button(
                            onClick = {
                                isWebEidAuthenticating = true
                                webEidSignAction()
                            },
                            enabled = isValidToWebEidAuthenticate,
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Text(
                                text =
                                    if (isCertificateFlow) {
                                        stringResource(R.string.web_eid_get_certificate)
                                    } else {
                                        stringResource(R.string.web_eid_sign)
                                    },
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        isWebEidAuthenticating = false
                        scope.launch {
                            viewModel.handleUserCancelled()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.web_eid_cancel),
                    )
                }
            }
        }
    }
}

@Composable
private fun WebEidAuthInfo(authRequest: WebEidAuthRequest) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.web_eid_auth_request_from),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = authRequest.origin.take(80),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Left,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.web_eid_details_forwarded),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = stringResource(R.string.web_eid_name_personal_identification_code),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.web_eid_auth_consent_text),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WebEidSignOrCertificateInfo(
    origin: String,
    isCertificateFlow: Boolean,
    signingPersonInfo: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text =
                if (isCertificateFlow) {
                    stringResource(R.string.web_eid_cert_request_from)
                } else {
                    stringResource(R.string.web_eid_sign_request_from)
                },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = origin.take(80),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Left,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text =
                if (isCertificateFlow) {
                    stringResource(R.string.web_eid_details_forwarded)
                } else {
                    stringResource(R.string.web_eid_details)
                },
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text =
                if (!isCertificateFlow && !signingPersonInfo.isNullOrBlank()) {
                    signingPersonInfo
                } else {
                    stringResource(R.string.web_eid_name_personal_identification_code)
                },
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Left,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text =
                if (isCertificateFlow) {
                    stringResource(R.string.web_eid_certificate_consent_text)
                } else {
                    stringResource(R.string.web_eid_signature_consent_text)
                },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Left,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WebEidRememberMe(
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
) {
    val rememberMeText = stringResource(R.string.signature_update_remember_me)

    SettingsSwitchItem(
        checked = rememberMe,
        onCheckedChange = onRememberMeChange,
        title = rememberMeText,
        contentDescription = rememberMeText,
        testTag = "webEidRememberMeSwitch",
    )

    if (rememberMe) {
        Text(
            text = stringResource(R.string.web_eid_remember_me_message),
        )
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
