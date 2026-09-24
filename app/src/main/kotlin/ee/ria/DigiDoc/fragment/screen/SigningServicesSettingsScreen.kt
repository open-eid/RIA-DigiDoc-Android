// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.advanced.signingservices.LTAComponent
import ee.ria.DigiDoc.ui.component.settings.advanced.signingservices.MobileIdAndSmartIdServicesComponent
import ee.ria.DigiDoc.ui.component.settings.advanced.signingservices.TimestampServicesComponent
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TabItem
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SigningServicesSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
) {
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val selectedSigningServiceTabIndex = rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("advancedSettingsScreen"),
        snackbarHost = { StatusSnackbarHost() },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_settings_signing_services_title,
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
                    .verticalScroll(rememberScrollState())
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("scrollView"),
        ) {
            TabView(
                modifier = modifier,
                testTag = "signingServicesSettingsTabView",
                selectedTabIndex = selectedSigningServiceTabIndex.intValue,
                onTabSelected = { index -> selectedSigningServiceTabIndex.intValue = index },
                listOf(
                    TabItem(
                        stringResource(R.string.main_settings_timestamp_services_title),
                    ) {
                        TimestampServicesComponent(
                            modifier,
                            sharedSettingsViewModel = sharedSettingsViewModel,
                            sharedCertificateViewModel = sharedCertificateViewModel,
                            navController = navController,
                        )
                    },
                    TabItem(
                        stringResource(R.string.main_settings_mobile_id_and_smart_id_title),
                    ) {
                        MobileIdAndSmartIdServicesComponent(
                            modifier,
                            sharedSettingsViewModel = sharedSettingsViewModel,
                        )
                    },
                    TabItem(
                        stringResource(R.string.main_settings_default_lta_tab_title),
                    ) {
                        LTAComponent(
                            modifier,
                            sharedSettingsViewModel = sharedSettingsViewModel,
                        )
                    },
                ),
            )

            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningServicesSettingsScreenPreview() {
    RIADigiDocTheme {
        SigningServicesSettingsScreen(
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
            sharedCertificateViewModel = hiltViewModel(),
            navController = rememberNavController(),
        )
    }
}
