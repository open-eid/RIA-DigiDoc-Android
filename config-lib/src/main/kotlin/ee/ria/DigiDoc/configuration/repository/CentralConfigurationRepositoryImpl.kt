@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CentralConfigurationRepositoryImpl
    @Inject
    constructor(
        private val configurationService: CentralConfigurationService,
    ) : CentralConfigurationRepository {
        override suspend fun fetchConfiguration(): String {
            return configurationService.fetchConfiguration()
        }

        override suspend fun fetchPublicKey(): String {
            return configurationService.fetchPublicKey()
        }

        @Throws(Exception::class)
        override suspend fun fetchSignature(): String {
            return configurationService.fetchSignature()
        }

        override suspend fun setupProxy(
            proxySetting: ProxySetting?,
            manualProxy: ManualProxy,
        ) {
            return configurationService.setupProxy(proxySetting, manualProxy)
        }
    }
