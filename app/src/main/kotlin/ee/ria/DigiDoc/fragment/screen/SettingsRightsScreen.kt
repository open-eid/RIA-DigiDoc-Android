@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsRightsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedSettingsViewModel: SharedSettingsViewModel,
    getIsScreenshotAllowed: () -> Boolean = { false },
    setIsScreenshotAllowed: (Boolean) -> Unit = {},
    getIsOpenAllFileTypesEnabled: () -> Boolean = { true },
    setIsOpenAllFileTypesEnabled: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val settingValueChanged = stringResource(id = R.string.setting_value_changed)
    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("settingsRightsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_settings_rights,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .testTag("scrollView"),
        ) {
            var checkedOpenAllFileTypes by remember { mutableStateOf(getIsOpenAllFileTypesEnabled()) }
            SettingsSwitchItem(
                testTag = "mainSettingsOpenAllFileTypes",
                modifier = modifier,
                checked = checkedOpenAllFileTypes,
                onCheckedChange = {
                    checkedOpenAllFileTypes = it
                    setIsOpenAllFileTypesEnabled(it)
                    AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, settingValueChanged)
                    sharedSettingsViewModel.recreateActivity()
                },
                title = stringResource(id = R.string.main_settings_open_all_filetypes_title),
                contentDescription = stringResource(id = R.string.main_settings_open_all_filetypes_title).lowercase(),
            )
            var checkedAllowScreenshots by remember { mutableStateOf(getIsScreenshotAllowed()) }
            SettingsSwitchItem(
                testTag = "mainSettingsAllowScreenshots",
                modifier = modifier,
                checked = checkedAllowScreenshots,
                onCheckedChange = {
                    checkedAllowScreenshots = it
                    setIsScreenshotAllowed(it)
                    AccessibilityUtil.sendAccessibilityEvent(context, TYPE_ANNOUNCEMENT, settingValueChanged)
                    sharedSettingsViewModel.recreateActivity()
                },
                title = stringResource(id = R.string.main_settings_allow_screenshots_title),
                contentDescription = stringResource(id = R.string.main_settings_allow_screenshots_title).lowercase(),
            )
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRightsScreenPreview() {
    RIADigiDocTheme {
        SettingsRightsScreen(
            navController = rememberNavController(),
            sharedSettingsViewModel = hiltViewModel(),
        )
    }
}
