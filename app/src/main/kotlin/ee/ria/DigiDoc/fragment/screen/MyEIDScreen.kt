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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun MyEIDScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(textVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "My eID Screen",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = textVerticalPadding),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyEIDScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        MyEIDScreen(navController)
    }
}
