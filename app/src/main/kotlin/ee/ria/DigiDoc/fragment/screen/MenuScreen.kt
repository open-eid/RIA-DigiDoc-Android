@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.LanguageSwitchRadioGroup
import ee.ria.DigiDoc.ui.component.menu.MenuItem
import ee.ria.DigiDoc.ui.component.menu.ToolbarScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import java.util.Locale

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val locale = sharedSettingsViewModel.dataStore.getLocale() ?: Locale.getDefault()

    val helpContentDesctiption =
        if (locale.language == "et") {
            stringResource(R.string.main_home_menu_help) +
                " link " +
                "w w w punkt i d punkt e e"
        } else {
            stringResource(R.string.main_home_menu_help) + " " +
                TextUtil.splitTextAndJoin(
                    stringResource(R.string.main_home_menu_help_url_short),
                    "",
                    " ",
                )
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            ToolbarScreen(
                modifier = modifier,
                onClickBack = {
                    navController.navigateUp()
                },
                title = "",
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                modifier
                    .padding(paddingValues = paddingValues)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
        ) {
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_help_outline),
                title = stringResource(id = R.string.main_home_menu_help),
                contentDescription = helpContentDesctiption,
                onClickItem = {
                    val browserIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.main_home_menu_help_url)))

                    ContextCompat.startActivity(context, browserIntent, null)
                },
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_accessibility),
                title = stringResource(id = R.string.main_home_menu_accessibility),
                contentDescription = stringResource(id = R.string.main_home_menu_accessibility_accessibility),
                onClickItem = {
                    navController.navigate(
                        Route.Accessibility.route,
                    )
                },
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                title = stringResource(id = R.string.main_home_menu_settings),
                contentDescription = stringResource(id = R.string.main_home_menu_settings_accessibility),
                onClickItem = {
                    navController.navigate(
                        Route.Settings.route,
                    )
                },
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_info_outline),
                title = stringResource(id = R.string.main_home_menu_about),
                contentDescription = stringResource(id = R.string.main_home_menu_about_accessibility),
                onClickItem = {
                    navController.navigate(
                        Route.Info.route,
                    )
                },
            )
            MenuItem(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_diagnostics),
                title = stringResource(id = R.string.main_home_menu_diagnostics),
                contentDescription = stringResource(id = R.string.main_home_menu_diagnostics_accessibility),
                onClickItem = {
                    navController.navigate(
                        Route.Diagnostics.route,
                    )
                },
            )

            LanguageSwitchRadioGroup(
                modifier = modifier,
                selectedRadioItem = locale.language,
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
