@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.configuration.client

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import ee.ria.DigiDoc.network.R
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getManualProxySettings
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getProxy
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getProxySetting
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import okhttp3.Authenticator
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import java.io.IOException
import java.net.Proxy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

class CentralConfigurationClient(
    private val context: Context?,
    private val centralConfigurationServiceUrl: String,
    private val userAgent: String,
) {
    private val logTag = javaClass.simpleName
    private val httpClient: OkHttpClient = constructHttpClient(context)

    val configuration: String
        get() {
            val future =
                requestData(
                    "$centralConfigurationServiceUrl/config.json",
                )
            future.exceptionally { e: Throwable ->
                errorLog(logTag, String.format("%s %s", "Unable to get configuration", e.localizedMessage), e)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        R.string.no_internet_connection,
                        Toast.LENGTH_LONG,
                    ).show()
                }
                future.join()
            }
            return future.join()
        }
    val configurationSignature: String
        get() {
            val future =
                requestData(
                    "$centralConfigurationServiceUrl/config.rsa",
                )
            future.exceptionally { e: Throwable ->
                errorLog(
                    logTag,
                    String.format("%s %s", "Unable to get configuration signature", e.localizedMessage),
                    e,
                )
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        R.string.no_internet_connection,
                        Toast.LENGTH_LONG,
                    ).show()
                }
                future.join()
            }
            return future.join()
        }
    val configurationSignaturePublicKey: String
        get() {
            val future =
                requestData(
                    "$centralConfigurationServiceUrl/config.pub",
                )
            future.exceptionally { e: Throwable ->
                errorLog(
                    logTag,
                    String.format("%s %s", "Unable to get configuration public key", e.localizedMessage),
                    e,
                )
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        R.string.no_internet_connection,
                        Toast.LENGTH_LONG,
                    ).show()
                }
                future.join()
            }
            return future.join()
        }

    private fun requestData(url: String): CompletableFuture<String> {
        val result = CompletableFuture<String>()
        val request: Request =
            Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", userAgent)
                .build()

        CompletableFuture.runAsync {
            val call = httpClient.newCall(request)
            try {
                val response = call.execute()
                if (response.code != 200) {
                    result.completeExceptionally(
                        CentralConfigurationException("Service responded with not OK status code " + response.code),
                    )
                    return@runAsync
                }
                val responseBody = response.body
                if (responseBody == null) {
                    result.completeExceptionally(CentralConfigurationException("Service responded with empty body"))
                    return@runAsync
                }
                result.complete(responseBody.string())
            } catch (e: IOException) {
                result.completeExceptionally(
                    CentralConfigurationException(
                        "Something went wrong during fetching configuration",
                        e,
                    ),
                )
            }
        }

        return result
    }

    internal class CentralConfigurationException : RuntimeException {
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 5

        private fun constructHttpClient(context: Context?): OkHttpClient {
            return try {
                val builder: OkHttpClient.Builder = constructClientBuilder(context)
                builder.build()
            } catch (e: Exception) {
                throw IllegalStateException("Failed to construct HTTP client", e)
            }
        }

        private fun constructClientBuilder(context: Context?): OkHttpClient.Builder {
            val builder: OkHttpClient.Builder =
                OkHttpClient.Builder()
                    .hostnameVerifier(OkHostnameVerifier)
                    .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .callTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
            if (context != null) {
                val proxySetting: ProxySetting? = getProxySetting(context)
                val manualProxy: ManualProxy = getManualProxySettings(context)
                val proxyConfig: ProxyConfig = getProxy(proxySetting, manualProxy)
                builder.proxy(if (proxySetting === ProxySetting.NO_PROXY) Proxy.NO_PROXY else proxyConfig.proxy())
                    .proxyAuthenticator(
                        if (proxySetting === ProxySetting.NO_PROXY) Authenticator.NONE else proxyConfig.authenticator(),
                    )
                builder.addInterceptor(
                    Interceptor { chain: Interceptor.Chain ->
                        val originalRequest = chain.request()
                        val credential =
                            manualProxy.password?.let { manualProxy.username?.let { username -> basic(username, it) } }
                        val requestBuilder: Request.Builder? =
                            credential?.let {
                                originalRequest.newBuilder()
                                    .addHeader("Proxy-Authorization", it)
                                    .addHeader("Authorization", credential)
                            }
                        val newRequest: Request? = requestBuilder?.build()
                        if (newRequest != null) {
                            chain.proceed(newRequest)
                        } else {
                            chain.proceed(originalRequest.newBuilder().build())
                        }
                    },
                )
            }
            return builder
        }
    }
}
