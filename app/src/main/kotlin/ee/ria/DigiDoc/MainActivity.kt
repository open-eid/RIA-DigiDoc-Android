@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var librarySetup: LibrarySetup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            librarySetup.setupLibraries(applicationContext)
        }
        setContent {
            RIADigiDocTheme {
                RIADigiDocAppScreen()
            }
        }
    }
}
