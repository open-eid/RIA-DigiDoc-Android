@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.logging

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_key
import ee.ria.DigiDoc.utilsLib.R.string.main_diagnostics_logging_running_key
import ee.ria.DigiDoc.utilsLib.date.DateUtil.dateFormat
import ee.ria.DigiDoc.utilsLib.date.DateUtil.dateTimeFormat
import ee.ria.DigiDoc.utilsLib.file.FileUtil
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.TimeZone
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

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

@Singleton
class LoggingUtil
    @Inject
    constructor() {
        companion object : Logging {
            private const val LOG_TAG: String = "LoggingUtil"

            private lateinit var logger: Logger
            private var fileHandler: FileHandler? = null
            private var isLoggingEnabled: Boolean = false

            fun initialize(
                context: Context,
                appLogger: Logger,
                loggingEnabled: Boolean,
            ) {
                isLoggingEnabled = loggingEnabled

                if (isLoggingEnabled) {
                    logger = appLogger
                    setupLogger(context)
                }
            }

            fun resetLogs(logDirectory: File) {
                try {
                    fileHandler?.close()
                } catch (se: SecurityException) {
                    Log.e(LOG_TAG, "Unable to close logging FileHandler", se)
                }
                if (logDirectory.exists()) {
                    logDirectory.deleteRecursively()
                }

                if (!logDirectory.exists()) {
                    logDirectory.mkdirs()
                }
            }

            private fun setupLogger(context: Context) {
                try {
                    val logDirectory = FileUtil.getLogsDirectory(context)
                    val logFile = File(logDirectory, "${dateFormat.format(Date())}.log")

                    resetLogs(logDirectory)

                    fileHandler = FileHandler(logFile.absolutePath, Int.MAX_VALUE, 1, true)
                    fileHandler?.formatter = LogFormatter()

                    fileHandler?.let { logger.addHandler(it) }

                    val consoleHandler = ConsoleHandler()
                    consoleHandler.formatter = LogFormatter()
                    logger.addHandler(consoleHandler)

                    logger.level = java.util.logging.Level.ALL
                    fileHandler?.level = java.util.logging.Level.ALL
                    consoleHandler.level = java.util.logging.Level.ALL
                } catch (ioe: IOException) {
                    Log.e("LoggingUtil", "Cannot setup logging", ioe)
                }
            }

            fun format(record: LogRecord): String {
                val timestamp = dateTimeFormat.format(Date(record.millis))
                val timeZone = TimeZone.getDefault().id
                val fileName = record.sourceClassName
                val message = record.message
                return "$timestamp $timeZone, $fileName, $message\n"
            }

            override fun errorLog(
                tag: String,
                message: String,
                throwable: Throwable?,
            ) {
                if (isLoggingEnabled) {
                    throwable?.let {
                        logger.severe("$tag: $message: ${it.localizedMessage}")
                    } ?: logger.severe("$tag: $message")
                }
            }

            override fun debugLog(
                tag: String,
                message: String,
                throwable: Throwable?,
            ) {
                if (isLoggingEnabled) {
                    throwable?.let {
                        logger.fine("$tag: $message: ${it.localizedMessage}")
                    } ?: logger.fine("$tag: $message")
                }
            }

            override fun infoLog(
                tag: String,
                message: String,
            ) {
                if (isLoggingEnabled) {
                    logger.info("$tag: $message")
                }
            }
        }

        private fun isDiagnosticsLoggingEnabled(context: Context): Boolean {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getBoolean(
                context.getString(main_diagnostics_logging_key),
                false,
            )
        }

        private fun isDiagnosticsLoggingRunning(context: Context): Boolean {
            val sharedPreferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getBoolean(
                context.getString(main_diagnostics_logging_running_key),
                false,
            )
        }

        fun handleOneTimeLogging(context: Context) {
            val preferences: SharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            if (isDiagnosticsLoggingEnabled(context) &&
                isDiagnosticsLoggingRunning(context)
            ) {
                editor.putBoolean(context.resources.getString(main_diagnostics_logging_key), false).apply()
                editor.putBoolean(context.resources.getString(main_diagnostics_logging_running_key), false).apply()
            } else if (isDiagnosticsLoggingEnabled(context)) {
                editor.putBoolean(context.resources.getString(main_diagnostics_logging_running_key), true).apply()
            }
        }
    }

class LogFormatter : Formatter() {
    override fun format(record: LogRecord): String {
        val timestamp = dateTimeFormat.format(Date(record.millis))
        val message = record.message
        return "$timestamp, $message\n"
    }
}
