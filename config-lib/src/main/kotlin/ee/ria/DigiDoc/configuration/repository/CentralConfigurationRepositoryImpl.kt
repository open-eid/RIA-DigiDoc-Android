@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
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

        override suspend fun fetchSignature(): String {
            return configurationService.fetchSignature()
        }
    }
