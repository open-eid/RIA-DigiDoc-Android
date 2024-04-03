import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dark
import ee.ria.DigiDoc.ui.theme.Normal
import ee.ria.DigiDoc.ui.theme.Primary500
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Transparent
import ee.ria.DigiDoc.ui.theme.White
import ee.ria.DigiDoc.utils.Route

@Composable
fun HomeNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onClickMenu: () -> Unit = {}
) {
    var navigationSelectedItem by remember {
        mutableStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeToolbar(
                modifier = modifier,
                onClickMenu = onClickMenu
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Primary500) {
                HomeNavigationItem().bottomNavigationItems().forEachIndexed {index,navigationItem ->
                    NavigationBarItem(
                        colors = NavigationBarItemColors(
                            selectedIconColor = White,
                            selectedTextColor = White,
                            selectedIndicatorColor = Transparent,
                            unselectedIconColor = Normal,
                            unselectedTextColor = Normal,
                            disabledIconColor = Dark,
                            disabledTextColor = Dark
                        ),
                        selected = index == navigationSelectedItem,
                        label = {
                            Text(navigationItem.label)
                        },
                        icon = {
                            Icon(
                                navigationItem.icon,
                                contentDescription = navigationItem.contentDescription
                            )
                        },
                        onClick = {
                            navigationSelectedItem = index
                            navController.navigate(navigationItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Signature.route,
            modifier = Modifier.padding(paddingValues = paddingValues)) {
            composable(Route.Signature.route) {
                SignatureScreen(navController = navController)
            }
            composable(Route.Crypto.route) {
                CryptoScreen(navController = navController)
            }
            composable(Route.eID.route) {
                MyEIDScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProductCardPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        HomeNavigation(navController)
    }
}