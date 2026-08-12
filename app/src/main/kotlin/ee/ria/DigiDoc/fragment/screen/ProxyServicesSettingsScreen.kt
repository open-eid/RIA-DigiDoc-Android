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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ProxyServicesSettingsScreen(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val hostFocusRequester = remember { FocusRequester() }
    val portFocusRequester = remember { FocusRequester() }
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val setProxySetting = sharedSettingsViewModel.dataStore::setProxySetting
    val setProxyHost = sharedSettingsViewModel.dataStore::setProxyHost
    val setProxyPort = sharedSettingsViewModel.dataStore::setProxyPort
    val setProxyUsername = sharedSettingsViewModel.dataStore::setProxyUsername
    val setProxyPassword = sharedSettingsViewModel.dataStore::setProxyPassword

    val getProxySetting = sharedSettingsViewModel.dataStore::getProxySetting
    val getProxyHost = sharedSettingsViewModel.dataStore::getProxyHost
    val getProxyPort = sharedSettingsViewModel.dataStore::getProxyPort
    val getProxyUsername = sharedSettingsViewModel.dataStore::getProxyUsername
    val getProxyPassword = sharedSettingsViewModel.dataStore::getProxyPassword

    val settingsProxyChoice = remember { mutableStateOf(getProxySetting().name) }

    var proxyHost by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = getProxyHost(),
                selection = TextRange(getProxyHost().length),
            ),
        )
    }

    var proxyPort by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = getProxyPort().toString(),
                selection = TextRange(getProxyPort().toString().length),
            ),
        )
    }

    var proxyUsername by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = getProxyUsername(),
                selection = TextRange(getProxyUsername().length),
            ),
        )
    }

    var proxyPassword by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = getProxyPassword(),
                selection = TextRange(getProxyPassword().length),
            ),
        )
    }

    val isValidPortNumber = sharedSettingsViewModel.dataStore::isValidPortNumber

    val proxyPortErrorText =
        if (proxyPort.text.isNotEmpty()) {
            if (!isValidPortNumber(proxyPort.text)) {
                stringResource(id = R.string.main_settings_proxy_port_error)
            } else {
                ""
            }
        } else {
            ""
        }

    val noProxyText = stringResource(R.string.main_settings_proxy_no_proxy)
    val systemProxyText = stringResource(R.string.main_settings_proxy_use_system)
    val manualProxyText = stringResource(R.string.main_settings_proxy_manual)
    val proxyCheckConnectionText = stringResource(R.string.main_settings_proxy_check_connection)
    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    LaunchedEffect(sharedSettingsViewModel.errorState) {
        sharedSettingsViewModel.errorState.collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    val type =
                        if (it == R.string.main_settings_proxy_check_connection_success) {
                            SnackbarType.SUCCESS
                        } else {
                            SnackbarType.ERROR
                        }
                    showMessage(context, it, type)
                    sharedSettingsViewModel.resetErrorState()
                }
            }
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("proxyServicesSettingsScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_proxy_title,
                onLeftButtonClick = {
                    navController.navigateUp()
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
            isThirdButtonVisible = false,
        )
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState()),
        ) {
            Card(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(top = XSPadding, bottom = SPadding),
                shape = buttonRoundedCornerShape,
                border =
                    BorderStroke(
                        width = XSBorder,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .clickable {
                                settingsProxyChoice.value = ProxySetting.NO_PROXY.name
                                setProxySetting(ProxySetting.NO_PROXY)
                                sharedSettingsViewModel.saveProxySettings()
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = noProxyText,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = noProxyText
                                },
                        selected = settingsProxyChoice.value == ProxySetting.NO_PROXY.name,
                        onClick = {
                            settingsProxyChoice.value = ProxySetting.NO_PROXY.name
                            setProxySetting(ProxySetting.NO_PROXY)
                            sharedSettingsViewModel.saveProxySettings()
                        },
                    )
                }
            }

            Card(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(top = XSPadding, bottom = SPadding),
                shape = buttonRoundedCornerShape,
                border =
                    BorderStroke(
                        width = XSBorder,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .clickable {
                                settingsProxyChoice.value = ProxySetting.SYSTEM_PROXY.name
                                setProxySetting(ProxySetting.SYSTEM_PROXY)
                                sharedSettingsViewModel.saveProxySettings()
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = systemProxyText,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = systemProxyText
                                },
                        selected = settingsProxyChoice.value == ProxySetting.SYSTEM_PROXY.name,
                        onClick = {
                            settingsProxyChoice.value = ProxySetting.SYSTEM_PROXY.name
                            setProxySetting(ProxySetting.SYSTEM_PROXY)
                            sharedSettingsViewModel.saveProxySettings()
                        },
                    )
                }
            }

            Card(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(top = XSPadding, bottom = SPadding),
                shape = buttonRoundedCornerShape,
                border =
                    BorderStroke(
                        width = XSBorder,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Row(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .clickable {
                                val proxyPortValue = proxyPort.text.toIntOrNull() ?: 80
                                settingsProxyChoice.value = ProxySetting.MANUAL_PROXY.name
                                setProxySetting(ProxySetting.MANUAL_PROXY)
                                sharedSettingsViewModel.saveProxySettings(
                                    ManualProxy(
                                        host = proxyHost.text,
                                        port = proxyPortValue,
                                        username = proxyUsername.text,
                                        password = proxyPassword.text,
                                    ),
                                )
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = manualProxyText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = manualProxyText
                                },
                        selected = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                        onClick = {
                            val proxyPortValue = proxyPort.text.toIntOrNull() ?: 80
                            settingsProxyChoice.value = ProxySetting.MANUAL_PROXY.name
                            setProxySetting(ProxySetting.MANUAL_PROXY)
                            sharedSettingsViewModel.saveProxySettings(
                                ManualProxy(
                                    host = proxyHost.text,
                                    port = proxyPortValue,
                                    username = proxyUsername.text,
                                    password = proxyPassword.text,
                                ),
                            )
                        },
                    )
                }

                if (settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name) {
                    Column(
                        modifier =
                            modifier
                                .padding(horizontal = SPadding)
                                .padding(bottom = LPadding),
                    ) {
                        PrimaryTextField(
                            modifier =
                                Modifier
                                    .focusRequester(hostFocusRequester)
                                    .padding(vertical = XSPadding),
                            value = proxyHost,
                            onValueChange = {
                                proxyHost = it
                                setProxyHost(it.text)
                            },
                            singleLine = true,
                            label = stringResource(R.string.main_settings_proxy_host),
                            enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Uri,
                                ),
                            onDone = {
                                portFocusRequester.requestFocus()
                            },
                            testTag = "proxyServicesHostTextField",
                            removeIconTestTag = "proxyServicesHostRemoveIconButton",
                        )

                        PrimaryTextField(
                            modifier =
                                Modifier
                                    .focusRequester(portFocusRequester)
                                    .padding(vertical = XSPadding),
                            value = proxyPort,
                            onValueChange = {
                                proxyPort = it
                                if (isValidPortNumber(it.text)) {
                                    setProxyPort(it.text.toInt())
                                }
                            },
                            singleLine = true,
                            label = stringResource(R.string.main_settings_proxy_port),
                            enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Number,
                                ),
                            onDone = {
                                usernameFocusRequester.requestFocus()
                            },
                            isError = proxyPortErrorText.isNotEmpty(),
                            errorText = proxyPortErrorText,
                            testTag = "proxyServicesPortTextField",
                            removeIconTestTag = "proxyServicesPortRemoveIconButton",
                        )

                        PrimaryTextField(
                            modifier =
                                Modifier
                                    .focusRequester(usernameFocusRequester)
                                    .padding(vertical = XSPadding),
                            value = proxyUsername,
                            onValueChange = {
                                proxyUsername = it
                                setProxyUsername(it.text)
                            },
                            singleLine = true,
                            label = stringResource(R.string.main_settings_proxy_username),
                            enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                            keyboardOptions =
                                KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                    keyboardType = KeyboardType.Text,
                                ),
                            onDone = {
                                passwordFocusRequester.requestFocus()
                            },
                            testTag = "proxyServicesUsernameTextField",
                            removeIconTestTag = "proxyServicesUsernameRemoveIconButton",
                        )

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PrimaryTextField(
                                modifier =
                                    Modifier
                                        .focusRequester(passwordFocusRequester)
                                        .weight(1f)
                                        .padding(vertical = XSPadding),
                                value = proxyPassword,
                                onValueChange = {
                                    proxyPassword = it
                                    setProxyPassword(it.text)
                                },
                                singleLine = true,
                                label = stringResource(R.string.main_settings_proxy_password),
                                enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                                isPasswordText = !passwordVisible,
                                trailingIcon = {
                                    val image =
                                        if (passwordVisible) {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                        } else {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                        }
                                    val description =
                                        if (passwordVisible) {
                                            stringResource(
                                                id = R.string.hide_password,
                                            )
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    IconButton(
                                        modifier =
                                            modifier
                                                .semantics { traversalIndex = 9f }
                                                .testTag("proxyServicesPasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Password,
                                    ),
                                testTag = "proxyServicesPasswordTextField",
                            )

                            if (isTalkBackEnabled(context) && proxyPassword.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    proxyPassword = TextFieldValue("")
                                    scope.launch(Main) {
                                        passwordFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        passwordFocusRequester.requestFocus()
                                    }
                                }) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }.testTag("proxyServicesPasswordRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = SPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    val proxyPort = proxyPort.text.toIntOrNull() ?: 80
                    sharedSettingsViewModel.checkConnection(
                        ManualProxy(
                            host = proxyHost.text,
                            port = proxyPort,
                            username = proxyUsername.text,
                            password = proxyPassword.text,
                        ),
                    )
                }) {
                    Text(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription =
                                        "${proxyCheckConnectionText.lowercase()} $buttonName"
                                    testTagsAsResourceId = true
                                }.testTag("proxyServicesCheckInternetConnectionButton"),
                        text = proxyCheckConnectionText,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}
