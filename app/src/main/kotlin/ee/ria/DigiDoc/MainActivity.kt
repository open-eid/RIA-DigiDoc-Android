@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var librarySetup: LibrarySetup

    @Inject
    lateinit var dataStore: DataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val locale = dataStore.getLocale()
        if (locale != null) {
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)

            createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

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
