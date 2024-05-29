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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.SettingsViewModel

@Composable
fun SignatureAddRadioGroup(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    selectedRadioItem: String,
    settingsViewModel: SettingsViewModel,
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
                ),
    ) {
        SignatureAddRadioItem().radioItems().forEachIndexed { _, navigationItem ->
            SignatureAddRadioButton(
                modifier =
                    modifier
                        .fillMaxWidth(),
                selected = navigationItem.route == selectedItem,
                label = navigationItem.label,
                contentDescription = navigationItem.contentDescription,
                onClick = {
                    selectedItem = navigationItem.route
                    settingsViewModel.dataStore.setSignatureAddMethod(selectedItem)
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
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    RIADigiDocTheme {
        val selected by remember { mutableStateOf(Route.SmartId.route) }
        SignatureAddRadioGroup(
            selectedRadioItem = selected,
            navController = navController,
            settingsViewModel = settingsViewModel,
        )
    }
}
