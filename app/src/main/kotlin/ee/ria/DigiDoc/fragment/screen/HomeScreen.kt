@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.ui.component.HomeNavigation
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    signatureAddController: NavHostController,
    onClickToMenuScreen: () -> Unit = {},
    onClickToFileChoosingScreen: () -> Unit = {},
    someList: List<SomeObject>? = listOf(SomeObject()),
) {
    HomeNavigation(
        modifier = modifier,
        navController = navController,
        signatureAddController = signatureAddController,
        onClickMenu = onClickToMenuScreen,
        onClickToFileChoosingScreen = onClickToFileChoosingScreen,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        HomeScreen(
            navController = navController,
            signatureAddController = signatureAddController,
        )
    }
}
