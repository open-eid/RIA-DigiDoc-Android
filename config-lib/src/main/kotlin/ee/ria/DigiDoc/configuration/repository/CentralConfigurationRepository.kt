// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

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
    @GET("config.ecc")
    suspend fun fetchSignature(): String

    suspend fun setupProxy(
        proxySetting: ProxySetting?,
        manualProxy: ManualProxy,
    )
}
