@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRightsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    someList: List<SomeObject>? = listOf(SomeObject()),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text(
                        text = stringResource(id = R.string.main_settings_rights),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClickBack = {
                            navController.navigateUp()
                        },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding).verticalScroll(rememberScrollState()),
        ) {
            var checkedOpenAllFileTypes by remember { mutableStateOf(true) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedOpenAllFileTypes,
                onCheckedChange = {
                    checkedOpenAllFileTypes = it
                },
                title = stringResource(id = R.string.main_settings_open_all_filetypes_title),
                contentDescription = stringResource(id = R.string.main_settings_open_all_filetypes_title).lowercase(),
            )
            var checkedAllowScreenshots by remember { mutableStateOf(false) }
            SettingsSwitchItem(
                modifier = modifier,
                checked = checkedAllowScreenshots,
                onCheckedChange = {
                    checkedAllowScreenshots = it
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
