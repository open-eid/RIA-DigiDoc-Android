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
import ee.ria.DigiDoc.fragment.screen.SettingsRightsScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun SettingsRightsFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        SettingsRightsScreen(
            navController = navController,
            modifier = modifier,
            getIsScreenshotAllowed = sharedSettingsViewModel.dataStore::getSettingsAllowScreenshots,
            setIsScreenshotAllowed = sharedSettingsViewModel.dataStore::setSettingsAllowScreenshots,
            getIsOpenAllFileTypesEnabled = sharedSettingsViewModel.dataStore::getSettingsOpenAllFileTypes,
            setIsOpenAllFileTypesEnabled = sharedSettingsViewModel.dataStore::setSettingsOpenAllFileTypes,
            sharedSettingsViewModel = sharedSettingsViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRightsFragmentPreview() {
    val navController = rememberNavController()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    RIADigiDocTheme {
        SettingsRightsFragment(navController, sharedSettingsViewModel = sharedSettingsViewModel)
    }
}
