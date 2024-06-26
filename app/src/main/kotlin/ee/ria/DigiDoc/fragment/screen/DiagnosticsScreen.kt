@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.SpannableBoldText
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utilsLib.file.FileUtil.sanitizeString
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil.showMessage
import ee.ria.DigiDoc.viewmodel.DiagnosticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DiagnosticsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    diagnosticsViewModel: DiagnosticsViewModel = hiltViewModel(),
) {
    diagnosticsViewModel.refreshConfigurationVariables()
    val context = LocalContext.current
    val getConfigurationLastUpdateCheckDate by
        diagnosticsViewModel.getConfigurationLastUpdateCheckDate.asFlow().collectAsState(
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
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_application_version_title),
                BuildConfig.VERSION_NAME,
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_operating_system_title),
                "Android " + Build.VERSION.RELEASE,
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
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_libdigidocpp_title),
                libdigidocppVersion.value,
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_urls_title),
                "",
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_config_url_title),
                diagnosticsViewModel.getConfigUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_tsl_url_title),
                diagnosticsViewModel.getTslUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_siva_url_title),
                diagnosticsViewModel.getSivaUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_tsa_url_title),
                diagnosticsViewModel.getTsaUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_ldap_person_url_title),
                diagnosticsViewModel.getLdapPersonUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_ldap_corp_url_title),
                diagnosticsViewModel.getLdapCorpUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_mid_proxy_url_title),
                diagnosticsViewModel.getMidRestUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_mid_sk_url_title),
                diagnosticsViewModel.getMidSkRestUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_sid_v2_proxy_url_title),
                diagnosticsViewModel.getSidV2RestUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_sid_v2_sk_url_title),
                diagnosticsViewModel.getSidV2SkRestUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_rpuuid_title),
                stringResource(diagnosticsViewModel.getRpUuid()),
            )
            SpannableBoldText(
                modifier = modifier,
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
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_central_configuration_title),
                "",
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_date_title),
                diagnosticsViewModel.getMetaInfGetDate(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_serial_title),
                diagnosticsViewModel.getMetaInfGetSerial(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_url_title),
                diagnosticsViewModel.getMetaInfGetUrl(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_version_title),
                diagnosticsViewModel.getMetaInfGetVersion(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_configuration_update_date),
                diagnosticsViewModel.getConfigurationUpdateDate(),
            )
            SpannableBoldText(
                modifier = modifier,
                stringResource(id = R.string.main_diagnostics_configuration_last_check_date),
                getConfigurationLastUpdateCheckDate ?: "",
            )
            var enableOneTimeLogGeneration by remember { mutableStateOf(false) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = enableOneTimeLogGeneration,
                onCheckedChange = {
                    enableOneTimeLogGeneration = it
                },
                title = stringResource(id = R.string.main_diagnostics_logging_switch),
                contentDescription = stringResource(id = R.string.main_diagnostics_logging_switch).lowercase(),
            )
            if (enableOneTimeLogGeneration) {
                PrimaryButton(
                    modifier =
                        modifier.fillMaxWidth().wrapContentHeight().padding(
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
                    modifier.fillMaxWidth().wrapContentHeight().padding(
                        horizontal = screenViewLargePadding,
                    ),
                contentDescription =
                    stringResource(
                        id = R.string.main_diagnostics_configuration_check_for_update_button,
                    ).lowercase(),
                title = R.string.main_diagnostics_configuration_check_for_update_button,
                onClickItem = {
                    CoroutineScope(Dispatchers.IO).launch {
                        diagnosticsViewModel.updateConfiguration()
                    }
                    diagnosticsViewModel.refreshConfigurationVariables()
                },
            )
            PrimaryButton(
                modifier =
                    modifier.fillMaxWidth().wrapContentHeight().padding(
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
