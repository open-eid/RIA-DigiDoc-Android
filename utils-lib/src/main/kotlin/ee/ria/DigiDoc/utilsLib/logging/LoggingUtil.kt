@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.logging

import android.util.Log
import ee.ria.DigiDoc.utilsLib.BuildConfig

// TODO: Use logging framework?

interface Logging {
    fun errorLog(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun debugLog(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    fun infoLog(
        tag: String,
        message: String,
    )
}

object LoggingUtil : Logging {
    override fun errorLog(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (BuildConfig.DEBUG) {
            throwable?.let {
                Log.e(tag, message, it)
            } ?: Log.e(tag, message)
        }
    }

    override fun debugLog(
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        if (BuildConfig.DEBUG) {
            throwable?.let {
                Log.d(tag, message, it)
            } ?: Log.d(tag, message)
        }
    }

    override fun infoLog(
        tag: String,
        message: String,
    ) {
        Log.i(tag, message)
    }
}
