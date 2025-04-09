@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.dialog.OptionChooserDialog
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun EncryptionServicesSettingsScreen(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val configuration = sharedSettingsViewModel.updatedConfiguration.value

    val getCdocSetting = sharedSettingsViewModel.dataStore::getCdocSetting
    val setCdocSetting = sharedSettingsViewModel.dataStore::setCdocSetting

    val useKeyTransfer = rememberSaveable { mutableStateOf(false) }
    val useDefaultKeyTransferServer = rememberSaveable { mutableStateOf(true) }

    var settingsCdocServiceChoice = remember { mutableStateOf(getCdocSetting().name) }
    var settingsCdocNameChoice = remember { mutableIntStateOf(R.string.option_ria) }

    val manualKeyTransferText = stringResource(R.string.option_manual_key_transfer)

    var uuidText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            ),
        )
    }

    var fetchUrlText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            ),
        )
    }

    var postUrlText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = "",
                selection = TextRange.Zero,
            ),
        )
    }

    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri == null) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                CoroutineScope(Dispatchers.IO).launch {
                    // TODO: Handle certificate
                    withContext(Main) {
                        // TODO: Update CDOC data
                    }
                }
            },
        )

    val issuedToTitleText = stringResource(R.string.main_settings_timestamp_cert_issued_to_title)
    val validToTitleText = stringResource(R.string.main_settings_timestamp_cert_valid_to_title)
    val showCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_show_certificate_button)
    val addCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_add_certificate_button)
    val noCertificateFoundText = stringResource(R.string.main_settings_timestamp_cert_no_certificate_found)

    val optionManualKeyTransfer = stringResource(R.string.option_manual_key_transfer)
    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    var openOptionChooserDialog by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val interactionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

    val nameChoices =
        listOf(
            stringResource(R.string.option_ria),
            stringResource(R.string.option_manual_key_transfer),
        )

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
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("encryptionServicesScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_crypto_services_title,
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
                    .padding(top = SPadding)
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
                                settingsCdocServiceChoice.value = CDOCSetting.CDOC1.name
                                setCdocSetting(CDOCSetting.CDOC1)
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Use CDOC1 file format for encryption",
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = "Use CDOC1 file format for encryption"
                                },
                        selected = settingsCdocServiceChoice.value == CDOCSetting.CDOC1.name,
                        onClick = {
                            settingsCdocServiceChoice.value = CDOCSetting.CDOC1.name
                            setCdocSetting(CDOCSetting.CDOC1)
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
                                    settingsCdocServiceChoice.value = CDOCSetting.CDOC2.name
                                    setCdocSetting(CDOCSetting.CDOC2)
                                },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Use CDOC2 file format for encryption",
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
                                        contentDescription = "Use CDOC2 file format for encryption"
                                    },
                            selected = settingsCdocServiceChoice.value == CDOCSetting.CDOC2.name,
                            onClick = {
                                settingsCdocServiceChoice.value = CDOCSetting.CDOC2.name
                                setCdocSetting(CDOCSetting.CDOC2)
                            },
                        )
                    }

                    if (settingsCdocServiceChoice.value == CDOCSetting.CDOC2.name) {
                        Spacer(modifier = modifier.height(LPadding))

                        SettingsSwitchItem(
                            modifier = modifier,
                            checked = useKeyTransfer.value,
                            onCheckedChange = {
                                useKeyTransfer.value = it
                            },
                            title = manualKeyTransferText,
                            contentDescription = manualKeyTransferText,
                            testTag = "encryptionServicesManuallySpecifiedKeySwitch",
                        )

                        if (useKeyTransfer.value) {
                            Box(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = "Name"
                                        },
                            ) {
                                OutlinedTextField(
                                    label = {
                                        Text("Name")
                                    },
                                    value = stringResource(settingsCdocNameChoice.intValue),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    modifier =
                                        modifier
                                            .fillMaxWidth()
                                            .focusRequester(focusRequester)
                                            .onGloballyPositioned { coordinates ->
                                                textFieldSize = coordinates.size.toSize()
                                            },
                                    trailingIcon = {
                                        Icon(
                                            imageVector =
                                                ImageVector.vectorResource(
                                                    R.drawable.ic_baseline_keyboard_arrow_down_24,
                                                ),
                                            contentDescription = "Name",
                                            modifier =
                                                modifier.clickable {
                                                    openOptionChooserDialog = !openOptionChooserDialog
                                                },
                                        )
                                    },
                                )

                                if (!openOptionChooserDialog) {
                                    Box(
                                        modifier =
                                            modifier
                                                .matchParentSize()
                                                .clickable(
                                                    onClick = {
                                                        openOptionChooserDialog = true
                                                    },
                                                    interactionSource = interactionSource,
                                                    indication = null,
                                                )
                                                .semantics {
                                                    contentDescription = "Name"
                                                },
                                    )
                                } else {
                                    BasicAlertDialog(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                },
                                        onDismissRequest = {
                                            openOptionChooserDialog = false
                                        },
                                    ) {
                                        Surface(
                                            modifier =
                                                modifier
                                                    .wrapContentHeight()
                                                    .wrapContentWidth()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(XSPadding)
                                                    .testTag("encryptionServicesNameDialog"),
                                        ) {
                                            OptionChooserDialog(
                                                modifier = modifier,
                                                title = R.string.choose_name_option,
                                                choices = nameChoices,
                                                selectedChoice = settingsCdocNameChoice.intValue,
                                                cancelButtonClick = {
                                                    openOptionChooserDialog = false
                                                },
                                                okButtonClick = { selectedResId ->
                                                    settingsCdocNameChoice.intValue = selectedResId
                                                    useDefaultKeyTransferServer.value =
                                                        selectedResId == R.string.option_ria
                                                    openOptionChooserDialog = false
                                                },
                                            )
                                            InvisibleElement(modifier = modifier)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = modifier.padding(MSPadding))

                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    enabled =
                                        settingsCdocServiceChoice.value == CDOCSetting.CDOC2.name &&
                                            useKeyTransfer.value &&
                                            !useDefaultKeyTransferServer.value,
                                    value = uuidText,
                                    singleLine = true,
                                    onValueChange = {
                                        uuidText = it.copy(selection = TextRange(it.text.length))
                                    },
                                    shape = RectangleShape,
                                    label = { Text("UUID") },
                                    modifier =
                                        modifier
                                            .focusRequester(focusRequester)
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("encryptionServicesUuidTextField"),
                                    trailingIcon = {
                                        if (!isTalkBackEnabled(context) && uuidText.text.isNotEmpty()) {
                                            IconButton(onClick = {
                                                uuidText = TextFieldValue("")
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

                                if (isTalkBackEnabled(context) && uuidText.text.isNotEmpty()) {
                                    IconButton(onClick = { uuidText = TextFieldValue("") }) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("encryptionServicesUuidRemoveIconButton"),
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                            contentDescription = "$clearButtonText $buttonName",
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = modifier.height(MSPadding))

                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    enabled =
                                        settingsCdocServiceChoice.value == CDOCSetting.CDOC2.name &&
                                            useKeyTransfer.value &&
                                            !useDefaultKeyTransferServer.value,
                                    value = fetchUrlText,
                                    singleLine = true,
                                    onValueChange = {
                                        fetchUrlText =
                                            it.copy(selection = TextRange(it.text.length))
                                    },
                                    shape = RectangleShape,
                                    label = { Text("Fetch URL") },
                                    modifier =
                                        modifier
                                            .focusRequester(focusRequester)
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("encryptionServicesFetchUrlTextField"),
                                    trailingIcon = {
                                        if (!isTalkBackEnabled(context) && fetchUrlText.text.isNotEmpty()) {
                                            IconButton(onClick = {
                                                fetchUrlText = TextFieldValue("")
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

                                if (isTalkBackEnabled(context) && fetchUrlText.text.isNotEmpty()) {
                                    IconButton(onClick = { fetchUrlText = TextFieldValue("") }) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("encryptionServicesFetchUrlRemoveIconButton"),
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                            contentDescription = "$clearButtonText $buttonName",
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = modifier.height(MSPadding))

                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                OutlinedTextField(
                                    enabled =
                                        settingsCdocServiceChoice.value == CDOCSetting.CDOC2.name &&
                                            useKeyTransfer.value &&
                                            !useDefaultKeyTransferServer.value,
                                    value = postUrlText,
                                    singleLine = true,
                                    onValueChange = {
                                        postUrlText = it.copy(selection = TextRange(it.text.length))
                                    },
                                    shape = RectangleShape,
                                    label = { Text("Post URL") },
                                    modifier =
                                        modifier
                                            .focusRequester(focusRequester)
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .semantics {
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("encryptionServicesPostUrlTextField"),
                                    trailingIcon = {
                                        if (!isTalkBackEnabled(context) && postUrlText.text.isNotEmpty()) {
                                            IconButton(onClick = {
                                                postUrlText = TextFieldValue("")
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

                                if (isTalkBackEnabled(context) && postUrlText.text.isNotEmpty()) {
                                    IconButton(onClick = { postUrlText = TextFieldValue("") }) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("encryptionServicesPostUrlRemoveIconButton"),
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
                                text = "Key transfer server SSL certificate",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Text(
                                modifier = modifier.fillMaxWidth(),
                                text = "$issuedToTitleText placeholder",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                modifier = modifier.fillMaxWidth(),
                                text = "$validToTitleText placeholder",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(modifier = modifier.height(SPadding))

                            FlowRow(
                                modifier = modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                TextButton(onClick = {}) {
                                    Text(
                                        modifier =
                                            modifier
                                                .semantics {
                                                    contentDescription =
                                                        "$showCertificateButtonText $buttonName"
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("encryptionServicesShowCertificateActionButton"),
                                        text = showCertificateButtonText,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
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
                                                .testTag("encryptionServicesAddCertificateActionButton"),
                                        text = addCertificateButtonText,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}
