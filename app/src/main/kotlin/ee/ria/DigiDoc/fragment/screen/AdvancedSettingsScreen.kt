@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.advanced.AdvancedSettingComponent
import ee.ria.DigiDoc.ui.component.settings.advanced.AdvancedSettingComponentItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AdvancedSettingsScreen(
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

    val getIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::getSettingsAskRoleAndAddress
    val setIsRoleAskingEnabled = sharedSettingsViewModel.dataStore::setSettingsAskRoleAndAddress

    var checkedAskRoleAndAddress by remember { mutableStateOf(getIsRoleAskingEnabled()) }

    val defaultSettingsButtonText = stringResource(R.string.main_settings_use_default_settings_button_title)
    val buttonName = stringResource(id = R.string.button_name)

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                modifier = modifier.padding(vertical = SPadding),
                hostState = snackBarHostState,
            )
        },
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("advancedSettingsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_menu_signing,
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
            isThirdButtonVisible = false,
        )

        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState())
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("scrollView"),
        ) {
            Text(
                text = stringResource(R.string.main_settings_general_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = modifier.height(XSPadding))
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clickable {
                            checkedAskRoleAndAddress = !checkedAskRoleAndAddress
                            setIsRoleAskingEnabled(checkedAskRoleAndAddress)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.main_settings_ask_role_and_address_title),
                    modifier = modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Checkbox(
                    checked = checkedAskRoleAndAddress,
                    onCheckedChange = {
                        checkedAskRoleAndAddress = it
                        setIsRoleAskingEnabled(it)
                    },
                )
            }
            HorizontalDivider(modifier = modifier.padding(vertical = MPadding))

            Text(
                text = stringResource(R.string.main_settings_system_settings_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = modifier.height(XSPadding))

            AdvancedSettingComponentItem().componentItems().forEach { item ->
                AdvancedSettingComponent(
                    modifier = modifier,
                    name = item.name,
                    testTag = item.testTag,
                ) {
                    navController.navigate(
                        item.route.route,
                    )
                }
            }

            Spacer(modifier = modifier.height(SPadding))

            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(vertical = SPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = {
                    checkedAskRoleAndAddress = false
                    sharedSettingsViewModel.resetToDefaultSettings()
                    showMessage(context, R.string.main_settings_use_default_settings_message)
                }) {
                    Text(
                        modifier =
                            modifier
                                .padding(vertical = SPadding)
                                .semantics {
                                    contentDescription =
                                        "$defaultSettingsButtonText $buttonName"
                                    testTagsAsResourceId = true
                                }
                                .testTag("mainSettingsProxyServicesCheckInternetConnectionButton"),
                        text = defaultSettingsButtonText,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AdvancedSettingsScreenPreview() {
    RIADigiDocTheme {
        AdvancedSettingsScreen(
            sharedMenuViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
