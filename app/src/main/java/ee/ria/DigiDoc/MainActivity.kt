@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.fragment.HomeFragment
import ee.ria.DigiDoc.fragment.MenuFragment
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RIADigiDocTheme {
                RIADigiDocAppScreen()
            }
        }
    }
}

@Composable
fun RIADigiDocAppScreen() {
    val navController = rememberNavController()
    val navBarNavController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
    ) {
        composable(route = Route.Home.route) {
            HomeFragment(
                navController = navController,
                navBarNavController = navBarNavController,
            )
        }
        composable(route = Route.Menu.route) { backStackEntry ->
            MenuFragment(
                navController = navController,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RIADigiDocAppScreenPreview() {
    RIADigiDocTheme {
        RIADigiDocAppScreen()
    }
}
