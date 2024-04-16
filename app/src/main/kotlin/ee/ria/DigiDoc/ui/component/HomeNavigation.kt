@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.CryptoScreen
import ee.ria.DigiDoc.fragment.screen.MyEIDScreen
import ee.ria.DigiDoc.fragment.screen.SignatureScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@Composable
fun HomeNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    signatureAddController: NavHostController,
    onClickMenu: () -> Unit = {},
    onClickToSigningScreen: () -> Unit = {},
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            HomeToolbar(
                modifier = modifier,
                onClickMenu = onClickMenu,
            )
        },
        bottomBar = {
            HomeNavigationBar(
                modifier = modifier,
                navController = navController,
            )
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Route.Signature.route,
            modifier = Modifier.padding(paddingValues = paddingValues),
        ) {
            composable(Route.Signature.route) {
                SignatureScreen(
                    navController = navController,
                    signatureAddController = signatureAddController,
                    onClickToSigningScreen = onClickToSigningScreen,
                    isDialogOpen = true,
                )
            }
            composable(Route.Crypto.route) {
                CryptoScreen(navController = navController)
            }
            composable(Route.EID.route) {
                MyEIDScreen(navController = navController)
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeNavigationPreview() {
    val navController = rememberNavController()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        HomeNavigation(
            navController = navController,
            signatureAddController = signatureAddController,
        )
    }
}
