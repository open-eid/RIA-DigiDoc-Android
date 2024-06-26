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
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun SettingsSigningFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
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
            getIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress,
            setIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::setSettingsAskRoleAndAddress,
            getSettingsUUID = sharedSettingsViewModel.dataStore::getSettingsUUID,
            setSettingsUUID = sharedSettingsViewModel.dataStore::setSettingsUUID,
            getSettingsTSAUrl = sharedSettingsViewModel.dataStore::getSettingsTSAUrl,
            setSettingsTSAUrl = sharedSettingsViewModel.dataStore::setSettingsTSAUrl,
            getProxySetting = sharedSettingsViewModel.dataStore::getProxySetting,
            setProxySetting = sharedSettingsViewModel.dataStore::setProxySetting,
            getProxyHost = sharedSettingsViewModel.dataStore::getProxyHost,
            setProxyHost = sharedSettingsViewModel.dataStore::setProxyHost,
            getProxyPort = sharedSettingsViewModel.dataStore::getProxyPort,
            setProxyPort = sharedSettingsViewModel.dataStore::setProxyPort,
            getProxyUsername = sharedSettingsViewModel.dataStore::getProxyUsername,
            setProxyUsername = sharedSettingsViewModel.dataStore::setProxyUsername,
            getProxyPassword = sharedSettingsViewModel.dataStore::getProxyPassword,
            setProxyPassword = sharedSettingsViewModel.dataStore::setProxyPassword,
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
