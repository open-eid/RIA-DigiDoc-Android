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
import ee.ria.DigiDoc.fragment.screen.SettingsSigningScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsSigningFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedCertificateViewModel: SharedCertificateViewModel,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("settingsSigningFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        SettingsSigningScreen(
            navController = navController,
            modifier = modifier,
            sharedCertificateViewModel = sharedCertificateViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsSigningFragmentPreview() {
    RIADigiDocTheme {
        SettingsSigningFragment(
            navController = rememberNavController(),
            sharedCertificateViewModel = hiltViewModel(),
        )
    }
}
