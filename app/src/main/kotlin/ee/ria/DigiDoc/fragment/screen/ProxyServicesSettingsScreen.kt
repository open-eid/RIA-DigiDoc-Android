@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
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

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

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

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    LaunchedEffect(sharedSettingsViewModel.errorState) {
        sharedSettingsViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    showMessage(context, errorState)
                }
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
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("proxyServicesSettingsScreen"),
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
                                settingsProxyChoice.value = ProxySetting.MANUAL_PROXY.name
                                setProxySetting(ProxySetting.MANUAL_PROXY)
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
                            settingsProxyChoice.value = ProxySetting.MANUAL_PROXY.name
                            setProxySetting(ProxySetting.MANUAL_PROXY)
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
                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                                value = proxyHost,
                                singleLine = true,
                                onValueChange = {
                                    proxyHost = it.copy(selection = TextRange(it.text.length))
                                    setProxyHost(it.text)
                                },
                                shape = RectangleShape,
                                label = { Text(stringResource(R.string.main_settings_proxy_host)) },
                                modifier =
                                    modifier
                                        .focusRequester(hostFocusRequester)
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("proxyServicesHostTextField"),
                                trailingIcon = {
                                    if (!isTalkBackEnabled(context) && proxyHost.text.isNotEmpty()) {
                                        IconButton(onClick = {
                                            proxyHost = TextFieldValue("")
                                        }) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                                contentDescription = "$clearButtonText $buttonName",
                                            )
                                        }
                                    }
                                },
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Uri,
                                    ),
                            )

                            if (isTalkBackEnabled(context) && proxyHost.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    proxyHost = TextFieldValue("")
                                    scope.launch(Main) {
                                        hostFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        hostFocusRequester.requestFocus()
                                    }
                                }) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }.testTag("proxyServicesHostRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        Spacer(modifier = modifier.height(XSPadding))

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                                value = proxyPort,
                                singleLine = true,
                                onValueChange = {
                                    proxyPort = it.copy(selection = TextRange(it.text.length))
                                    if (isValidPortNumber(it.text)) {
                                        setProxyPort(it.text.toInt())
                                    }
                                },
                                shape = RectangleShape,
                                label = { Text(stringResource(R.string.main_settings_proxy_port)) },
                                modifier =
                                    modifier
                                        .focusRequester(portFocusRequester)
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("proxyServicesPortTextField"),
                                trailingIcon = {
                                    if (!isTalkBackEnabled(context) && proxyPort.text.isNotEmpty()) {
                                        IconButton(onClick = {
                                            proxyPort = TextFieldValue("")
                                        }) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                                contentDescription = "$clearButtonText $buttonName",
                                            )
                                        }
                                    }
                                },
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Number,
                                    ),
                            )

                            if (isTalkBackEnabled(context) && proxyPort.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    proxyPort = TextFieldValue("")
                                    scope.launch(Main) {
                                        portFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        portFocusRequester.requestFocus()
                                    }
                                }) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }.testTag("proxyServicesPortRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }
                        if (proxyPortErrorText.isNotEmpty()) {
                            Text(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .focusable(enabled = true)
                                        .semantics { contentDescription = proxyPortErrorText }
                                        .testTag("proxyServicesPortErrorText"),
                                text = proxyPortErrorText,
                                color = MaterialTheme.colorScheme.errorContainer,
                            )
                        }

                        Spacer(modifier = modifier.height(XSPadding))

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                                value = proxyUsername,
                                singleLine = true,
                                onValueChange = {
                                    proxyUsername = it.copy(selection = TextRange(it.text.length))
                                    setProxyUsername(it.text)
                                },
                                shape = RectangleShape,
                                label = { Text(stringResource(R.string.main_settings_proxy_username)) },
                                modifier =
                                    modifier
                                        .focusRequester(usernameFocusRequester)
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("proxyServicesUsernameTextField"),
                                trailingIcon = {
                                    if (!isTalkBackEnabled(context) && proxyUsername.text.isNotEmpty()) {
                                        IconButton(onClick = {
                                            proxyUsername = TextFieldValue("")
                                        }) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                                contentDescription = "$clearButtonText $buttonName",
                                            )
                                        }
                                    }
                                },
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Text,
                                    ),
                            )

                            if (isTalkBackEnabled(context) && proxyUsername.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    proxyUsername = TextFieldValue("")
                                    scope.launch(Main) {
                                        usernameFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        usernameFocusRequester.requestFocus()
                                    }
                                }) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }.testTag("proxyServicesUsernameRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        Spacer(modifier = modifier.height(XSPadding))

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                enabled = settingsProxyChoice.value == ProxySetting.MANUAL_PROXY.name,
                                value = proxyPassword,
                                singleLine = true,
                                onValueChange = {
                                    proxyPassword = it.copy(selection = TextRange(it.text.length))
                                    setProxyPassword(it.text)
                                },
                                shape = RectangleShape,
                                label = { Text(stringResource(R.string.main_settings_proxy_password)) },
                                modifier =
                                    modifier
                                        .focusRequester(passwordFocusRequester)
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("proxyServicesPasswordTextField"),
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
                                                .testTag("mainSettingsProxyPasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                textStyle = MaterialTheme.typography.titleSmall,
                                visualTransformation =
                                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Password,
                                    ),
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
                    sharedSettingsViewModel.checkConnection(
                        ManualProxy(
                            host = proxyHost.text,
                            port = proxyPort.text.toInt(),
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
                                }.testTag("mainSettingsProxyServicesCheckInternetConnectionButton"),
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
