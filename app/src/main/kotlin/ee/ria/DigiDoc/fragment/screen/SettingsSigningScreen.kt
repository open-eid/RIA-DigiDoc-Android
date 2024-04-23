@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.ui.component.settings.SettingsEditValueDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsInputItem
import ee.ria.DigiDoc.ui.component.settings.SettingsItem
import ee.ria.DigiDoc.ui.component.settings.SettingsProxyCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSivaCategoryDialog
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.theme.Dimensions
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_TSA_URL_VALUE
import ee.ria.DigiDoc.utils.Constant.Defaults.DEFAULT_UUID_VALUE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSigningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    someList: List<SomeObject>? = listOf(SomeObject()),
) {
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
                        .padding(Dimensions.alertDialogOuterPadding),
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
                        .padding(Dimensions.alertDialogOuterPadding),
            ) {
                SettingsProxyCategoryDialog(
                    onClickBack = dismissSettingsProxyCategoryDialog,
                )
            }
        }
    }
    val uuidValue = DEFAULT_UUID_VALUE
    val openSettingsAccessToSigningServiceDialog = remember { mutableStateOf(false) }
    val dismissSettingsAccessToSigningServiceDialog = {
        openSettingsAccessToSigningServiceDialog.value = false
    }
    if (openSettingsAccessToSigningServiceDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissSettingsAccessToSigningServiceDialog,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(Dimensions.alertDialogOuterPadding),
            ) {
                SettingsEditValueDialog(
                    title = stringResource(id = R.string.main_settings_uuid_title),
                    placeHolderText = uuidValue,
                    cancelButtonClick = dismissSettingsAccessToSigningServiceDialog,
                    okButtonClick = {
                        // TODO:
                    },
                )
            }
        }
    }
    val tsaUrlValue = DEFAULT_TSA_URL_VALUE
    val openSettingsAccessToTimeStampingServiceDialog = remember { mutableStateOf(false) }
    val dismissSettingsAccessToTimeStampingServiceDialog = {
        openSettingsAccessToTimeStampingServiceDialog.value = false
    }
    if (openSettingsAccessToTimeStampingServiceDialog.value) {
        BasicAlertDialog(
            onDismissRequest = dismissSettingsAccessToTimeStampingServiceDialog,
        ) {
            Surface(
                modifier =
                    modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(Dimensions.alertDialogOuterPadding),
            ) {
                SettingsEditValueDialog(
                    title = stringResource(id = R.string.main_settings_tsa_url_title),
                    placeHolderText = tsaUrlValue,
                    cancelButtonClick = dismissSettingsAccessToTimeStampingServiceDialog,
                    okButtonClick = {
                        // TODO:
                    },
                )
            }
        }
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
            var checkedAskRoleAndAddress by remember { mutableStateOf(true) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedAskRoleAndAddress,
                onCheckedChange = {
                    checkedAskRoleAndAddress = it
                },
                title = stringResource(id = R.string.main_settings_ask_role_and_address_title),
                contentDescription = stringResource(id = R.string.main_settings_ask_role_and_address_title).lowercase(),
            )
            SettingsInputItem(
                value = uuidValue,
                onClickItem = {
                    openSettingsAccessToSigningServiceDialog.value = true
                },
                title = stringResource(id = R.string.main_settings_uuid_title),
                contentDescription = stringResource(id = R.string.main_settings_uuid_title).lowercase(),
            )
            SettingsInputItem(
                value = tsaUrlValue,
                onClickItem = {
                    openSettingsAccessToTimeStampingServiceDialog.value = true
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
    RIADigiDocTheme {
        SettingsSigningScreen(
            navController = navController,
        )
    }
}
