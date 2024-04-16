@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.ui.component.signing.SigningNavigation
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SigningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    someList: List<SomeObject>? = listOf(SomeObject()),
) {
    SigningNavigation(
        modifier = modifier,
        navController = navController,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SigningScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        SigningScreen(
            navController = navController,
        )
    }
}
