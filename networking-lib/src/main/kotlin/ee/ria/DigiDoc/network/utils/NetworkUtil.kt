@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getManualProxySettings
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getProxy
import ee.ria.DigiDoc.network.proxy.ProxyUtil.getProxySetting
import okhttp3.Authenticator
import okhttp3.Credentials.basic
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import java.net.Proxy
import java.util.concurrent.TimeUnit

object NetworkUtil {
    const val DEFAULT_TIMEOUT: Int = 5

    fun constructClientBuilder(context: Context?): OkHttpClient.Builder {
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
                        basic(manualProxy.username, manualProxy.password)
                    val requestBuilder: Request.Builder =
                        originalRequest.newBuilder()
                            .addHeader("Proxy-Authorization", credential)
                            .addHeader("Authorization", credential)

                    val newRequest: Request = requestBuilder.build()
                    chain.proceed(newRequest)
                },
            )
        }

        return builder
    }
}
