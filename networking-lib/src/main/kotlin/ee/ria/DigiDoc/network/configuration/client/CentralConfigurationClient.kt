@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.configuration.client

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class CentralConfigurationClient(configurationServiceUrl: String, userAgent: String) {
    private val defaultTimeout = 5

    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val constructHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(UserAgentInterceptor(userAgent))
            .addInterceptor(NetworkInterceptor())
            .hostnameVerifier(OkHostnameVerifier)
            .connectTimeout(defaultTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(defaultTimeout.toLong(), TimeUnit.SECONDS)
            .callTimeout(defaultTimeout.toLong(), TimeUnit.SECONDS)
            .writeTimeout(defaultTimeout.toLong(), TimeUnit.SECONDS)
            .build()

    val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(configurationServiceUrl)
            .client(constructHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
            chain.request().newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", userAgent)
                .build()
        return chain.proceed(request)
    }
}

class NetworkInterceptor : Interceptor {
    private val logTag = javaClass.simpleName

    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            return chain.proceed(chain.request())
        } catch (ioe: IOException) {
            errorLog(logTag, "Unable to request resource", ioe)
            throw ioe
        }
    }
}
