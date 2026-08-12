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
