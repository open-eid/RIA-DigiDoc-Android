@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.HomeScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    externalFileUris: List<Uri>,
) {
    LaunchedEffect(Unit) {
        if (externalFileUris.isNotEmpty()) {
            navController.navigate(Route.FileChoosing.route) {
                popUpTo(Route.Home.route) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("homeFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        HomeScreen(
            modifier = modifier,
            navController = navController,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeFragmentPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        HomeFragment(
            navController = navController,
            externalFileUris = listOf(),
        )
    }
}
