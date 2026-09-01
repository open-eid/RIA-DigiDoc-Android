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
import android.content.res.AssetManager
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.configuration.exception.ConfigurationSignatureValidationException
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utils.locale.LocaleUtil
import ee.ria.DigiDoc.utilsLib.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.net.UnknownHostException
import java.nio.file.Files

@OptIn(ExperimentalCoroutinesApi::class)
class LibrarySetupTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var localizedContext: Context
    private lateinit var configurationLoader: ConfigurationLoader
    private lateinit var initialization: Initialization
    private lateinit var librarySetup: LibrarySetup

    @Before
    fun setUp() {
        val assets = mock<AssetManager>()
        whenever(assets.list("tslFiles")).thenReturn(emptyArray())

        context = mock()
        whenever(context.cacheDir).thenReturn(Files.createTempDirectory("librarySetupTest").toFile())
        whenever(context.assets).thenReturn(assets)

        localizedContext = mock()
        whenever(localizedContext.getString(any())).thenReturn("message")

        val dataStore = mock<DataStore>()
        whenever(dataStore.getProxySetting()).thenReturn(ProxySetting.NO_PROXY)
        whenever(dataStore.getManualProxySettings()).thenReturn(ManualProxy("", 80, "", ""))

        val localeUtil = mock<LocaleUtil>()
        whenever(localeUtil.updateLocale(any(), any())).thenReturn(localizedContext)

        configurationLoader = mock()

        initialization = mock()

        librarySetup =
            LibrarySetup(
                initialization,
                mock<CryptoInitialization>(),
                configurationLoader,
                dataStore,
                mock<LibdigidocLibraryLoader>(),
                localeUtil,
            )
    }

    @Test
    fun librarySetup_setupLibraries_showsValidationMessageWhenCentralConfigurationCannotBeValidated() =
        runTest {
            givenLoadedConfiguration(true)
            givenInitConfigurationFails(ConfigurationSignatureValidationException())

            librarySetup.setupLibraries(context, false)

            verify(localizedContext).getString(R.string.configuration_update_validation_failed)
            verify(localizedContext, never()).getString(R.string.configuration_initialization_failed)
            verify(context, never()).getString(any())
        }

    @Test
    fun librarySetup_setupLibraries_showsInitializationFailedMessageWhenNoConfigurationLoaded() =
        runTest {
            givenLoadedConfiguration(false)
            givenInitConfigurationFails(ConfigurationSignatureValidationException())

            librarySetup.setupLibraries(context, false)

            verify(localizedContext).getString(R.string.configuration_initialization_failed)
            verify(localizedContext, never()).getString(R.string.configuration_update_validation_failed)
        }

    @Test
    fun librarySetup_setupLibraries_showsNoMessageWhenNetworkIsUnavailable() =
        runTest {
            givenLoadedConfiguration(true)
            givenInitConfigurationFails(UnknownHostException())

            librarySetup.setupLibraries(context, false)

            verify(localizedContext, never()).getString(any())
        }

    @Test
    fun librarySetup_setupLibraries_showsInitializationFailedMessageWhenConfigurationFailsForOtherReason() =
        runTest {
            givenLoadedConfiguration(true)
            givenInitConfigurationFails(IllegalStateException("broken"))

            librarySetup.setupLibraries(context, false)

            verify(localizedContext).getString(R.string.configuration_initialization_failed)
            verify(localizedContext, never()).getString(R.string.configuration_update_validation_failed)
        }

    @Test
    fun librarySetup_setupLibraries_doesNotRetryConfigurationWhenValidationFailedButConfigurationLoaded() =
        runTest {
            givenLoadedConfiguration(true)
            givenInitConfigurationFails(ConfigurationSignatureValidationException())

            librarySetup.setupLibraries(context, false)
            librarySetup.setupLibraries(context, false)

            verify(configurationLoader, times(1)).initConfiguration(any(), anyOrNull(), any())
            verify(initialization, times(2)).init(any(), any())
        }

    @Test
    fun librarySetup_setupLibraries_configuresOnceWhenEnteredConcurrently() =
        runTest {
            givenLoadedConfiguration(true)

            coroutineScope {
                launch { librarySetup.setupLibraries(context, false) }
                launch { librarySetup.setupLibraries(context, false) }
            }

            verify(configurationLoader, times(1)).initConfiguration(any(), anyOrNull(), any())
            verify(initialization, times(2)).init(any(), any())
        }

    @Test
    fun librarySetup_setupLibraries_loadsFallbackConfigurationWhenNoneLoaded() =
        runTest {
            givenLoadedConfiguration(false)
            givenInitConfigurationFails(IllegalStateException("broken"))

            librarySetup.setupLibraries(context, false)

            verify(configurationLoader).loadLocalConfiguration(context)
        }

    @Test
    fun librarySetup_setupLibraries_skipsFallbackWhenConfigurationAlreadyLoaded() =
        runTest {
            givenLoadedConfiguration(true)
            givenInitConfigurationFails(IllegalStateException("broken"))

            librarySetup.setupLibraries(context, false)

            verify(configurationLoader, never()).loadLocalConfiguration(any())
        }

    private fun givenLoadedConfiguration(loaded: Boolean) {
        val configuration = if (loaded) mock<ConfigurationProvider>() else null
        whenever(configurationLoader.getConfigurationFlow()).thenReturn(MutableStateFlow(configuration))
    }

    private suspend fun givenInitConfigurationFails(throwable: Throwable) {
        whenever(configurationLoader.initConfiguration(any(), anyOrNull(), any())).thenThrow(throwable)
    }
}
