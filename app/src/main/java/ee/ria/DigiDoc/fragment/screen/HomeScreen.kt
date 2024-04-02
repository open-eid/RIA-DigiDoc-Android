import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun HomeScreen() {
    HomeNavigation()
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    RIADigiDocTheme {
        HomeScreen()
    }
}