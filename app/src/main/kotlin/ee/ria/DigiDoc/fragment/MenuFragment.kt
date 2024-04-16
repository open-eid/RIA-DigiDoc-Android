@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.MenuScreen
import ee.ria.DigiDoc.ui.theme.Primary500
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.MenuViewModel

@Composable
fun MenuFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    menuViewModel: MenuViewModel = hiltViewModel(),
    id: Int = -1,
) {
    menuViewModel.getObject(id)
    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(Primary500),
        color = Primary500,
        contentColor = Primary500,
    ) {
        if (menuViewModel.someState.value.id != null) {
            MenuScreen(
                modifier = modifier,
                navController = navController,
                someObject = menuViewModel.someState.value,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuFragmentPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        MenuFragment(
            navController = navController,
        )
    }
}
