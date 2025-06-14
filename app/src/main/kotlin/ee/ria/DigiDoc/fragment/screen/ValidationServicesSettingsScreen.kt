@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.settings.TSASetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ValidationServicesSettingsScreen(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val configuration = sharedSettingsViewModel.updatedConfiguration.value

    val getSettingsSivaUrl = sharedSettingsViewModel.dataStore::getSettingsSivaUrl
    val getSivaSetting = sharedSettingsViewModel.dataStore::getSivaSetting
    val setSettingsSivaUrl = sharedSettingsViewModel.dataStore::setSettingsSivaUrl
    val setSivaSetting = sharedSettingsViewModel.dataStore::setSivaSetting
    val defaultSivaServiceUrl = configuration?.sivaUrl ?: getSettingsSivaUrl()
    var settingsSivaServiceChoice = remember { mutableStateOf(getSivaSetting().name) }
    var settingsSivaServiceUrl by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = defaultSivaServiceUrl,
                selection = TextRange(defaultSivaServiceUrl.length),
            ),
        )
    }
    sharedSettingsViewModel.updateSivaData(settingsSivaServiceUrl.text, context)
    val issuedTo by sharedSettingsViewModel.sivaIssuedTo.asFlow().collectAsState(
        "",
    )
    val validTo by sharedSettingsViewModel.sivaValidTo.asFlow().collectAsState(
        "",
    )

    val sivaCertificate by sharedSettingsViewModel.sivaCertificate.asFlow().collectAsState(
        null,
    )

    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri == null) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                CoroutineScope(Dispatchers.IO).launch {
                    sharedSettingsViewModel.handleSivaFile(uri)
                    withContext(Main) {
                        sharedSettingsViewModel.updateSivaData(settingsSivaServiceUrl.text, context)
                    }
                }
            },
        )

    var urlText by remember { mutableStateOf(defaultSivaServiceUrl) }

    val issuedToTitleText = stringResource(R.string.main_settings_timestamp_cert_issued_to_title)
    val validToTitleText = stringResource(R.string.main_settings_timestamp_cert_valid_to_title)
    val showCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_show_certificate_button)
    val addCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_add_certificate_button)
    val noCertificateFoundText = stringResource(R.string.main_settings_timestamp_cert_no_certificate_found)

    val useDefaultAccessText = stringResource(R.string.main_settings_siva_default_access_title)
    val useManualAccessText = stringResource(R.string.main_settings_siva_default_manual_access_title)

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

    // Reset SiVa URL when the user navigates away from this screen and has set default choice
    DisposableEffect(Unit) {
        onDispose {
            if (settingsSivaServiceChoice.value == SivaSetting.DEFAULT.name) {
                setSettingsSivaUrl(configuration?.sivaUrl ?: "")
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
                }
                .testTag("validationServicesSettingsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_validation_services_title,
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
            Text(
                text = stringResource(R.string.main_settings_siva_service_title),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .padding(bottom = SPadding)
                        .semantics {
                            heading()
                        },
            )

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
                                settingsSivaServiceChoice.value = SivaSetting.DEFAULT.name
                                setSivaSetting(SivaSetting.DEFAULT)
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = useDefaultAccessText,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = useDefaultAccessText
                                },
                        selected = settingsSivaServiceChoice.value == SivaSetting.DEFAULT.name,
                        onClick = {
                            settingsSivaServiceChoice.value = SivaSetting.DEFAULT.name
                            setSivaSetting(SivaSetting.DEFAULT)
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
                Column(
                    modifier =
                        modifier
                            .padding(SPadding)
                            .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier =
                            modifier
                                .clickable {
                                    settingsSivaServiceChoice.value = SivaSetting.MANUAL.name
                                    setSivaSetting(SivaSetting.MANUAL)
                                },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = useManualAccessText,
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
                                        contentDescription = useManualAccessText
                                    },
                            selected = settingsSivaServiceChoice.value == SivaSetting.MANUAL.name,
                            onClick = {
                                settingsSivaServiceChoice.value = SivaSetting.MANUAL.name
                                setSivaSetting(SivaSetting.MANUAL)
                            },
                        )
                    }

                    if (settingsSivaServiceChoice.value == SivaSetting.MANUAL.name) {
                        Spacer(modifier = modifier.height(LPadding))

                        Row(
                            modifier =
                                modifier
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                enabled = settingsSivaServiceChoice.value == SivaSetting.MANUAL.name,
                                value = settingsSivaServiceUrl,
                                singleLine = true,
                                onValueChange = {
                                    settingsSivaServiceUrl = it
                                    setSettingsSivaUrl(it.text)
                                },
                                shape = RectangleShape,
                                label = { Text(stringResource(R.string.main_settings_siva_service_url)) },
                                modifier =
                                    modifier
                                        .focusRequester(focusRequester)
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("validationServicesComponentTextField"),
                                trailingIcon = {
                                    if (!isTalkBackEnabled(context) && settingsSivaServiceUrl.text.isNotEmpty()) {
                                        IconButton(onClick = {
                                            settingsSivaServiceUrl = TextFieldValue("")
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
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Uri,
                                    ),
                            )

                            if (isTalkBackEnabled(context) && settingsSivaServiceUrl.text.isNotEmpty()) {
                                IconButton(onClick = {
                                    settingsSivaServiceUrl = TextFieldValue("")
                                    scope.launch(Main) {
                                        focusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        focusRequester.requestFocus()
                                    }
                                }) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("validationServicesRemoveIconButton"),
                                        imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                        contentDescription = "$clearButtonText $buttonName",
                                    )
                                }
                            }
                        }

                        Spacer(modifier = modifier.height(SPadding))

                        Text(
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        heading()
                                    },
                            text = stringResource(R.string.main_settings_siva_certificate_title),
                            style = MaterialTheme.typography.bodyLarge,
                        )

                        if (sivaCertificate != null) {
                            Text(
                                modifier = modifier.fillMaxWidth(),
                                text = "$issuedToTitleText $issuedTo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                modifier = modifier.fillMaxWidth(),
                                text = "$validToTitleText $validTo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            Text(
                                modifier = modifier.fillMaxWidth(),
                                text = noCertificateFoundText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = modifier.height(SPadding))

                        FlowRow(
                            modifier = modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (sivaCertificate != null) {
                                TextButton(onClick = {
                                    sivaCertificate?.let {
                                        sharedCertificateViewModel.setCertificate(
                                            it,
                                        )
                                        navController.navigate(
                                            Route.CertificateDetail.route,
                                        )
                                    }
                                }) {
                                    Text(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    contentDescription =
                                                        "$showCertificateButtonText $buttonName"
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("validationServicesShowCertificateActionButton"),
                                        text = showCertificateButtonText,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            TextButton(onClick = {
                                filePicker.launch("*/*")
                            }) {
                                Text(
                                    modifier =
                                        modifier
                                            .semantics {
                                                contentDescription =
                                                    "$addCertificateButtonText $buttonName"
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("validationServicesAddCertificateActionButton"),
                                    text = addCertificateButtonText,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}
