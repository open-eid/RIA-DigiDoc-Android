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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.shared.SettingsRadioCard
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryTextField
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
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
    val scope = rememberCoroutineScope()

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val configuration = sharedSettingsViewModel.updatedConfiguration.value

    val getSettingsSivaUrl = sharedSettingsViewModel.dataStore::getSettingsSivaUrl
    val getSivaSetting = sharedSettingsViewModel.dataStore::getSivaSetting
    val setSettingsSivaUrl = sharedSettingsViewModel.dataStore::setSettingsSivaUrl
    val setSivaSetting = sharedSettingsViewModel.dataStore::setSivaSetting
    val defaultSivaServiceUrl = getSettingsSivaUrl().ifEmpty { configuration?.sivaUrl } ?: ""
    val settingsSivaServiceChoice = remember { mutableStateOf(getSivaSetting().name) }
    var settingsSivaServiceUrl by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = defaultSivaServiceUrl,
                selection = TextRange(defaultSivaServiceUrl.length),
            ),
        )
    }
    sharedSettingsViewModel.updateSivaData(settingsSivaServiceUrl.text, context)
    val issuedTo by sharedSettingsViewModel.sivaIssuedTo.collectAsState(
        "",
    )
    val validTo by sharedSettingsViewModel.sivaValidTo.collectAsState(
        "",
    )

    val sivaCertificate by sharedSettingsViewModel.sivaCertificate.collectAsState(
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
                scope.launch(Dispatchers.IO) {
                    sharedSettingsViewModel.handleSivaFile(uri)
                    withContext(Main) {
                        sharedSettingsViewModel.updateSivaData(settingsSivaServiceUrl.text, context)
                    }
                }
            },
        )

    val issuedToTitleText = stringResource(R.string.main_settings_timestamp_cert_issued_to_title)
    val validToTitleText = stringResource(R.string.main_settings_timestamp_cert_valid_to_title)
    val showCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_show_certificate_button)
    val addCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_add_certificate_button)
    val noCertificateFoundText = stringResource(R.string.main_settings_timestamp_cert_no_certificate_found)

    val useDefaultAccessText = stringResource(R.string.main_settings_siva_default_access_title)
    val useManualAccessText = stringResource(R.string.main_settings_siva_default_manual_access_title)

    val buttonName = stringResource(id = R.string.button_name)

    // Reset SiVa URL when the user navigates away from this screen and has set default choice
    DisposableEffect(Unit) {
        onDispose {
            if (settingsSivaServiceChoice.value == SivaSetting.DEFAULT.name) {
                setSettingsSivaUrl(configuration?.sivaUrl ?: "")
            }
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("validationServicesSettingsScreen"),
        snackbarHost = { StatusSnackbarHost() },
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

            SettingsRadioCard(
                modifier = modifier,
                label = useDefaultAccessText,
                selected = settingsSivaServiceChoice.value == SivaSetting.DEFAULT.name,
                onClick = {
                    settingsSivaServiceChoice.value = SivaSetting.DEFAULT.name
                    setSivaSetting(SivaSetting.DEFAULT)
                },
            )

            SettingsRadioCard(
                modifier = modifier,
                label = useManualAccessText,
                selected = settingsSivaServiceChoice.value == SivaSetting.MANUAL.name,
                onClick = {
                    settingsSivaServiceChoice.value = SivaSetting.MANUAL.name
                    setSivaSetting(SivaSetting.MANUAL)
                },
            ) {
                if (settingsSivaServiceChoice.value == SivaSetting.MANUAL.name) {
                    PrimaryTextField(
                        modifier =
                            Modifier
                                .padding(vertical = LPadding),
                        value = settingsSivaServiceUrl,
                        onValueChange = {
                            settingsSivaServiceUrl = it
                            setSettingsSivaUrl(it.text)
                        },
                        singleLine = true,
                        label = stringResource(R.string.main_settings_siva_service_url),
                        enabled = settingsSivaServiceChoice.value == SivaSetting.MANUAL.name,
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Uri,
                            ),
                        testTag = "validationServicesComponentTextField",
                        removeIconTestTag = "validationServicesRemoveIconButton",
                    )

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
                                            }.testTag("validationServicesShowCertificateActionButton"),
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
                                        }.testTag("validationServicesAddCertificateActionButton"),
                                text = addCertificateButtonText,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}
