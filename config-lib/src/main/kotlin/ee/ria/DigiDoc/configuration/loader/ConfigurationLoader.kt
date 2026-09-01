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
