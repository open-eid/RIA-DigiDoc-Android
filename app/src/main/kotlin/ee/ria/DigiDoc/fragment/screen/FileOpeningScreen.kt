@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.common.model.FileOpeningMethod
import ee.ria.DigiDoc.ui.component.signing.FileOpeningNavigation
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel

@Composable
fun FileOpeningScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedContainerViewModel: SharedContainerViewModel,
    fileOpeningMethod: FileOpeningMethod,
) {
    FileOpeningNavigation(
        modifier = modifier,
        navController = navController,
        sharedContainerViewModel = sharedContainerViewModel,
        fileOpeningMethod = fileOpeningMethod,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FileOpeningScreenPreview() {
    RIADigiDocTheme {
        FileOpeningScreen(
            navController = rememberNavController(),
            sharedContainerViewModel = hiltViewModel(),
            fileOpeningMethod = FileOpeningMethod.ALL,
        )
    }
}
