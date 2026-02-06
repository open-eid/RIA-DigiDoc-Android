/*
 * Copyright 2017 - 2025 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.AndroidEntryPoint
import ee.ria.DigiDoc.common.model.AppState
import ee.ria.DigiDoc.domain.model.theme.ThemeSetting
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.fragment.RootFragment
import ee.ria.DigiDoc.init.LibrarySetup
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.root.RootChecker
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.locale.LocaleUtil
import ee.ria.DigiDoc.utils.locale.LocaleUtilImpl
import ee.ria.DigiDoc.utils.secure.SecureUtil
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_key
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_running_key
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getExternalFileUris
import ee.ria.DigiDoc.utilsLib.file.FileUtil.getLogsDirectory
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity :
    ComponentActivity(),
    DefaultLifecycleObserver {
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
    lateinit var localeUtil: LocaleUtil

    @Inject
    lateinit var loggingUtil: LoggingUtil

    @Inject
    lateinit var secureUtil: SecureUtil

    private var isAppReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super<ComponentActivity>.onCreate(savedInstanceState)

        val isDebug = BuildConfig.BUILD_TYPE.contentEquals("debug")

        if (!isDebug) {
            secureUtil.markAsSecure(this)
        }

        installSplashScreen().setKeepOnScreenCondition {
            !isAppReady
        }

        enableEdgeToEdge()

        val useDarkMode =
            if (isSystemModeEnabled(dataStore) == true) {
                null
            } else {
                isDarkModeEnabled(dataStore)
            }

        if (rootChecker.isRooted()) {
            setContent {
                RIADigiDocTheme(darkTheme = useDarkMode) {
                    RootFragment()
                }
            }
            return
        }

        lifecycle.addObserver(this)

        val componentClassName = this.javaClass.name

        val locale = dataStore.getLocale() ?: getLocale("en")
        val webEidUri = intent?.data?.takeIf { it.scheme == "web-eid-mobile" }

        val externalFileUris =
            if (webEidUri != null) {
                listOf()
            } else {
                getExternalFileUris(intent)
            }

        localeUtil.updateLocale(applicationContext, locale)

        // Observe if activity needs to be recreated for changes to take effect (eg. Settings)
        activityManager.shouldRecreateActivity.observe(this) { shouldRecreate ->
            if (shouldRecreate) {
                activityManager.setShouldRecreateActivity(false)
                activityManager.recreateActivity(this)
            }
        }

        Firebase.crashlytics.isCrashlyticsCollectionEnabled = false

        activityManager.shouldResetLogging.observe(this) { shouldResetLogging ->
            if (shouldResetLogging && !isDebug) {
                activityManager.setShouldResetLogging(false)
                loggingUtil.handleOneTimeLogging(this)
                LoggingUtil.resetLogs(getLogsDirectory(this))
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val isDiagnosticsLoggingEnabled = sharedPreferences.getBoolean(getString(main_diagnostics_logging_key), false)
        val isDiagnosticsLoggingRunning =
            sharedPreferences.getBoolean(
                getString(main_diagnostics_logging_running_key),
                false,
            )
        val isLoggingEnabled = isDebug || (isDiagnosticsLoggingEnabled && isDiagnosticsLoggingRunning)
        lifecycleScope.launch {
            LoggingUtil.initialize(
                applicationContext,
                Logger.getLogger(MainActivity::class.java.name),
                isLoggingEnabled,
            )
            fileTypeSetup.initializeApplicationFileTypesAssociation(componentClassName)
            librarySetup.setupLibraries(applicationContext, isLoggingEnabled)

            isAppReady = true

            setContent {
                RIADigiDocTheme(darkTheme = useDarkMode) {
                    RIADigiDocAppScreen(
                        externalFileUris = externalFileUris,
                        webEidUri = webEidUri,
                    )
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        // Instantiating here manually, as "attachBaseContext" runs before Hilt
        val localeUtilImpl = LocaleUtilImpl()
        val language = localeUtilImpl.getPreferredLanguage(newBase)
        val localizedContext = newBase?.let { localeUtilImpl.updateLocale(it, getLocale(language)) }
        super.attachBaseContext(localizedContext)
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        AppState.isAppInForeground = true
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        AppState.isAppInForeground = false
    }

    private fun isSystemModeEnabled(dataStore: DataStore): Boolean = dataStore.getThemeSetting() == ThemeSetting.SYSTEM

    private fun isDarkModeEnabled(dataStore: DataStore): Boolean = dataStore.getThemeSetting() == ThemeSetting.DARK
}
