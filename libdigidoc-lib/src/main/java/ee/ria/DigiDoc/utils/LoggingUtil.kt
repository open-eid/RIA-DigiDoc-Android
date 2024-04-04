@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import android.util.Log
import ee.ria.DigiDoc.libdigidoclib.BuildConfig

// TODO: Move to common module and / or use logging framework
object LoggingUtil {
    fun errorLog(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (BuildConfig.DEBUG) {
            throwable?.let {
                Log.e(tag, message, it)
            } ?: Log.e(tag, message)
        }
    }

    fun debugLog(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (BuildConfig.DEBUG) {
            throwable?.let {
                Log.d(tag, message, it)
            } ?: Log.d(tag, message)
        }
    }
}
