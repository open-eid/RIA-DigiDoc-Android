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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.CancelAndOkButtonRow
import ee.ria.DigiDoc.ui.component.shared.DiagnosticsText
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryOutlinedButton
import ee.ria.DigiDoc.ui.component.shared.SpannableBoldText
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.keyboard.keyboardScrollable
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.viewmodel.DiagnosticsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun DiagnosticsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    diagnosticsViewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val screenScrollState = rememberScrollState()
    val loggingDialogScrollState = rememberScrollState()

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val currentConfiguration by
        diagnosticsViewModel.updatedConfiguration.asFlow().collectAsState(
            null,
        )
    var actionFile by remember { mutableStateOf<File?>(null) }
    var enableOneTimeLogGeneration by remember {
        mutableStateOf(diagnosticsViewModel.dataStore.getIsLogFileGenerationEnabled())
    }
    val openRestartConfirmationDialog = rememberSaveable { mutableStateOf(false) }

    val settingValueChanged = stringResource(id = R.string.setting_value_changed)
    val settingValueChangeCancelled = stringResource(id = R.string.setting_value_change_cancelled)
    val closeRestartConfirmationDialog = {
        openRestartConfirmationDialog.value = false
    }
    val dismissRestartConfirmationDialog = {
        enableOneTimeLogGeneration = false
        diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
        closeRestartConfirmationDialog()
        sendAccessibilityEvent(context, getAccessibilityEventType(), settingValueChangeCancelled)
    }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                actionFile?.let { file ->
                    diagnosticsViewModel.saveFile(file, result)
                }
                showMessage(context, R.string.file_saved, SnackbarType.SUCCESS)
            }
        }

    val saveLogFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                actionFile?.let { file ->
                    diagnosticsViewModel.saveFile(file, result)
                }
                showMessage(context, R.string.file_saved, SnackbarType.SUCCESS)
                enableOneTimeLogGeneration = false
                diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
                diagnosticsViewModel.dataStore.setIsLogFileGenerationRunning(false)
                diagnosticsViewModel.resetLogs(context)
                sendAccessibilityEvent(context, getAccessibilityEventType(), settingValueChanged)
                sharedSettingsViewModel.recreateActivity(true)
            }
        }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("diagnosticsScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_diagnostics_title,
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
        )
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    modifier
                        .verticalScroll(screenScrollState)
                        .keyboardScrollable(screenScrollState)
                        .fillMaxWidth()
                        .testTag("scrollView"),
                horizontalAlignment = Alignment.Start,
            ) {
                PrimaryOutlinedButton(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("configurationUpdateButton"),
                    contentDescription =
                        stringResource(
                            id = R.string.main_diagnostics_configuration_check_for_update_button,
                        ).lowercase(),
                    title = R.string.main_diagnostics_configuration_check_for_update_button,
                    onClickItem = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    diagnosticsViewModel.updateConfiguration(context)
                                }
                                showMessage(context, R.string.configuration_update_success, SnackbarType.SUCCESS)
                            } catch (_: Exception) {
                                showMessage(context, R.string.configuration_update_failed)
                            }
                        }
                    },
                )
                PrimaryOutlinedButton(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("configurationSaveButton"),
                    contentDescription =
                        stringResource(
                            id = R.string.main_diagnostics_configuration_save_diagnostics_button,
                        ).lowercase(),
                    title = R.string.main_diagnostics_configuration_save_diagnostics_button,
                    iconRes = R.drawable.ic_m3_download_48dp_wght400,
                    onClickItem = {
                        try {
                            val diagnosticsFile =
                                diagnosticsViewModel.createDiagnosticsFile(context, currentConfiguration)
                            actionFile = diagnosticsFile
                            val saveIntent =
                                Intent.createChooser(
                                    Intent(Intent.ACTION_CREATE_DOCUMENT)
                                        .addCategory(Intent.CATEGORY_OPENABLE)
                                        .putExtra(
                                            Intent.EXTRA_TITLE,
                                            sanitizeString(diagnosticsFile.name, ""),
                                        ).setType("text/plain")
                                        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                                    null,
                                )
                            saveFileLauncher.launch(saveIntent)
                        } catch (_: ActivityNotFoundException) {
                            // no Activity to handle this kind of files
                        }
                    },
                )

                val enableOneTimeLogGenerationSwitchContentDescription =
                    stringResource(
                        id = R.string.main_diagnostics_logging_switch,
                    ).lowercase()
                SettingsSwitchItem(
                    modifier =
                        modifier
                            .padding(
                                start = XSPadding,
                                end = XSPadding,
                                top = XSPadding,
                                bottom = zeroPadding,
                            ).semantics {
                                testTagsAsResourceId = true
                            }.testTag("mainDiagnosticsLogging"),
                    checked = enableOneTimeLogGeneration,
                    onCheckedChange = {
                        if (!enableOneTimeLogGeneration) {
                            openRestartConfirmationDialog.value = true
                        } else {
                            enableOneTimeLogGeneration = false
                            diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
                            diagnosticsViewModel.dataStore.setIsLogFileGenerationRunning(false)
                            diagnosticsViewModel.resetLogs(context)
                            sendAccessibilityEvent(
                                context,
                                getAccessibilityEventType(),
                                settingValueChanged,
                            )
                            sharedSettingsViewModel.recreateActivity(true)
                        }
                    },
                    title = stringResource(id = R.string.main_diagnostics_logging_switch),
                    contentDescription = enableOneTimeLogGenerationSwitchContentDescription,
                )
                if (enableOneTimeLogGeneration) {
                    PrimaryOutlinedButton(
                        modifier =
                            modifier
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("mainDiagnosticsSaveLoggingButton"),
                        contentDescription =
                            stringResource(
                                id = R.string.main_diagnostics_save_log,
                            ).lowercase(),
                        title = R.string.main_diagnostics_save_log,
                        iconRes = R.drawable.ic_m3_download_48dp_wght400,
                        onClickItem = {
                            try {
                                val logFile = diagnosticsViewModel.createLogFile(context)
                                actionFile = logFile
                                val saveIntent =
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                                            .addCategory(Intent.CATEGORY_OPENABLE)
                                            .putExtra(
                                                Intent.EXTRA_TITLE,
                                                sanitizeString(logFile.name, ""),
                                            ).setType("text/x-log")
                                            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                                        null,
                                    )
                                saveLogFileLauncher.launch(saveIntent)
                            } catch (_: ActivityNotFoundException) {
                                // no Activity to handle this kind of files
                            }
                        },
                    )
                }

                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_central_configuration_title),
                    "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationDate",
                    labelRes = R.string.main_diagnostics_date_title,
                    value = currentConfiguration?.metaInf?.date ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationSerial",
                    labelRes = R.string.main_diagnostics_serial_title,
                    value = currentConfiguration?.metaInf?.serial.toString(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationUrl",
                    labelRes = R.string.main_diagnostics_url_title,
                    value = currentConfiguration?.metaInf?.url ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationVersion",
                    labelRes = R.string.main_diagnostics_version_title,
                    value = currentConfiguration?.metaInf?.version.toString(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationUpdateDate",
                    labelRes = R.string.main_diagnostics_configuration_update_date,
                    value = diagnosticsViewModel.getConfigurationDate(currentConfiguration?.configurationUpdateDate),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCentralConfigurationLastCheck",
                    labelRes = R.string.main_diagnostics_configuration_last_check_date,
                    value =
                        diagnosticsViewModel.getConfigurationDate(
                            currentConfiguration?.configurationLastUpdateCheckDate,
                        ),
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )
                SpannableBoldText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("mainDiagnosticsApplicationVersion"),
                    stringResource(id = R.string.main_diagnostics_application_version_title),
                    "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}",
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )
                SpannableBoldText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("mainDiagnosticsAndroidVersion"),
                    stringResource(id = R.string.main_diagnostics_operating_system_title),
                    "Android " + Build.VERSION.RELEASE,
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )
                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_libraries_title),
                    "",
                )
                val libdigidocppVersion =
                    remember {
                        mutableStateOf(
                            diagnosticsViewModel.dataStore.getLibdigidocppVersion(),
                        )
                    }
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsLibdigidocppVersion",
                    labelRes = R.string.main_diagnostics_libdigidocpp_title,
                    value = libdigidocppVersion.value,
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )
                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_urls_title),
                    "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsConfigUrl",
                    labelRes = R.string.main_diagnostics_config_url_title,
                    value = currentConfiguration?.metaInf?.url ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsTslUrl",
                    labelRes = R.string.main_diagnostics_tsl_url_title,
                    value = currentConfiguration?.tslUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsSivaUrl",
                    labelRes = R.string.main_diagnostics_siva_url_title,
                    value = diagnosticsViewModel.getSivaUrl(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsTsaUrl",
                    labelRes = R.string.main_diagnostics_tsa_url_title,
                    value = diagnosticsViewModel.getTsaUrl(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsLdapPersonUrl",
                    labelRes = R.string.main_diagnostics_ldap_person_url_title,
                    value = currentConfiguration?.ldapPersonUrls?.joinToString(", ") ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsLdapCorpUrl",
                    labelRes = R.string.main_diagnostics_ldap_corp_url_title,
                    value = currentConfiguration?.ldapCorpUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsMobileIDUrl",
                    labelRes = R.string.main_diagnostics_mid_proxy_url_title,
                    value = currentConfiguration?.midRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsMobileIDSKUrl",
                    labelRes = R.string.main_diagnostics_mid_sk_url_title,
                    value = currentConfiguration?.midSkRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsSmartIDUrlV2",
                    labelRes = R.string.main_diagnostics_sid_v2_proxy_url_title,
                    value = currentConfiguration?.sidV2RestUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsSmartIDSKUrlV2",
                    labelRes = R.string.main_diagnostics_sid_v2_sk_url_title,
                    value = currentConfiguration?.sidV2SkRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsRpUuid",
                    labelRes = R.string.main_diagnostics_rpuuid_title,
                    value = stringResource(diagnosticsViewModel.getRpUuid()),
                )
                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_cdoc2_title),
                    "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCdoc2Default",
                    labelRes = R.string.main_diagnostics_cdoc2_default_title,
                    value = diagnosticsViewModel.isCdoc2Selected(currentConfiguration).toString(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCdoc2UseKeyserver",
                    labelRes = R.string.main_diagnostics_cdoc2_use_keyserver_title,
                    value = diagnosticsViewModel.isCdoc2KeyServerUsed(currentConfiguration).toString(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsCdoc2DefaultKeyserver",
                    labelRes = R.string.main_diagnostics_cdoc2_default_keyserver_title,
                    value = diagnosticsViewModel.getCdoc2KeyServerUUID(currentConfiguration),
                )
                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_settings_title),
                    "",
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsProxyConfig",
                    labelRes = R.string.main_diagnostics_proxy_config_title,
                    value = diagnosticsViewModel.getProxyConfig(),
                )
                DiagnosticsText(
                    modifier = modifier,
                    testTag = "mainDiagnosticsProxyAuth",
                    labelRes = R.string.main_diagnostics_proxy_auth_title,
                    value = diagnosticsViewModel.isProxyAuthEnabled().toString(),
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )
                Column(
                    modifier = modifier.testTag("mainDiagnosticsTslCacheLayout"),
                ) {
                    SpannableBoldText(
                        modifier = modifier,
                        stringResource(id = R.string.main_diagnostics_tsl_cache_title),
                        "",
                    )
                    diagnosticsViewModel.getTslCacheData(context).forEach { data ->
                        Text(
                            modifier = modifier.padding(horizontal = SPadding),
                            text = data,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    HorizontalDivider(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(horizontal = XSPadding)
                                .padding(top = SPadding),
                    )
                }
                if (openRestartConfirmationDialog.value) {
                    BasicAlertDialog(
                        modifier =
                            modifier
                                .clip(buttonRoundCornerShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .semantics {
                                    testTagsAsResourceId = true
                                }.testTag("mainDiagnosticsRestartConfirmationDialog"),
                        onDismissRequest = dismissRestartConfirmationDialog,
                    ) {
                        Surface(
                            modifier =
                                modifier
                                    .padding(SPadding)
                                    .wrapContentHeight()
                                    .wrapContentWidth()
                                    .verticalScroll(loggingDialogScrollState),
                        ) {
                            Column(
                                modifier =
                                    modifier
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.keyboardScrollable(loggingDialogScrollState)
                                        .testTag("diagnosticsActivateLoggingContainer"),
                            ) {
                                HrefMessageDialog(
                                    text1 = R.string.main_diagnostics_restart_message,
                                    text2 = R.string.main_diagnostics_restart_message_restart_now,
                                    linkText = R.string.read_more_here,
                                    linkUrl = R.string.main_diagnostics_restart_message_href,
                                    newLineBeforeLink = true,
                                    newLineBeforeText2 = true,
                                )

                                CancelAndOkButtonRow(
                                    okButtonTestTag = "hrefMessageDialogOkButton",
                                    cancelButtonTestTag = "hrefMessageDialogCancelButton",
                                    cancelButtonClick = dismissRestartConfirmationDialog,
                                    okButtonClick = {
                                        enableOneTimeLogGeneration = true
                                        diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(
                                            true,
                                        )
                                        closeRestartConfirmationDialog()
                                        sendAccessibilityEvent(
                                            context,
                                            getAccessibilityEventType(),
                                            settingValueChanged,
                                        )
                                        sharedSettingsViewModel.recreateActivity(true)
                                    },
                                    cancelButtonTitle = R.string.cancel_button,
                                    okButtonTitle = R.string.ok_button,
                                    cancelButtonContentDescription =
                                        stringResource(
                                            id = R.string.cancel_button,
                                        ).lowercase(),
                                    okButtonContentDescription = stringResource(id = R.string.ok_button).lowercase(),
                                    showCancelButton = true,
                                )
                                InvisibleElement(modifier = modifier)
                            }
                        }
                    }
                }
                InvisibleElement(modifier = modifier)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DiagnosticsScreenPreview() {
    RIADigiDocTheme {
        DiagnosticsScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
        )
    }
}
