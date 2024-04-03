package ee.ria.DigiDoc.fragment

import HomeScreen
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@Composable
fun HomeFragment(
    navController: NavHostController,
    navBarNavController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background
    ) {
        HomeScreen(
            modifier,
            navBarNavController,
            onClickToMenuScreen = {
                navController.navigate(
                    Route.Menu.route
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeFragmentPreview() {
    val navController = rememberNavController()
    val navBarController = rememberNavController()
    RIADigiDocTheme {
        HomeFragment(navController, navBarController)
    }
}