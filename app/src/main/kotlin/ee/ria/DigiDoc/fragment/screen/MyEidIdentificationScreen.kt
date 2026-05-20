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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.IdentityAction
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMyEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidIdentificationScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedMyEidViewModel: SharedMyEidViewModel,
) {
    val context = LocalActivity.current as Activity
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isAuthenticating by rememberSaveable { mutableStateOf(false) }
    var isValidToAuthenticate by remember { mutableStateOf(false) }
    var cancelAction by remember { mutableStateOf<() -> Unit>({}) }
    var nfcSupported by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val identificationMethodText = stringResource(R.string.myeid_identification_method)
    val chosenMethodNameText = stringResource(R.string.signature_update_signature_add_method_nfc)
    val rememberMeText = stringResource(R.string.signature_update_remember_me)

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
                    if (isAuthenticating) {
                        R.string.signing_cancel
                    } else {
                        R.string.back
                    },
                onLeftButtonClick = {
                    if (isAuthenticating) {
                        cancelAction()
                        isAuthenticating = false
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
                modifier =
                    modifier
                        .semantics {
                            heading()
                            testTagsAsResourceId = true
                        }.testTag("myEidIdentificationTitle"),
                text = stringResource(R.string.myeid_identification_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )

            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = XSPadding)
                        .alpha(if (!isAuthenticating) 1f else 0.001f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(XSPadding),
            ) {
                Text(
                    text = identificationMethodText,
                    modifier =
                        modifier
                            .focusable(false)
                            .notAccessible()
                            .testTag("identificationMethodTitle"),
                    color = MaterialTheme.colorScheme.onSecondary,
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.labelLarge,
                )

                Text(
                    modifier =
                        modifier
                            .semantics {
                                contentDescription = "$identificationMethodText $chosenMethodNameText"
                                testTagsAsResourceId = true
                            }.testTag("myEidChosenMethodNameTitle"),
                    text = chosenMethodNameText,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }

            NFCView(
                modifier = modifier,
                activity = context,
                onError = {
                    isAuthenticating = false
                    cancelAction()
                },
                onSuccess = {
                    isAuthenticating = false
                    navController.navigateUp()
                },
                isAuthenticating = isAuthenticating,
                rememberMe = rememberMe,
                sharedSettingsViewModel = sharedSettingsViewModel,
                sharedContainerViewModel = sharedContainerViewModel,
                showPinField = false,
                isSupported = { supported ->
                    nfcSupported = supported
                },
                cancelAction = { action ->
                    cancelAction = action
                },
                isAuthenticated = { authenticated, idCardData ->
                    if (authenticated) {
                        sharedMyEidViewModel.setIdCardData(idCardData)

                        navController.navigate(
                            Route.MyEidScreen.route,
                        )
                    }
                },
                isValidToAuthenticate = { isValid ->
                    isValidToAuthenticate = isValid
                },
                identityAction = IdentityAction.AUTH,
            )

            if (!isAuthenticating && nfcSupported) {
                SettingsSwitchItem(
                    modifier = modifier,
                    checked = rememberMe,
                    onCheckedChange = {
                        rememberMe = it
                    },
                    title = rememberMeText,
                    contentDescription = rememberMeText,
                    testTag = "myEidRememberMeSwitch",
                )

                if (rememberMe) {
                    Text(
                        text = stringResource(R.string.signature_update_remember_me_message),
                    )
                }

                Spacer(modifier = modifier.height(SPadding))

                Button(
                    onClick = {
                        isAuthenticating = true
                    },
                    enabled = isValidToAuthenticate,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .focusable(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text = stringResource(R.string.myeid_identify_button),
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyEidIdentificationScreenPreview() {
    RIADigiDocTheme {
        MyEidIdentificationScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
            sharedMyEidViewModel = hiltViewModel(),
        )
    }
}
