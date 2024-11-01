@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import android.text.TextUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.siva.SivaSetting
import ee.ria.DigiDoc.ui.component.settings.SettingsItem
import ee.ria.DigiDoc.ui.component.settings.SettingsProxyCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSivaCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.settings.SettingsTextField
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_TSA_URL_VALUE
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utilsLib.toast.ToastUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsSigningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedCertificateViewModel: SharedCertificateViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val configuration by sharedSettingsViewModel.updatedConfiguration.asFlow().collectAsState(
        null,
    )

    val getIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    val setIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::setSettingsAskRoleAndAddress
    val getSettingsUUID = sharedSettingsViewModel.dataStore::getSettingsUUID
    val setSettingsUUID = sharedSettingsViewModel.dataStore::setSettingsUUID
    val getSettingsTSAUrl = sharedSettingsViewModel.dataStore::getSettingsTSAUrl
    val setSettingsTSAUrl = sharedSettingsViewModel.dataStore::setSettingsTSAUrl
    val getProxySetting = sharedSettingsViewModel.dataStore::getProxySetting
    val setProxySetting = sharedSettingsViewModel.dataStore::setProxySetting
    val getProxyHost = sharedSettingsViewModel.dataStore::getProxyHost
    val setProxyHost = sharedSettingsViewModel.dataStore::setProxyHost
    val getProxyPort = sharedSettingsViewModel.dataStore::getProxyPort
    val setProxyPort = sharedSettingsViewModel.dataStore::setProxyPort
    val isValidPortNumber = sharedSettingsViewModel.dataStore::isValidPortNumber
    val getProxyUsername = sharedSettingsViewModel.dataStore::getProxyUsername
    val setProxyUsername = sharedSettingsViewModel.dataStore::setProxyUsername
    val getProxyPassword = sharedSettingsViewModel.dataStore::getProxyPassword
    val setProxyPassword = sharedSettingsViewModel.dataStore::setProxyPassword

    val getSettingsSivaUrl = sharedSettingsViewModel.dataStore::getSettingsSivaUrl
    val getSivaSetting = sharedSettingsViewModel.dataStore::getSivaSetting
    val setSettingsSivaUrl = sharedSettingsViewModel.dataStore::setSettingsSivaUrl
    val setSivaSetting = sharedSettingsViewModel.dataStore::setSivaSetting
    var settingsSivaServiceUrl by remember { mutableStateOf(TextFieldValue(text = getSettingsSivaUrl())) }
    sharedSettingsViewModel.updateData(settingsSivaServiceUrl.text)
    val issuedTo by sharedSettingsViewModel.issuedTo.asFlow().collectAsState(
        "",
    )
    val validTo by sharedSettingsViewModel.validTo.asFlow().collectAsState(
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
                    sharedSettingsViewModel.handleFile(uri)
                    withContext(Main) {
                        sharedSettingsViewModel.updateData(settingsSivaServiceUrl.text)
                    }
                }
            },
        )

    val settingsSivaServiceChoice = remember { mutableStateOf(getSivaSetting().name) }

    val openSettingsSivaCategoryDialog = remember { mutableStateOf(false) }
    val dismissSettingsSivaCategoryDialog = {
        openSettingsSivaCategoryDialog.value = false
    }

    LaunchedEffect(sharedSettingsViewModel.errorState) {
        sharedSettingsViewModel.errorState.asFlow().collect { errorState ->
            errorState?.let {
                withContext(Main) {
                    ToastUtil.showMessage(context, errorState)
                }
            }
        }
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
                    sivaSettingSelected = settingsSivaServiceChoice.value,
                    issuedTo = issuedTo ?: "",
                    validTo = validTo ?: "",
                    onClickBack = dismissSettingsSivaCategoryDialog,
                    onClickSivaSettingDefault = {
                        settingsSivaServiceChoice.value = SivaSetting.DEFAULT.name
                        setSivaSetting(SivaSetting.DEFAULT)
                    },
                    onClickSivaSettingManual = {
                        settingsSivaServiceChoice.value = SivaSetting.MANUAL.name
                        setSivaSetting(SivaSetting.MANUAL)
                    },
                    onAddCertificateClick = {
                        filePicker.launch("*/*")
                    },
                    onShowCertificateClick = {
                        sivaCertificate?.let {
                            sharedCertificateViewModel.setCertificate(
                                it,
                            )
                            navController.navigate(
                                Route.CertificateDetail.route,
                            )
                        }
                    },
                    onSettingsSivaUrlValueChanged = {
                        settingsSivaServiceUrl = it
                        setSettingsSivaUrl(it.text)
                    },
                    settingsSivaServiceUrl = settingsSivaServiceUrl,
                )
                InvisibleElement(modifier = modifier)
            }
        }
    }
    val openSettingsProxyCategoryDialog = remember { mutableStateOf(false) }
    val settingsProxyChoice = remember { mutableStateOf(getProxySetting().name) }
    var settingsProxyHost by remember { mutableStateOf(TextFieldValue(text = getProxyHost())) }
    var settingsProxyPort by remember { mutableStateOf(TextFieldValue(text = getProxyPort().toString())) }
    var settingsProxyUsername by remember { mutableStateOf(TextFieldValue(text = getProxyUsername())) }
    var settingsProxyPassword by remember { mutableStateOf(TextFieldValue(text = getProxyPassword())) }
    val dismissSettingsProxyCategoryDialog = {
        sharedSettingsViewModel.saveProxySettings(
            true,
            ManualProxy(
                host = settingsProxyHost.text,
                port = settingsProxyPort.text.toInt(),
                username = settingsProxyUsername.text,
                password = settingsProxyPassword.text,
            ),
        )
        openSettingsProxyCategoryDialog.value = false
    }
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
                    sharedSettingsViewModel = sharedSettingsViewModel,
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
                    onProxyPortValueChange = { proxyPortString ->
                        settingsProxyPort = proxyPortString
                        if (proxyPortString.text.isEmpty()) {
                            settingsProxyPort = TextFieldValue("80")
                            setProxyPort(80)
                        } else {
                            try {
                                val proxyPortInt =
                                    if (!isValidPortNumber(proxyPortString.text)) {
                                        80
                                    } else {
                                        proxyPortString.text.trim { it <= ' ' }.toInt()
                                    }
                                setProxyPort(proxyPortInt)
                            } catch (e: NumberFormatException) {
                                settingsProxyPort = TextFieldValue("80")
                                setProxyPort(80)
                            }
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
                        setProxyPassword(it.text)
                    },
                    checkConnectionClick = {
                        sharedSettingsViewModel.checkConnection(
                            ManualProxy(
                                host = settingsProxyHost.text,
                                port = settingsProxyPort.text.toInt(),
                                username = settingsProxyUsername.text,
                                password = settingsProxyPassword.text,
                            ),
                        )
                    },
                )
                InvisibleElement(modifier = modifier)
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
    val defaultTsaUrlValue = configuration?.tsaUrl ?: DEFAULT_TSA_URL_VALUE
    var tsaUrlValue = defaultTsaUrlValue
    if (!TextUtils.isEmpty(getSettingsTSAUrl())) {
        tsaUrlValue = getSettingsTSAUrl()
    }
    val useDefaultCheckedSettingsAccessToTimeStampingService =
        remember { mutableStateOf(TextUtils.isEmpty(getSettingsTSAUrl())) }
    var fieldValueSettingsAccessToTimeStampingService by remember {
        mutableStateOf(TextFieldValue(text = getSettingsTSAUrl()))
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("settingsSigningScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_signing,
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
                    .testTag("scrollView"),
        ) {
            var checkedAskRoleAndAddress by remember { mutableStateOf(getIsRoleAskingEnabled()) }
            SettingsSwitchItem(
                testTag = "mainSettingsAskRoleAndAddress",
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
                testTag = "mainSettingsAccessToSigningService",
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
                testTag = "mainSettingsAccessToTimeStampingService",
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
                testTag = "signingSettingsSivaCategory",
                modifier = modifier,
                onClickItem = {
                    openSettingsSivaCategoryDialog.value = true
                },
                imageVector = null,
                title = stringResource(id = R.string.main_settings_siva_service_title),
                contentDescription = stringResource(id = R.string.main_settings_siva_service_title).lowercase(),
            )
            SettingsItem(
                testTag = "signingSettingsProxyCategory",
                modifier = modifier,
                onClickItem = {
                    openSettingsProxyCategoryDialog.value = true
                },
                imageVector = null,
                title = stringResource(id = R.string.main_settings_proxy_title),
                contentDescription = stringResource(id = R.string.main_settings_proxy_title).lowercase(),
            )
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSigningScreenPreview() {
    RIADigiDocTheme {
        SettingsSigningScreen(
            navController = rememberNavController(),
            sharedCertificateViewModel = hiltViewModel(),
        )
    }
}
