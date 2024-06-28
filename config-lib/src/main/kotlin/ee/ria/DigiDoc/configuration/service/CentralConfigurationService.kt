@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.service

import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.network.configuration.interceptors.NetworkInterceptor
import ee.ria.DigiDoc.network.configuration.interceptors.UserAgentInterceptor
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface CentralConfigurationService {
    suspend fun fetchConfiguration(): String

    suspend fun fetchPublicKey(): String

    suspend fun fetchSignature(): String

    fun constructHttpClient(defaultTimeout: Long): OkHttpClient

    fun constructRetrofit(
        baseUrl: String,
        constructHttpClient: OkHttpClient,
    ): Retrofit
}

@Singleton
class CentralConfigurationServiceImpl
    @Inject
    constructor(
        private val userAgent: String,
        private val configurationProperty: ConfigurationProperty,
    ) : CentralConfigurationService {
        private val defaultTimeout = 5L

        override suspend fun fetchConfiguration(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchConfiguration()
        }

        override suspend fun fetchPublicKey(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchPublicKey()
        }

        @Throws(Exception::class)
        override suspend fun fetchSignature(): String {
            val retrofit =
                constructRetrofit(
                    configurationProperty.centralConfigurationServiceUrl,
                    constructHttpClient(defaultTimeout),
                ).create(CentralConfigurationRepository::class.java)

            return retrofit.fetchSignature()
        }

        override fun constructHttpClient(defaultTimeout: Long): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                )
                .addInterceptor(UserAgentInterceptor(userAgent))
                .addInterceptor(NetworkInterceptor())
                .hostnameVerifier(OkHostnameVerifier)
                .connectTimeout(defaultTimeout, TimeUnit.SECONDS)
                .readTimeout(defaultTimeout, TimeUnit.SECONDS)
                .callTimeout(defaultTimeout, TimeUnit.SECONDS)
                .writeTimeout(defaultTimeout, TimeUnit.SECONDS)
                .build()
        }

        override fun constructRetrofit(
            baseUrl: String,
            constructHttpClient: OkHttpClient,
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("$baseUrl/")
                .client(constructHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
