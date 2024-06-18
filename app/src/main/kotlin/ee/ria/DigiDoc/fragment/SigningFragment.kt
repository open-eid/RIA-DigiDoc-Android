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
import ee.ria.DigiDoc.fragment.screen.SigningScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@Composable
fun SigningFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    Surface(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        SigningScreen(
            navController = navController,
            modifier = modifier,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedSignatureViewModel = sharedSignatureViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningFragmentPreview() {
    val navController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    val sharedSignatureViewModel: SharedSignatureViewModel = hiltViewModel()
    RIADigiDocTheme {
        SigningFragment(
            navController = navController,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedSignatureViewModel = sharedSignatureViewModel,
        )
    }
}
