@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions.radioButtonHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.radioGroupBarHeight
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SignatureAddRadioGroup(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    selectedRadioItem: Int = 0,
) {
    var selectedItem by remember { mutableIntStateOf(selectedRadioItem) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier =
            modifier.wrapContentHeight().fillMaxWidth().padding(
                horizontal = radioButtonHorizontalPadding,
                vertical = zeroPadding,
            ),
    ) {
        SignatureAddRadioItem().radioItems().forEachIndexed { index, navigationItem ->
            SignatureAddRadioButton(
                modifier =
                    modifier.height(radioGroupBarHeight).semantics {
                        contentDescription = navigationItem.contentDescription
                    }.fillMaxWidth(),
                selected = index == selectedItem,
                label = navigationItem.label,
                onClick = {
                    selectedItem = index
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
    RIADigiDocTheme {
        val selected by remember { mutableIntStateOf(1) }
        SignatureAddRadioGroup(
            selectedRadioItem = selected,
            navController = navController,
        )
    }
}
