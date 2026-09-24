// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigurationRepositoryImpl
    @Inject
    constructor(
        private val configurationLoader: ConfigurationLoader,
    ) : ConfigurationRepository {
        override fun getConfiguration(): ConfigurationProvider? = configurationLoader.getConfigurationFlow().value

        override suspend fun observeConfigurationUpdates(onUpdate: suspend (ConfigurationProvider) -> Unit) {
            configurationLoader
                .getConfigurationFlow()
                .filterNotNull()
                .collect { config ->
                    onUpdate(config)
                }
        }
    }
