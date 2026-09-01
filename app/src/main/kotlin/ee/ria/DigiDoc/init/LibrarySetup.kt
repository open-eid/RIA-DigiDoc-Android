/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.init

import android.content.Context
import android.widget.Toast
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.exception.ConfigurationSignatureValidationException
import ee.ria.DigiDoc.configuration.exception.PublicKeyNotFoundException
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.utils.TSLUtil
import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.exceptions.AlreadyInitializedException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.utils.locale.LocaleUtil
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibrarySetup
    @Inject
    constructor(
        private val initialization: Initialization,
        private val cryptoInitialization: CryptoInitialization,
        private val configurationLoader: ConfigurationLoader,
        private val dataStore: DataStore,
        private val libdigidocLibraryLoader: LibdigidocLibraryLoader,
        private val localeUtil: LocaleUtil,
    ) {
        private val logTag = "LibrarySetup"

        private val setupMutex = Mutex()
        private var isConfigured = false

        suspend fun setupLibraries(
            context: Context,
            isLoggingEnabled: Boolean,
        ) {
            val localizedContext = localeUtil.updateLocale(context, dataStore.getLocale() ?: getLocale("en"))

            libdigidocLibraryLoader.init(context)

            cryptoInitialization.init(isLoggingEnabled)

            setupMutex.withLock {
                if (!isConfigured) {
                    withContext(IO) {
                        try {
                            try {
                                TSLUtil.setupTSLFiles(context)
                            } catch (ex: Exception) {
                                errorLog(logTag, "Unable to set up the bundled TSL files", ex)
                            }
                            configurationLoader.initConfiguration(
                                context,
                                dataStore.getProxySetting(),
                                dataStore.getManualProxySettings(),
                            )
                            isConfigured = true
                        } catch (ex: ConfigurationSignatureValidationException) {
                            errorLog(logTag, "Configuration signature validation failed", ex)

                            val hasConfiguration = configurationLoader.getConfigurationFlow().value != null
                            isConfigured = hasConfiguration

                            withContext(Main) {
                                SnackBarManager.showMessage(
                                    localizedContext,
                                    if (hasConfiguration) {
                                        R.string.configuration_update_validation_failed
                                    } else {
                                        R.string.configuration_initialization_failed
                                    },
                                )
                            }
                        } catch (ex: PublicKeyNotFoundException) {
                            errorLog(logTag, "Bundled signing key unavailable", ex)

                            withContext(Main) {
                                SnackBarManager.showMessage(
                                    localizedContext,
                                    R.string.configuration_initialization_failed,
                                )
                            }
                        } catch (ce: CancellationException) {
                            throw ce
                        } catch (ex: Exception) {
                            errorLog(logTag, "Unable to initialize configuration", ex)

                            val isNetworkFailure =
                                ex is UnknownHostException ||
                                    ex is SocketTimeoutException ||
                                    ex is InterruptedIOException
                            if (isNetworkFailure) {
                                debugLog(
                                    logTag,
                                    "Configuration was not refreshed because the network is unreachable; " +
                                        "continuing with the currently cached configuration",
                                )
                            } else {
                                withContext(Main) {
                                    SnackBarManager.showMessage(
                                        localizedContext,
                                        R.string.configuration_initialization_failed,
                                    )
                                }
                            }
                        }

                        if (configurationLoader.getConfigurationFlow().value == null) {
                            try {
                                configurationLoader.loadLocalConfiguration(context)
                            } catch (ce: CancellationException) {
                                throw ce
                            } catch (fallbackFailure: Exception) {
                                errorLog(logTag, "Unable to load local configuration", fallbackFailure)
                            }
                        }
                    }
                }

                try {
                    initialization.init(context, isLoggingEnabled)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Exception) {
                    if (e is AlreadyInitializedException) {
                        debugLog(logTag, "libdigidocpp was already initialized")
                    } else {
                        errorLog(logTag, "Unable to initialize libdigidocpp", e)
                        withContext(Main) {
                            Toast
                                .makeText(
                                    localizedContext,
                                    R.string.libdigidocpp_initialization_failed,
                                    Toast.LENGTH_LONG,
                                ).show()
                        }
                    }
                }
            }
        }
    }
