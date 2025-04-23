@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.component.crypto.CryptoFileOpeningNavigation
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel

@Composable
fun CryptoFileOpeningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    CryptoFileOpeningNavigation(
        modifier = modifier,
        navController = navController,
        sharedContainerViewModel = sharedContainerViewModel,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CryptoFileOpeningScreenPreview() {
    RIADigiDocTheme {
        CryptoFileOpeningScreen(
            navController = rememberNavController(),
            sharedContainerViewModel = hiltViewModel(),
        )
    }
}
