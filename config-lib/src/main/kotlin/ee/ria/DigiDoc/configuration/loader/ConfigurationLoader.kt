// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.loader

import android.content.Context
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.flow.StateFlow

interface ConfigurationLoader {
    @Throws(Exception::class)
    suspend fun initConfiguration(
        context: Context,
        proxySetting: ProxySetting?,
        manualProxy: ManualProxy,
    )

    fun getConfigurationFlow(): StateFlow<ConfigurationProvider?>

    suspend fun loadConfigurationProperty(context: Context): ConfigurationProperty

    suspend fun loadCachedConfiguration(
        context: Context,
        afterCentralCheck: Boolean,
    )

    suspend fun loadLocalConfiguration(context: Context)

    suspend fun loadDefaultConfiguration(context: Context)

    @Throws(Exception::class)
    suspend fun loadCentralConfiguration(
        context: Context,
        proxySetting: ProxySetting?,
        proxy: ManualProxy,
    )

    suspend fun shouldCheckForUpdates(context: Context): Boolean
}
