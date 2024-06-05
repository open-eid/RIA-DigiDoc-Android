@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationViewModel
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.ui.component.settings.SettingsItem
import ee.ria.DigiDoc.ui.component.settings.SettingsProxyCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSivaCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.settings.SettingsTextField
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_TSA_URL_VALUE
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_UUID_VALUE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSigningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    getIsRoleAskingEnabled: () -> Boolean = { false },
    setIsRoleAskingEnabled: (Boolean) -> Unit = {},
    getSettingsUUID: () -> String = { "" },
    setSettingsUUID: (String) -> Unit = {},
    getSettingsTSAUrl: () -> String = { "" },
    setSettingsTSAUrl: (String) -> Unit = {},
    getProxySetting: () -> ProxySetting = { ProxySetting.NO_PROXY },
    setProxySetting: (ProxySetting) -> Unit = {},
    getProxyHost: () -> String = { "" },
    setProxyHost: (String) -> Unit = {},
    getProxyPort: () -> Int = { 80 },
    setProxyPort: (Int) -> Unit = {},
    getProxyUsername: () -> String = { "" },
    setProxyUsername: (String) -> Unit = {},
    getProxyPassword: (context: Context) -> String = { "" },
    setProxyPassword: (context: Context, password: String) -> Unit = { _: Context, _: String -> },
    configuration: LiveData<ConfigurationProvider>,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val openSettingsSivaCategoryDialog = remember { mutableStateOf(false) }
    val dismissSettingsSivaCategoryDialog = {
        openSettingsSivaCategoryDialog.value = false
    }
    if (openSettingsSivaCategoryDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissSettingsSivaCategoryDialog,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(itemSpacingPadding),
            ) {
                SettingsSivaCategoryDialog(
                    onClickBack = dismissSettingsSivaCategoryDialog,
                )
            }
        }
    }
    val openSettingsProxyCategoryDialog = remember { mutableStateOf(false) }
    val dismissSettingsProxyCategoryDialog = {
        openSettingsProxyCategoryDialog.value = false
    }
    val settingsProxyChoice = remember { mutableStateOf(getProxySetting().name) }
    var settingsProxyHost by remember { mutableStateOf(TextFieldValue(text = getProxyHost())) }
    var settingsProxyPort by remember { mutableStateOf(TextFieldValue(text = getProxyPort().toString())) }
    var settingsProxyUsername by remember { mutableStateOf(TextFieldValue(text = getProxyUsername())) }
    var settingsProxyPassword by remember { mutableStateOf(TextFieldValue(text = getProxyPassword(context))) }
    if (openSettingsProxyCategoryDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissSettingsProxyCategoryDialog,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(itemSpacingPadding),
            ) {
                SettingsProxyCategoryDialog(
                    onClickBack = dismissSettingsProxyCategoryDialog,
                    proxyChoice = settingsProxyChoice.value,
                    onClickNoProxy = {
                        settingsProxyChoice.value = ProxySetting.NO_PROXY.name
                        setProxySetting(ProxySetting.NO_PROXY)
                    },
                    onClickManualProxy = {
                        settingsProxyChoice.value = ProxySetting.MANUAL_PROXY.name
                        setProxySetting(ProxySetting.MANUAL_PROXY)
                    },
                    onClickSystemProxy = {
                        settingsProxyChoice.value = ProxySetting.SYSTEM_PROXY.name
                        setProxySetting(ProxySetting.SYSTEM_PROXY)
                    },
                    proxyHostValue = settingsProxyHost,
                    onProxyHostValueChange = {
                        settingsProxyHost = it
                        setProxyHost(it.text)
                    },
                    proxyPortValue = settingsProxyPort,
                    onProxyPortValueChange = {
                        settingsProxyPort = it
                        if (it.text.isEmpty()) {
                            settingsProxyPort = TextFieldValue("80")
                            setProxyPort(80)
                        } else {
                            setProxyPort(it.text.toInt())
                        }
                    },
                    proxyUsernameValue = settingsProxyUsername,
                    onProxyUsernameValueChange = {
                        settingsProxyUsername = it
                        setProxyUsername(it.text)
                    },
                    proxyPasswordValue = settingsProxyPassword,
                    onProxyPasswordValueChange = {
                        settingsProxyPassword = it
                        setProxyPassword(context, it.text)
                    },
                )
            }
        }
    }
    var uuidValue = DEFAULT_UUID_VALUE
    if (!TextUtils.isEmpty(getSettingsUUID())) {
        uuidValue = getSettingsUUID()
    }
    val useDefaultCheckedSettingsAccessToSigningService =
        remember { mutableStateOf(TextUtils.isEmpty(getSettingsUUID())) }
    var fieldValueSettingsAccessToSigningService by remember {
        mutableStateOf(
            TextFieldValue(text = getSettingsUUID()),
        )
    }
    var defaultTsaUrlValue = DEFAULT_TSA_URL_VALUE
    var tsaUrlValue = defaultTsaUrlValue
    configuration.observe(lifecycleOwner) { configurationProvider ->
        configurationProvider.tsaUrl.let { defaultTsaUrlValue = it }
    }
    if (!TextUtils.isEmpty(getSettingsTSAUrl())) {
        tsaUrlValue = getSettingsTSAUrl()
    }
    val useDefaultCheckedSettingsAccessToTimeStampingService =
        remember { mutableStateOf(TextUtils.isEmpty(getSettingsTSAUrl())) }
    var fieldValueSettingsAccessToTimeStampingService by remember {
        mutableStateOf(TextFieldValue(text = getSettingsTSAUrl()))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text(
                        text = stringResource(id = R.string.main_settings_signing),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClickBack = {
                            navController.navigateUp()
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding).verticalScroll(rememberScrollState()),
        ) {
            var checkedAskRoleAndAddress by remember { mutableStateOf(getIsRoleAskingEnabled()) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedAskRoleAndAddress,
                onCheckedChange = {
                    checkedAskRoleAndAddress = it
                    setIsRoleAskingEnabled(it)
                },
                title = stringResource(id = R.string.main_settings_ask_role_and_address_title),
                contentDescription = stringResource(id = R.string.main_settings_ask_role_and_address_title).lowercase(),
            )
            SettingsTextField(
                defaultValue = uuidValue,
                value = fieldValueSettingsAccessToSigningService,
                onValueChange = {
                    fieldValueSettingsAccessToSigningService = it
                    setSettingsUUID(fieldValueSettingsAccessToSigningService.text)
                },
                useDefaultChecked = useDefaultCheckedSettingsAccessToSigningService.value,
                useDefaultCheckedChange = {
                    useDefaultCheckedSettingsAccessToSigningService.value = it
                    if (it) {
                        fieldValueSettingsAccessToSigningService = TextFieldValue(text = "")
                        setSettingsUUID("")
                        uuidValue = DEFAULT_UUID_VALUE
                    }
                },
                title = stringResource(id = R.string.main_settings_uuid_title),
                contentDescription = stringResource(id = R.string.main_settings_uuid_title).lowercase(),
            )
            SettingsTextField(
                defaultValue = tsaUrlValue,
                value = fieldValueSettingsAccessToTimeStampingService,
                onValueChange = {
                    fieldValueSettingsAccessToTimeStampingService = it
                    setSettingsTSAUrl(fieldValueSettingsAccessToTimeStampingService.text)
                },
                useDefaultChecked = useDefaultCheckedSettingsAccessToTimeStampingService.value,
                useDefaultCheckedChange = {
                    useDefaultCheckedSettingsAccessToTimeStampingService.value = it
                    if (it) {
                        fieldValueSettingsAccessToTimeStampingService = TextFieldValue(text = "")
                        setSettingsTSAUrl("")
                        tsaUrlValue = defaultTsaUrlValue
                    }
                },
                title = stringResource(id = R.string.main_settings_tsa_url_title),
                contentDescription = stringResource(id = R.string.main_settings_tsa_url_title).lowercase(),
            )
            SettingsItem(
                modifier = modifier,
                onClickItem = {
                    openSettingsSivaCategoryDialog.value = true
                },
                imageVector = null,
                title = stringResource(id = R.string.main_settings_siva_service_title),
                contentDescription = stringResource(id = R.string.main_settings_siva_service_title).lowercase(),
            )
            SettingsItem(
                modifier = modifier,
                onClickItem = {
                    openSettingsProxyCategoryDialog.value = true
                },
                imageVector = null,
                title = stringResource(id = R.string.main_settings_proxy_title),
                contentDescription = stringResource(id = R.string.main_settings_proxy_title).lowercase(),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSigningScreenPreview() {
    val navController = rememberNavController()
    val configurationViewModel: ConfigurationViewModel = hiltViewModel()
    RIADigiDocTheme {
        SettingsSigningScreen(
            navController = navController,
            configuration = configurationViewModel.configuration,
        )
    }
}
