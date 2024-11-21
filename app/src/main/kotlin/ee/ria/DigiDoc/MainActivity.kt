@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.common.model.AppState
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.fragment.RootFragment
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.root.RootChecker
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_key
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_running_key
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getExternalFileUris
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.logging.Logger
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), DefaultLifecycleObserver {
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

    @Inject
    lateinit var loggingUtil: LoggingUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ComponentActivity>.onCreate(savedInstanceState)

        if (rootChecker.isRooted()) {
            setContent {
                RIADigiDocTheme {
                    RootFragment()
                }
            }
            return
        }

        lifecycle.addObserver(this)

        val componentClassName = this.javaClass.name

        val externalFileUris = getExternalFileUris(intent)

        val locale = dataStore.getLocale() ?: Locale("en")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        val context = createConfigurationContext(config)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Observe if activity needs to be recreated for changes to take effect (eg. Settings)
        activityManager.shouldRecreateActivity.observe(this) { shouldRecreate ->
            if (shouldRecreate) {
                activityManager.setShouldRecreateActivity(false)
                activityManager.recreateActivity(this)
            }
        }

        Firebase.crashlytics.isCrashlyticsCollectionEnabled = false

        activityManager.shouldResetLogging.observe(this) { shouldResetLogging ->
            if (shouldResetLogging) {
                activityManager.setShouldResetLogging(false)
                loggingUtil.handleOneTimeLogging(this)
                LoggingUtil.resetLogs(FileUtil.getLogsDirectory(this))
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isDiagnosticsLoggingEnabled = sharedPreferences.getBoolean(getString(main_diagnostics_logging_key), false)
        val isDiagnosticsLoggingRunning =
            sharedPreferences.getBoolean(
                getString(main_diagnostics_logging_running_key),
                false,
            )
        val isLoggingEnabled = BuildConfig.DEBUG || (isDiagnosticsLoggingEnabled && isDiagnosticsLoggingRunning)
        lifecycleScope.launch {
            LoggingUtil.initialize(
                applicationContext,
                Logger.getLogger(MainActivity::class.java.name),
                isLoggingEnabled,
            )
            fileTypeSetup.initializeApplicationFileTypesAssociation(componentClassName)
            librarySetup.setupLibraries(context, isLoggingEnabled)
        }
        setContent {
            RIADigiDocTheme {
                RIADigiDocAppScreen(externalFileUris)
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        AppState.isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        AppState.isAppInForeground = false
    }
}
