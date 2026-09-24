// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.utils

import android.content.Context
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil.getManualProxySettings
import ee.ria.DigiDoc.network.utils.ProxyUtil.getProxy
import ee.ria.DigiDoc.network.utils.ProxyUtil.getProxySetting
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.internal.tls.OkHostnameVerifier
import java.net.Proxy
import java.util.concurrent.TimeUnit

object NetworkUtil {
    const val DEFAULT_TIMEOUT: Int = 5

    fun constructClientBuilder(context: Context?): OkHttpClient.Builder {
        val builder: OkHttpClient.Builder =
            OkHttpClient
                .Builder()
                .hostnameVerifier(OkHostnameVerifier)
                .connectTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .callTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT.toLong(), TimeUnit.SECONDS)

        if (context != null) {
            val proxySetting: ProxySetting? = getProxySetting(context)
            val manualProxy: ManualProxy = getManualProxySettings(context)
            val proxyConfig: ProxyConfig = getProxy(proxySetting, manualProxy)

            builder
                .proxy(if (proxySetting === ProxySetting.NO_PROXY) Proxy.NO_PROXY else proxyConfig.proxy())
                .proxyAuthenticator(
                    if (proxySetting === ProxySetting.NO_PROXY) Authenticator.NONE else proxyConfig.authenticator(),
                )
        }

        return builder
    }
}
