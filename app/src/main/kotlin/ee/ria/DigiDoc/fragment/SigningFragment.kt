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
import ee.ria.DigiDoc.viewmodel.SharedContainerViewModel

@Composable
fun SigningFragment(
    navController: NavHostController,
    signatureAddController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    Surface(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        SigningScreen(
            navController = navController,
            signatureAddController = signatureAddController,
            modifier = modifier,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningFragmentPreview() {
    val navController = rememberNavController()
    val signatureAddController = rememberNavController()
    val sharedContainerViewModel: SharedContainerViewModel = hiltViewModel()
    RIADigiDocTheme {
        SigningFragment(
            navController = navController,
            signatureAddController = signatureAddController,
            sharedContainerViewModel = sharedContainerViewModel,
        )
    }
}
