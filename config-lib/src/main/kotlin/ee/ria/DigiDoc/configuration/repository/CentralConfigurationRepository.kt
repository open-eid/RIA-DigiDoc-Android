@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import retrofit2.http.GET

interface CentralConfigurationRepository {
    @Throws(Exception::class)
    @GET("config.json")
    suspend fun fetchConfiguration(): String

    @Throws(Exception::class)
    @GET("config.pub")
    suspend fun fetchPublicKey(): String

    @Throws(Exception::class)
    @GET("config.rsa")
    suspend fun fetchSignature(): String

    suspend fun setupProxy(
        proxySetting: ProxySetting?,
        manualProxy: ManualProxy,
    )
}
