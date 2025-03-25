@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.EncryptionServicesSettingsScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EncryptionServicesSettingsFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("encryptionServicesSettingsFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        EncryptionServicesSettingsScreen(
            navController = navController,
            modifier = modifier,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedMenuViewModel = sharedMenuViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EncryptionServicesSettingsFragmentPreview() {
    RIADigiDocTheme {
        EncryptionServicesSettingsFragment(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
        )
    }
}
