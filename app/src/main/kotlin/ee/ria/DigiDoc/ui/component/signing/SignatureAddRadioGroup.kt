@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.RadioButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

@Composable
fun SignatureAddRadioGroup(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    selectedRadioItem: String,
    sharedSettingsViewModel: SharedSettingsViewModel,
) {
    var selectedItem by remember { mutableStateOf(selectedRadioItem) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier =
            modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    horizontal = screenViewLargePadding,
                    vertical = screenViewExtraLargePadding,
                )
                .testTag("signatureUpdateSignatureAddMethod"),
    ) {
        SignatureAddRadioItem().radioItems().forEachIndexed { _, navigationItem ->
            RadioButton(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .testTag(navigationItem.testTag),
                selected = navigationItem.route == selectedItem,
                label = navigationItem.label,
                contentDescription =
                    if (navigationItem.route == selectedItem) {
                        "${navigationItem.contentDescription} ${stringResource(
                            id = R.string.signature_method_selected,
                        )}"
                    } else {
                        "${stringResource(id = R.string.signature_method)} ${navigationItem.contentDescription}"
                    },
                onClick = {
                    selectedItem = navigationItem.route
                    sharedSettingsViewModel.dataStore.setSignatureAddMethod(selectedItem)
                    navController.navigate(navigationItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureAddRadioGroupPreview() {
    val navController = rememberNavController()
    val sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel()
    RIADigiDocTheme {
        val selected by remember { mutableStateOf(Route.SmartId.route) }
        SignatureAddRadioGroup(
            selectedRadioItem = selected,
            navController = navController,
            sharedSettingsViewModel = sharedSettingsViewModel,
        )
    }
}
