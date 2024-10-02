@file:Suppress("PackageName")

package ee.ria.DigiDoc

import android.content.Context
import android.widget.Toast
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.utils.TSLUtil
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibrarySetup
    @Inject
    constructor(
        private val initialization: Initialization,
        private val configurationLoader: ConfigurationLoader,
    ) {
        private val logTag = "LibrarySetup"

        suspend fun setupLibraries(
            context: Context,
            isLoggingEnabled: Boolean,
        ) {
            try {
                TSLUtil.setupTSLFiles(context)
                configurationLoader.initConfiguration(context)
            } catch (ex: Exception) {
                if (ex !is UnknownHostException && ex !is SocketTimeoutException) {
                    errorLog(logTag, "Unable to initialize configuration", ex)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.configuration_initialization_failed,
                            Toast.LENGTH_LONG,
                        )
                            .show()
                    }
                }
            }

            try {
                initialization.init(context, isLoggingEnabled)
            } catch (e: Exception) {
                if (e !is AlreadyInitializedException) {
                    errorLog(logTag, "Unable to initialize libdigidocpp", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.libdigidocpp_initialization_failed,
                            Toast.LENGTH_LONG,
                        )
                            .show()
                    }
                }
            }
        }
    }
