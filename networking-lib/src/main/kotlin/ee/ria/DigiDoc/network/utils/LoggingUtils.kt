
@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

fun isLoggingEnabled(context: Context): Boolean {
    val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)
    val isDiagnosticsLoggingEnabled =
        sharedPreferences.getBoolean(
            context.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_key),
            false,
        )
    val isDiagnosticsLoggingRunning =
        sharedPreferences.getBoolean(
            context.getString(ee.ria.DigiDoc.network.R.string.main_diagnostics_logging_running_key),
            false,
        )

    return isDiagnosticsLoggingEnabled && isDiagnosticsLoggingRunning
}
