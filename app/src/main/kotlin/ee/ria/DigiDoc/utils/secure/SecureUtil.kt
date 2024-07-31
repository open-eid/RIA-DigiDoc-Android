@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.secure

import android.content.Context
import android.content.SharedPreferences
import android.view.Window
import android.view.WindowManager
import androidx.preference.PreferenceManager
import com.takisoft.preferencex.BuildConfig
import ee.ria.DigiDoc.R

object SecureUtil {
    fun markAsSecure(
        context: Context,
        window: Window,
    ) {
        if (shouldMarkAsSecure(context)) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE,
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun shouldMarkAsSecure(context: Context): Boolean {
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)
        val isScreenshotAllowed =
            sharedPreferences.getBoolean(
                context.getString(R.string.main_settings_allow_screenshots_key),
                false,
            )
        if (BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            return false
        }
        return !isScreenshotAllowed
    }
}
