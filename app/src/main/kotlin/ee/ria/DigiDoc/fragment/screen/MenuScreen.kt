@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.ui.component.MenuItem
import ee.ria.DigiDoc.ui.component.ToolbarScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    someObject: SomeObject? = null,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ToolbarScreen(
                modifier = modifier.fillMaxWidth(),
                onClickBack = {
                    navController.navigateUp()
                },
                title = "",
            )
        },
    ) { paddingValues ->
        Column(
            modifier = modifier.padding(paddingValues = paddingValues).fillMaxWidth(),
        ) {
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_help_outline),
                title = stringResource(id = R.string.main_home_menu_help),
                contentDescription = stringResource(id = R.string.main_home_menu_help_accessibility),
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_accessibility),
                title = stringResource(id = R.string.main_home_menu_accessibility),
                contentDescription = stringResource(id = R.string.main_home_menu_accessibility_accessibility),
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                title = stringResource(id = R.string.main_home_menu_settings),
                contentDescription = stringResource(id = R.string.main_home_menu_settings_accessibility),
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_info_outline),
                title = stringResource(id = R.string.main_home_menu_about),
                contentDescription = stringResource(id = R.string.main_home_menu_about_accessibility),
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_diagnostics),
                title = stringResource(id = R.string.main_home_menu_diagnostics),
                contentDescription = stringResource(id = R.string.main_home_menu_diagnostics_accessibility),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuScreenPreview() {
    RIADigiDocTheme {
        val navController = rememberNavController()
        MenuScreen(
            navController = navController,
        )
    }
}
