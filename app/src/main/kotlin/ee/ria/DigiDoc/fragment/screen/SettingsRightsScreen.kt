@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SettingsRightsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    getIsScreenshotAllowed: () -> Boolean = { false },
    setIsScreenshotAllowed: (Boolean) -> Unit = {},
    getIsOpenAllFileTypesEnabled: () -> Boolean = { true },
    setIsOpenAllFileTypesEnabled: (Boolean) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_rights,
                onBackButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding).verticalScroll(rememberScrollState()),
        ) {
            var checkedOpenAllFileTypes by remember { mutableStateOf(getIsOpenAllFileTypesEnabled()) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedOpenAllFileTypes,
                onCheckedChange = {
                    checkedOpenAllFileTypes = it
                    setIsOpenAllFileTypesEnabled(it)
                },
                title = stringResource(id = R.string.main_settings_open_all_filetypes_title),
                contentDescription = stringResource(id = R.string.main_settings_open_all_filetypes_title).lowercase(),
            )
            var checkedAllowScreenshots by remember { mutableStateOf(getIsScreenshotAllowed()) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedAllowScreenshots,
                onCheckedChange = {
                    checkedAllowScreenshots = it
                    setIsScreenshotAllowed(it)
                },
                title = stringResource(id = R.string.main_settings_allow_screenshots_title),
                contentDescription = stringResource(id = R.string.main_settings_allow_screenshots_title).lowercase(),
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRightsScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        SettingsRightsScreen(
            navController = navController,
        )
    }
}
