@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CryptoScreen(navController: NavController) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(screenViewLargePadding)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("cryptoScreen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Crypto Screen",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = screenViewLargePadding),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CryptoScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        CryptoScreen(navController)
    }
}
