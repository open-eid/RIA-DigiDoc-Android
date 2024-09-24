@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.fragment.RootFragment
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.root.RootChecker
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

    @Inject
    lateinit var fileTypeSetup: FileTypeSetup

    @Inject
    lateinit var activityManager: ActivityManager

    @Inject
    lateinit var rootChecker: RootChecker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (rootChecker.isRooted()) {
            setContent {
                RIADigiDocTheme {
                    RootFragment()
                }
            }
            return
        }
        val componentClassName = this.javaClass.name
        val externalFileUri = intent?.data

        val locale = dataStore.getLocale()
        if (locale != null) {
            Locale.setDefault(locale)
            val config = resources.configuration
            config.setLocale(locale)

            createConfigurationContext(config)
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        // Observe if activity needs to be recreated for changes to take effect (eg. Settings)
        activityManager.shouldRecreateActivity.observe(this) { shouldRecreate ->
            if (shouldRecreate) {
                activityManager.setShouldRecreateActivity(false)
                activityManager.recreateActivity(this)
            }
        }

        Firebase.crashlytics.isCrashlyticsCollectionEnabled = false

        lifecycleScope.launch {
            fileTypeSetup.initializeApplicationFileTypesAssociation(componentClassName)
            librarySetup.setupLibraries(applicationContext)
        }
        setContent {
            RIADigiDocTheme {
                RIADigiDocAppScreen(externalFileUri)
            }
        }
    }
}
