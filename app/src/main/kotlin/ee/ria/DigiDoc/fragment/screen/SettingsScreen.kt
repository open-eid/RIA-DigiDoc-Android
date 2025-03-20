@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("settingsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_title,
                onLeftButtonClick = {
                    navController.navigateUp()
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )
        Column(
            modifier =
                modifier
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .testTag("scrollView"),
        ) {
            SettingsItem(
                testTag = "mainSettingsSigningCategory",
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
                testTag = "mainSettingsRightsCategory",
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
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(
                        horizontal = screenViewLargePadding,
                    )
                    .testTag("mainSettingsUseDefaultSettings"),
                contentDescription =
                    stringResource(
                        id = R.string.main_settings_use_default_settings_button_title,
                    ).lowercase(),
                title = R.string.main_settings_use_default_settings_button_title,
                onClickItem = {
                    sharedSettingsViewModel.resetToDefaultSettings()
                    showMessage(context, R.string.main_settings_use_default_settings_message)
                },
            )
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    RIADigiDocTheme {
        SettingsScreen(
            sharedMenuViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
