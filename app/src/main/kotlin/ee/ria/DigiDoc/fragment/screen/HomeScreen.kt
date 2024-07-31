@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.component.HomeNavigation
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    onClickToMenuScreen: () -> Unit = {},
    onClickToFileChoosingScreen: () -> Unit = {},
    onClickToRecentDocumentsScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    HomeNavigation(
        modifier = modifier,
        navController = navController,
        onClickMenu = onClickToMenuScreen,
        onClickToFileChoosingScreen = onClickToFileChoosingScreen,
        onClickToRecentDocumentsScreen = onClickToRecentDocumentsScreen,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        HomeScreen(
            navController = navController,
        )
    }
}
