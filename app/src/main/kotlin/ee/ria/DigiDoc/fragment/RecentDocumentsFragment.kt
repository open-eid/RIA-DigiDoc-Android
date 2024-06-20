@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.fragment.screen.RecentDocumentsScreen
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel

@Composable
fun RecentDocumentsFragment(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    Surface(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        color = MaterialTheme.colorScheme.background,
    ) {
        RecentDocumentsScreen(
            navController = navController,
            sharedContainerViewModel = sharedContainerViewModel,
            modifier = modifier,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecentDocumentsFragmentPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        RecentDocumentsFragment(
            navController = navController,
            sharedContainerViewModel = hiltViewModel(),
        )
    }
}
