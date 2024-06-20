@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import ee.ria.DigiDoc.ui.component.settings.SettingsItem
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_title,
                onBackButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
        ) {
            SettingsItem(
                modifier = modifier,
                onClickItem = {
                    navController.navigate(
                        Route.SettingsSigning.route,
                    )
                },
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_signing),
                title = stringResource(id = R.string.main_settings_signing),
                contentDescription = stringResource(id = R.string.main_settings_signing).lowercase(),
            )
            SettingsItem(
                modifier = modifier,
                onClickItem = {
                    navController.navigate(
                        Route.SettingsRights.route,
                    )
                },
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_icon_permissions),
                title = stringResource(id = R.string.main_settings_rights),
                contentDescription = stringResource(id = R.string.main_settings_rights).lowercase(),
            )
            PrimaryButton(
                modifier.fillMaxWidth().wrapContentHeight().padding(
                    horizontal = screenViewLargePadding,
                ),
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_use_default_settings_button_title,
                    ).lowercase(),
                title = R.string.main_settings_use_default_settings_button_title,
                onClickItem = { /* TODO */ },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        SettingsScreen(
            navController = navController,
        )
    }
}
