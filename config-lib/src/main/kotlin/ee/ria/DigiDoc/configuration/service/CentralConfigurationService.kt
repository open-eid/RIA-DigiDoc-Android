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

package ee.ria.DigiDoc.configuration.service

import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.network.configuration.interceptors.NetworkInterceptor
import ee.ria.DigiDoc.network.configuration.interceptors.UserAgentInterceptor
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil
import okhttp3.Authenticator
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface CentralConfigurationService {
    suspend fun fetchConfiguration(): String

    suspend fun fetchPublicKey(): String

    suspend fun fetchSignature(): String

    suspend fun setupProxy(
        proxySetting: ProxySetting?,
        manualProxy: ManualProxy,
    )

    fun constructHttpClient(
        defaultTimeout: Long,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): OkHttpClient

    fun constructRetrofit(
        baseUrl: String,
        constructHttpClient: OkHttpClient,
    ): Retrofit
}

@Singleton
open class CentralConfigurationServiceImpl
    @Inject
    constructor(
        private val userAgent: String,
        private val configurationProperty: ConfigurationProperty,
    ) : CentralConfigurationService {
        private val defaultTimeout = 5L
        private var proxySetting: ProxySetting = ProxySetting.NO_PROXY
        private var manualProxy: ManualProxy = ManualProxy("", 80, "", "")

        override suspend fun fetchConfiguration(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout, proxySetting, manualProxy),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchConfiguration()
        }

        override suspend fun fetchPublicKey(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout, proxySetting, manualProxy),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchPublicKey()
        }

        @Throws(Exception::class)
        override suspend fun fetchSignature(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout, proxySetting, manualProxy),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchSignature()
        }

        override suspend fun setupProxy(
            proxySetting: ProxySetting?,
            manualProxy: ManualProxy,
        ) {
            this.proxySetting = proxySetting ?: ProxySetting.NO_PROXY
            this.manualProxy = manualProxy
        }

        override fun constructHttpClient(
            defaultTimeout: Long,
            proxySetting: ProxySetting?,
            manualProxySettings: ManualProxy,
        ): OkHttpClient {
            val proxyConfig: ProxyConfig = ProxyUtil.getProxy(proxySetting, manualProxySettings)

            return OkHttpClient
                .Builder()
                .proxy(if (proxySetting === ProxySetting.NO_PROXY) Proxy.NO_PROXY else proxyConfig.proxy())
                .proxyAuthenticator(
                    if (proxySetting === ProxySetting.NO_PROXY) Authenticator.NONE else proxyConfig.authenticator(),
                ).addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                ).addInterceptor(UserAgentInterceptor(userAgent))
                .addInterceptor(NetworkInterceptor())
                .hostnameVerifier(OkHostnameVerifier)
                .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
                .connectionPool(ConnectionPool(0, 5, TimeUnit.SECONDS))
                .readTimeout(defaultTimeout, TimeUnit.SECONDS)
                .callTimeout(defaultTimeout, TimeUnit.SECONDS)
                .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
                .build()
        }

        override fun constructRetrofit(
            baseUrl: String,
            constructHttpClient: OkHttpClient,
        ): Retrofit =
            Retrofit
                .Builder()
                .baseUrl("$baseUrl/")
                .client(constructHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }
