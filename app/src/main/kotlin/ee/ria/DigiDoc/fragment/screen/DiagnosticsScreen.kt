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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.DiagnosticsText
import ee.ria.DigiDoc.ui.component.shared.HrefMessageDialog
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.SpannableBoldText
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.viewmodel.DiagnosticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    diagnosticsViewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    val currentConfiguration by
        diagnosticsViewModel.updatedConfiguration.asFlow().collectAsState(
            null,
        )
    var actionFile by remember { mutableStateOf<File?>(null) }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                actionFile?.let { file ->
                    diagnosticsViewModel.saveFile(file, result)
                }
                showMessage(context, R.string.file_saved)
            }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_diagnostics_title,
                onBackButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            SpannableBoldText(
                modifier = modifier.padding(top = screenViewLargePadding),
                stringResource(id = R.string.main_diagnostics_application_version_title),
                BuildConfig.VERSION_NAME,
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_operating_system_title),
                "Android " + Build.VERSION.RELEASE,
            )
            SpannableBoldText(
                modifier = modifier.padding(top = screenViewLargePadding),
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
                stringResource(id = R.string.main_diagnostics_libdigidocpp_title),
                libdigidocppVersion.value,
            )
            SpannableBoldText(
                modifier = modifier.padding(top = screenViewLargePadding),
                stringResource(id = R.string.main_diagnostics_urls_title),
                "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_config_url_title),
                currentConfiguration?.metaInf?.url ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_tsl_url_title),
                currentConfiguration?.tslUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_siva_url_title),
                diagnosticsViewModel.getSivaUrl(),
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_tsa_url_title),
                diagnosticsViewModel.getTsaUrl(),
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_ldap_person_url_title),
                currentConfiguration?.ldapPersonUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_ldap_corp_url_title),
                currentConfiguration?.ldapCorpUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_mid_proxy_url_title),
                currentConfiguration?.midRestUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_mid_sk_url_title),
                currentConfiguration?.midSkRestUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_sid_v2_proxy_url_title),
                currentConfiguration?.sidV2RestUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_sid_v2_sk_url_title),
                currentConfiguration?.sidV2SkRestUrl ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_rpuuid_title),
                stringResource(diagnosticsViewModel.getRpUuid()),
            )
            SpannableBoldText(
                modifier = modifier.padding(top = screenViewLargePadding),
                stringResource(id = R.string.main_diagnostics_tsl_cache_title),
                "",
            )
            diagnosticsViewModel.getTslCacheData().forEach { data ->
                Text(
                    modifier = modifier.padding(horizontal = screenViewLargePadding),
                    text = data,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            SpannableBoldText(
                modifier = modifier.padding(top = screenViewLargePadding),
                stringResource(id = R.string.main_diagnostics_central_configuration_title),
                "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_date_title),
                currentConfiguration?.metaInf?.date ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_serial_title),
                currentConfiguration?.metaInf?.serial.toString(),
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_url_title),
                currentConfiguration?.metaInf?.url ?: "",
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_version_title),
                currentConfiguration?.metaInf?.version.toString(),
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_configuration_update_date),
                diagnosticsViewModel.getConfigurationDate(currentConfiguration?.configurationUpdateDate),
            )
            DiagnosticsText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_configuration_last_check_date),
                diagnosticsViewModel.getConfigurationDate(currentConfiguration?.configurationLastUpdateCheckDate),
            )
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
            SettingsSwitchItem(
                modifier = modifier,
                checked = enableOneTimeLogGeneration,
                onCheckedChange = {
                    if (!enableOneTimeLogGeneration) {
                        openRestartConfirmationDialog.value = true
                    } else {
                        enableOneTimeLogGeneration = false
                        diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(false)
                        diagnosticsViewModel.dataStore.setIsLogFileGenerationRunning(false)
                        AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, settingValueChanged)
                        activity.finish()
                        activity.startActivity(activity.intent)
                    }
                },
                title = stringResource(id = R.string.main_diagnostics_logging_switch),
                contentDescription = stringResource(id = R.string.main_diagnostics_logging_switch).lowercase(),
            )
            if (enableOneTimeLogGeneration) {
                PrimaryButton(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                horizontal = screenViewLargePadding,
                            ),
                    contentDescription =
                        stringResource(
                            id = R.string.main_diagnostics_save_log,
                        ).lowercase(),
                    title = R.string.main_diagnostics_save_log,
                    onClickItem = {
                        try {
                            val diagnosticsFile = diagnosticsViewModel.createLogFile()
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
            }
            PrimaryButton(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            horizontal = screenViewLargePadding,
                        ),
                contentDescription =
                    stringResource(
                        id = R.string.main_diagnostics_configuration_check_for_update_button,
                    ).lowercase(),
                title = R.string.main_diagnostics_configuration_check_for_update_button,
                onClickItem = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            diagnosticsViewModel.updateConfiguration()
                        } catch (e: Exception) {
                            withContext(Main) {
                                showMessage(context, R.string.no_internet_connection)
                            }
                        }
                    }
                },
            )
            PrimaryButton(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            horizontal = screenViewLargePadding,
                        ),
                contentDescription =
                    stringResource(
                        id = R.string.main_diagnostics_configuration_save_diagnostics_button,
                    ).lowercase(),
                title = R.string.main_diagnostics_configuration_save_diagnostics_button,
                onClickItem = {
                    try {
                        val diagnosticsFile = diagnosticsViewModel.createDiagnosticsFile()
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
            if (openRestartConfirmationDialog.value) {
                BasicAlertDialog(
                    onDismissRequest = dismissRestartConfirmationDialog,
                ) {
                    Surface(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .padding(screenViewLargePadding)
                                .verticalScroll(rememberScrollState()),
                    ) {
                        HrefMessageDialog(
                            text1 = R.string.main_diagnostics_restart_message,
                            text2 = R.string.main_diagnostics_restart_message_restart_now,
                            linkText = R.string.main_diagnostics_restart_message_read_more,
                            linkUrl = R.string.main_diagnostics_restart_message_href,
                            cancelButtonClick = dismissRestartConfirmationDialog,
                            okButtonClick = {
                                enableOneTimeLogGeneration = true
                                diagnosticsViewModel.dataStore.setIsLogFileGenerationEnabled(true)
                                closeRestartConfirmationDialog()
                                AccessibilityUtil.sendAccessibilityEvent(
                                    context,
                                    TYPE_ANNOUNCEMENT,
                                    settingValueChanged,
                                )
                                activity.finish()
                                activity.startActivity(activity.intent)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DiagnosticsScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        DiagnosticsScreen(
            navController = navController,
        )
    }
}
