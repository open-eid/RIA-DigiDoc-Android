@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.service

import retrofit2.http.GET

interface CentralConfigurationService {
    @GET("config.json")
    suspend fun fetchConfiguration(): String

    @GET("config.pub")
    suspend fun fetchPublicKey(): String

    @GET("config.rsa")
    suspend fun fetchSignature(): String
}
