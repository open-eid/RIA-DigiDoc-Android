@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.configuration.domain.model.ConfigurationViewModel
import ee.ria.DigiDoc.fragment.screen.SettingsSigningScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.SettingsViewModel

@Composable
fun SettingsSigningFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    configurationViewModel: ConfigurationViewModel = hiltViewModel(),
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        SettingsSigningScreen(
            navController = navController,
            modifier = modifier,
            getIsRoleAskingEnabled = settingsViewModel.dataStore::getSettingsAskRoleAndAddress,
            setIsRoleAskingEnabled = settingsViewModel.dataStore::setSettingsAskRoleAndAddress,
            getSettingsUUID = settingsViewModel.dataStore::getSettingsUUID,
            setSettingsUUID = settingsViewModel.dataStore::setSettingsUUID,
            getSettingsTSAUrl = settingsViewModel.dataStore::getSettingsTSAUrl,
            setSettingsTSAUrl = settingsViewModel.dataStore::setSettingsTSAUrl,
            getProxySetting = settingsViewModel.dataStore::getProxySetting,
            setProxySetting = settingsViewModel.dataStore::setProxySetting,
            getProxyHost = settingsViewModel.dataStore::getProxyHost,
            setProxyHost = settingsViewModel.dataStore::setProxyHost,
            getProxyPort = settingsViewModel.dataStore::getProxyPort,
            setProxyPort = settingsViewModel.dataStore::setProxyPort,
            getProxyUsername = settingsViewModel.dataStore::getProxyUsername,
            setProxyUsername = settingsViewModel.dataStore::setProxyUsername,
            getProxyPassword = settingsViewModel.dataStore::getProxyPassword,
            setProxyPassword = settingsViewModel.dataStore::setProxyPassword,
            configuration = configurationViewModel.configuration,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSigningFragmentPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        SettingsSigningFragment(navController)
    }
}
