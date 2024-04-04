@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilslib.logging

import android.util.Log
import ee.ria.utilslib.BuildConfig

// TODO: Use logging framework?
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
