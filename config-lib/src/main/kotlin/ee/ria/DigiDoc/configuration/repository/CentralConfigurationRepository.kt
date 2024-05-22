@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
import kotlin.jvm.Throws

class CentralConfigurationRepository(private val apiService: CentralConfigurationService) {
    @Throws(Exception::class)
    suspend fun fetchConfiguration(): String {
        return apiService.fetchConfiguration()
    }

    @Throws(Exception::class)
    suspend fun fetchPublicKey(): String {
        return apiService.fetchPublicKey()
    }

    @Throws(Exception::class)
    suspend fun fetchSignature(): String {
        return apiService.fetchSignature()
    }
}
