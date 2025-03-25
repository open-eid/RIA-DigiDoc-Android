@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
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
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.buttonRoundCornerShape
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.viewmodel.DiagnosticsViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
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
    val activity = (context as Activity)

    markAsSecure(context, activity.window)

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    val currentConfiguration by
        diagnosticsViewModel.updatedConfiguration.asFlow().collectAsState(
            null,
        )
    var actionFile by remember { mutableStateOf<File?>(null) }
    var enableOneTimeLogGeneration by remember {
        mutableStateOf(diagnosticsViewModel.dataStore.getIsLogFileGenerationEnabled())
    }
    val openRestartConfirmationDialog = remember { mutableStateOf(false) }

    val settingValueChanged = stringResource(id = R.string.setting_value_changed)
    val settingValueChangeCancelled = stringResource(id = R.string.setting_value_change_cancelled)
    val closeRestartConfirmationDialog = {
        openRestartConfirmationDialog.value = false
    }
    val dismissRestartConfirmationDialog = {
        enableOneTimeLogGeneration = false
        diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
        closeRestartConfirmationDialog()
        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, settingValueChangeCancelled)
    }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                actionFile?.let { file ->
                    diagnosticsViewModel.saveFile(file, result)
                }
                showMessage(context, R.string.file_saved)
            }
        }

    val saveLogFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                actionFile?.let { file ->
                    diagnosticsViewModel.saveFile(file, result)
                }
                showMessage(context, R.string.file_saved)
                enableOneTimeLogGeneration = false
                diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
                diagnosticsViewModel.dataStore.setIsLogFileGenerationRunning(false)
                diagnosticsViewModel.resetLogs(context)
                AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, settingValueChanged)
                sharedSettingsViewModel.recreateActivity(true)
            }
        }

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
                .testTag("diagnosticsScreen"),
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
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .testTag("scrollView"),
                horizontalAlignment = Alignment.Start,
            ) {
                PrimaryOutlinedButton(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("configurationUpdateButton"),
                    contentDescription =
                        stringResource(
                            id = R.string.main_diagnostics_configuration_check_for_update_button,
                        ).lowercase(),
                    title = R.string.main_diagnostics_configuration_check_for_update_button,
                    onClickItem = {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                diagnosticsViewModel.updateConfiguration(context)
                            } catch (e: Exception) {
                                withContext(Main) {
                                    showMessage(context, R.string.no_internet_connection)
                                }
                            }
                        }
                    },
                )
                PrimaryOutlinedButton(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("configurationSaveButton"),
                    contentDescription =
                        stringResource(
                            id = R.string.main_diagnostics_configuration_save_diagnostics_button,
                        ).lowercase(),
                    title = R.string.main_diagnostics_configuration_save_diagnostics_button,
                    iconRes = R.drawable.ic_m3_download_48dp_wght400,
                    onClickItem = {
                        try {
                            val diagnosticsFile =
                                diagnosticsViewModel.createDiagnosticsFile(context)
                            actionFile = diagnosticsFile
                            val saveIntent =
                                Intent.createChooser(
                                    Intent(Intent.ACTION_CREATE_DOCUMENT)
                                        .addCategory(Intent.CATEGORY_OPENABLE)
                                        .putExtra(
                                            Intent.EXTRA_TITLE,
                                            sanitizeString(diagnosticsFile.name, ""),
                                        )
                                        .setType("text/plain")
                                        .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                                    null,
                                )
                            saveFileLauncher.launch(saveIntent)
                        } catch (e: ActivityNotFoundException) {
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
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsLogging"),
                    checked = enableOneTimeLogGeneration,
                    onCheckedChange = {
                        if (!enableOneTimeLogGeneration) {
                            openRestartConfirmationDialog.value = true
                        } else {
                            enableOneTimeLogGeneration = false
                            diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
                            diagnosticsViewModel.dataStore.setIsLogFileGenerationRunning(false)
                            diagnosticsViewModel.resetLogs(context)
                            AccessibilityUtil.sendAccessibilityEvent(
                                context,
                                TYPE_ANNOUNCEMENT,
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
                                }
                                .testTag("mainDiagnosticsSaveLoggingButton"),
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
                                            )
                                            .setType("text/x-log")
                                            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION),
                                        null,
                                    )
                                saveLogFileLauncher.launch(saveIntent)
                            } catch (e: ActivityNotFoundException) {
                                // no Activity to handle this kind of files
                            }
                        },
                    )
                }

                SpannableBoldText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsApplicationVersion"),
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
                            }
                            .testTag("mainDiagnosticsAndroidVersion"),
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
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsLibdigidocppVersion"),
                    stringResource(id = R.string.main_diagnostics_libdigidocpp_title),
                    libdigidocppVersion.value,
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
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsConfigUrl"),
                    stringResource(id = R.string.main_diagnostics_config_url_title),
                    currentConfiguration?.metaInf?.url ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsTslUrl"),
                    stringResource(id = R.string.main_diagnostics_tsl_url_title),
                    currentConfiguration?.tslUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsSivaUrl"),
                    stringResource(id = R.string.main_diagnostics_siva_url_title),
                    diagnosticsViewModel.getSivaUrl(),
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsTsaUrl"),
                    stringResource(id = R.string.main_diagnostics_tsa_url_title),
                    diagnosticsViewModel.getTsaUrl(),
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsLdapPersonUrl"),
                    stringResource(id = R.string.main_diagnostics_ldap_person_url_title),
                    currentConfiguration?.ldapPersonUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsLdapCorpUrl"),
                    stringResource(id = R.string.main_diagnostics_ldap_corp_url_title),
                    currentConfiguration?.ldapCorpUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsMobileIDUrl"),
                    stringResource(id = R.string.main_diagnostics_mid_proxy_url_title),
                    currentConfiguration?.midRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsMobileIDSKUrl"),
                    stringResource(id = R.string.main_diagnostics_mid_sk_url_title),
                    currentConfiguration?.midSkRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsSmartIDUrlV2"),
                    stringResource(id = R.string.main_diagnostics_sid_v2_proxy_url_title),
                    currentConfiguration?.sidV2RestUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsSmartIDSKUrlV2"),
                    stringResource(id = R.string.main_diagnostics_sid_v2_sk_url_title),
                    currentConfiguration?.sidV2SkRestUrl ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsRpUuid"),
                    stringResource(id = R.string.main_diagnostics_rpuuid_title),
                    stringResource(diagnosticsViewModel.getRpUuid()),
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
                SpannableBoldText(
                    modifier = modifier,
                    stringResource(id = R.string.main_diagnostics_central_configuration_title),
                    "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationDate"),
                    stringResource(id = R.string.main_diagnostics_date_title),
                    currentConfiguration?.metaInf?.date ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationSerial"),
                    stringResource(id = R.string.main_diagnostics_serial_title),
                    currentConfiguration?.metaInf?.serial.toString(),
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationUrl"),
                    stringResource(id = R.string.main_diagnostics_url_title),
                    currentConfiguration?.metaInf?.url ?: "",
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationVersion"),
                    stringResource(id = R.string.main_diagnostics_version_title),
                    currentConfiguration?.metaInf?.version.toString(),
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationUpdateDate"),
                    stringResource(id = R.string.main_diagnostics_configuration_update_date),
                    diagnosticsViewModel.getConfigurationDate(currentConfiguration?.configurationUpdateDate),
                )
                DiagnosticsText(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("mainDiagnosticsCentralConfigurationLastCheck"),
                    stringResource(id = R.string.main_diagnostics_configuration_last_check_date),
                    diagnosticsViewModel.getConfigurationDate(currentConfiguration?.configurationLastUpdateCheckDate),
                )
                HorizontalDivider(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(horizontal = XSPadding)
                            .padding(top = SPadding),
                )

                if (openRestartConfirmationDialog.value) {
                    BasicAlertDialog(
                        modifier =
                            modifier
                                .clip(buttonRoundCornerShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .semantics {
                                    testTagsAsResourceId = true
                                }
                                .testTag("mainDiagnosticsRestartConfirmationDialog"),
                        onDismissRequest = dismissRestartConfirmationDialog,
                    ) {
                        Surface(
                            modifier =
                                modifier
                                    .padding(SPadding)
                                    .wrapContentHeight()
                                    .wrapContentWidth(),
                        ) {
                            Column(
                                modifier =
                                    modifier
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("diagnosticsActivateLoggingContainer"),
                            ) {
                                HrefMessageDialog(
                                    text1 = R.string.main_diagnostics_restart_message,
                                    text2 = R.string.main_diagnostics_restart_message_restart_now,
                                    linkText = R.string.main_diagnostics_restart_message_read_more,
                                    linkUrl = R.string.main_diagnostics_restart_message_href,
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
                                        AccessibilityUtil.sendAccessibilityEvent(
                                            context,
                                            TYPE_ANNOUNCEMENT,
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
