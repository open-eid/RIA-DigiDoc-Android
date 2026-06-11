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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider.CDOC2Conf
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.dialog.OptionChooserDialog
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.EncryptionServicesViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun EncryptionServicesSettingsScreen(
    modifier: Modifier = Modifier,
    encryptionServicesViewModel: EncryptionServicesViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val configuration = sharedSettingsViewModel.updatedConfiguration.value

    val cdoc2Conf = configuration?.cdoc2Conf ?: emptyMap()

    val cdocSetting by encryptionServicesViewModel.cdocSetting.collectAsStateWithLifecycle()
    val useCDOC2SelectedService by encryptionServicesViewModel.useOnlineEncryption.collectAsStateWithLifecycle()
    val selectedCDOC2Service by encryptionServicesViewModel.selectedCDOC2Service.collectAsStateWithLifecycle()
    val cdoc2Uuid by encryptionServicesViewModel.cdoc2Uuid.collectAsStateWithLifecycle()
    val cdoc2FetchUrl by encryptionServicesViewModel.cdoc2FetchUrl.collectAsStateWithLifecycle()
    val cdoc2PostUrl by encryptionServicesViewModel.cdoc2PostUrl.collectAsStateWithLifecycle()

    val issuedTo by sharedSettingsViewModel.cryptoCertIssuedTo.collectAsState(null)
    val validTo by sharedSettingsViewModel.cryptoCertValidTo.collectAsState(null)
    val cryptoCertificate by sharedSettingsViewModel.cryptoCertificate.collectAsState(null)

    val useKeyTransfer = rememberSaveable { mutableStateOf(useCDOC2SelectedService) }
    val useDefaultKeyTransferServer =
        rememberSaveable {
            mutableStateOf(
                cdoc2Conf.values.any { it.uuid.toString() == selectedCDOC2Service },
            )
        }

    val keyTransferText = stringResource(R.string.option_key_transfer)
    val manualKeyTransferText = stringResource(R.string.option_manual_key_transfer)

    val customDefaultCDOC2UUID: UUID =
        UUID.fromString(
            "00000000-0000-0000-0000-00000000000" + (cdoc2Conf.size + 1).toString(),
        )
    val customDefaultCDOC2FetchUrl = "https://cdoc2-keyserver-get"
    val customDefaultCDOC2PostUrl = "https://cdoc2-keyserver-post"

    val cdoc2ConfManual =
        CDOC2Conf(
            uuid = customDefaultCDOC2UUID,
            name = manualKeyTransferText,
            post = customDefaultCDOC2FetchUrl,
            fetch = customDefaultCDOC2PostUrl,
        )

    val allConfs = cdoc2Conf + (cdoc2ConfManual.uuid.toString() to cdoc2ConfManual)

    val configurationNames = allConfs.values.map { it.name }

    val selectedCdoc2Conf = allConfs[selectedCDOC2Service] ?: cdoc2ConfManual

    val useCDOC1Label = stringResource(R.string.main_settings_crypto_use_cdoc1)
    val useCDOC2Label = stringResource(R.string.main_settings_crypto_use_cdoc2)

    val serverLabel = stringResource(R.string.main_settings_crypto_server)
    val uuidLabel = stringResource(R.string.main_settings_crypto_uuid)
    val fetchUrlLabel = stringResource(R.string.main_settings_crypto_fetch_url)
    val postUrlLabel = stringResource(R.string.main_settings_crypto_post_url)

    var uuidText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = cdoc2Uuid,
                selection = TextRange.Zero,
            ),
        )
    }

    var fetchUrlText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = if (cdoc2FetchUrl.isNotEmpty()) cdoc2FetchUrl else selectedCdoc2Conf.fetch,
                selection = TextRange.Zero,
            ),
        )
    }

    var postUrlText by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = if (cdoc2PostUrl.isNotEmpty()) cdoc2PostUrl else selectedCdoc2Conf.post,
                selection = TextRange.Zero,
            ),
        )
    }

    val saveParameters = {
        encryptionServicesViewModel.setCdocSetting(cdocSetting)
        encryptionServicesViewModel.setUseOnlineEncryption(useKeyTransfer.value)
        var valueCDOC2UUID: String
        var valueCDOC2FetchUrl: String
        var valueCDOC2PostUrl: String

        val cdoc2Service = encryptionServicesViewModel.selectedCDOC2Service.value
        if (cdoc2Service == customDefaultCDOC2UUID.toString()) {
            useDefaultKeyTransferServer.value = false
            valueCDOC2UUID = cdoc2Uuid
            valueCDOC2FetchUrl = cdoc2FetchUrl
            valueCDOC2PostUrl = cdoc2PostUrl
        } else {
            val conf = allConfs[cdoc2Service] ?: cdoc2ConfManual
            useDefaultKeyTransferServer.value = true
            valueCDOC2UUID = cdoc2Service
            valueCDOC2FetchUrl = conf.fetch
            valueCDOC2PostUrl = conf.post
        }

        encryptionServicesViewModel.setCdoc2Uuid(valueCDOC2UUID)
        encryptionServicesViewModel.setCdoc2FetchUrl(valueCDOC2FetchUrl)
        encryptionServicesViewModel.setCdoc2PostUrl(valueCDOC2PostUrl)

        uuidText =
            TextFieldValue(
                text = valueCDOC2UUID,
                selection = TextRange.Zero,
            )
        fetchUrlText =
            TextFieldValue(
                text = valueCDOC2FetchUrl,
                selection = TextRange.Zero,
            )
        postUrlText =
            TextFieldValue(
                text = valueCDOC2PostUrl,
                selection = TextRange.Zero,
            )
    }

    sharedSettingsViewModel.updateCryptoCertData(context)

    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri == null) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                CoroutineScope(Dispatchers.IO).launch {
                    sharedSettingsViewModel.handleCryptoCertFile(uri)
                    withContext(Main) {
                        sharedSettingsViewModel.updateCryptoCertData(context)
                    }
                }
            },
        )

    val issuedToTitleText = stringResource(R.string.main_settings_timestamp_cert_issued_to_title)
    val validToTitleText = stringResource(R.string.main_settings_timestamp_cert_valid_to_title)
    val showCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_show_certificate_button)
    val addCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_add_certificate_button)
    val noCertificateFoundText = stringResource(R.string.main_settings_timestamp_cert_no_certificate_found)

    val buttonName = stringResource(id = R.string.button_name)

    var openOptionChooserDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("encryptionServicesScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_crypto_services_title,
                onLeftButtonClick = {
                    saveParameters()
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
                                encryptionServicesViewModel.setCdocSetting(CDOCSetting.CDOC1)
                            },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = useCDOC1Label,
                        modifier =
                            modifier
                                .weight(1f)
                                .notAccessible(),
                    )
                    RadioButton(
                        modifier =
                            modifier
                                .semantics {
                                    contentDescription = useCDOC1Label
                                },
                        selected = cdocSetting == CDOCSetting.CDOC1,
                        onClick = {
                            encryptionServicesViewModel.setCdocSetting(CDOCSetting.CDOC1)
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
                                    encryptionServicesViewModel.setCdocSetting(CDOCSetting.CDOC2)
                                },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = useCDOC2Label,
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
                                        contentDescription = useCDOC2Label
                                    },
                            selected = cdocSetting == CDOCSetting.CDOC2,
                            onClick = {
                                encryptionServicesViewModel.setCdocSetting(CDOCSetting.CDOC2)
                            },
                        )
                    }

                    if (cdocSetting == CDOCSetting.CDOC2) {
                        Spacer(modifier = modifier.height(LPadding))

                        SettingsSwitchItem(
                            modifier = modifier,
                            checked = useKeyTransfer.value,
                            onCheckedChange = {
                                useKeyTransfer.value = it
                                saveParameters()
                            },
                            title = keyTransferText,
                            contentDescription = keyTransferText,
                            testTag = "encryptionServicesManuallySpecifiedKeySwitch",
                        )

                        val isCustomServerEditable =
                            cdocSetting == CDOCSetting.CDOC2 &&
                                useKeyTransfer.value &&
                                !useDefaultKeyTransferServer.value

                        if (useKeyTransfer.value) {
                            Box(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .semantics {
                                            contentDescription = serverLabel
                                        },
                            ) {
                                PrimaryTextField(
                                    value = TextFieldValue(text = selectedCdoc2Conf.name),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    label = serverLabel,
                                    trailingIcon = {
                                        Icon(
                                            imageVector =
                                                ImageVector.vectorResource(
                                                    R.drawable.ic_baseline_keyboard_arrow_down_24,
                                                ),
                                            contentDescription = serverLabel,
                                            modifier =
                                                modifier.clickable {
                                                    openOptionChooserDialog = !openOptionChooserDialog
                                                },
                                        )
                                    },
                                    testTag = "encryptionServicesServerTextField",
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
                                                ).semantics {
                                                    contentDescription =
                                                        "$serverLabel: ${selectedCdoc2Conf.name}"
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
                                                title = R.string.choose_server_option,
                                                choices = configurationNames,
                                                selectedChoice =
                                                    allConfs.keys.indexOf(
                                                        selectedCdoc2Conf.uuid.toString(),
                                                    ),
                                                cancelButtonClick = {
                                                    openOptionChooserDialog = false
                                                },
                                                okButtonClick = { selectedIndex ->
                                                    val entries = allConfs.entries.toList()
                                                    val entry = entries[selectedIndex]
                                                    encryptionServicesViewModel.setSelectedCDOC2Service(
                                                        entry.value.uuid.toString(),
                                                    )
                                                    useDefaultKeyTransferServer.value =
                                                        selectedCDOC2Service != customDefaultCDOC2UUID.toString()
                                                    saveParameters()
                                                    openOptionChooserDialog = false
                                                },
                                            )
                                            InvisibleElement(modifier = modifier)
                                        }
                                    }
                                }
                            }

                            PrimaryTextField(
                                modifier = Modifier.padding(vertical = MSPadding),
                                value = uuidText,
                                onValueChange = {
                                    uuidText = it
                                    encryptionServicesViewModel.setCdoc2Uuid(it.text)
                                },
                                singleLine = true,
                                label = uuidLabel,
                                enabled = isCustomServerEditable,
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Text,
                                    ),
                                testTag = "encryptionServicesUuidTextField",
                                removeIconTestTag = "encryptionServicesUuidRemoveIconButton",
                            )

                            PrimaryTextField(
                                modifier = Modifier.padding(vertical = MSPadding),
                                value = fetchUrlText,
                                onValueChange = {
                                    fetchUrlText = it
                                    encryptionServicesViewModel.setCdoc2FetchUrl(it.text)
                                },
                                singleLine = true,
                                label = fetchUrlLabel,
                                enabled = isCustomServerEditable,
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next,
                                        keyboardType = KeyboardType.Uri,
                                    ),
                                testTag = "encryptionServicesFetchUrlTextField",
                                removeIconTestTag = "encryptionServicesFetchUrlRemoveIconButton",
                            )

                            PrimaryTextField(
                                modifier = Modifier.padding(vertical = MSPadding),
                                value = postUrlText,
                                onValueChange = {
                                    postUrlText = it
                                    encryptionServicesViewModel.setCdoc2PostUrl(it.text)
                                },
                                singleLine = true,
                                label = postUrlLabel,
                                enabled = isCustomServerEditable,
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.Uri,
                                    ),
                                testTag = "encryptionServicesPostUrlTextField",
                                removeIconTestTag = "encryptionServicesPostUrlRemoveIconButton",
                            )

                            if (isCustomServerEditable) {
                                Spacer(modifier = modifier.height(SPadding))

                                Text(
                                    modifier =
                                        modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                heading()
                                            },
                                    text = stringResource(R.string.main_settings_crypto_certificate_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                )

                                if (cryptoCertificate != null) {
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
                                    if (cryptoCertificate != null) {
                                        TextButton(onClick = {
                                            cryptoCertificate?.let {
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
                                                        }.testTag("encryptionServicesShowCertificateActionButton"),
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
                                                    }.testTag("encryptionServicesAddCertificateActionButton"),
                                            text = addCertificateButtonText,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
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
