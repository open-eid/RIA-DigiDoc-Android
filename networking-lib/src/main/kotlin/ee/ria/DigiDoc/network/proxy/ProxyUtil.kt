@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.proxy

import android.content.Context
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.common.preferences.EncryptedPreferences
import ee.ria.DigiDoc.network.R
import okhttp3.Authenticator
import okhttp3.Credentials.basic
import okhttp3.Response
import okhttp3.Route
import org.apache.commons.lang3.math.NumberUtils
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.GeneralSecurityException
import java.util.concurrent.CompletableFuture

object ProxyUtil {
    fun getProxyValues(
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy?,
    ): ManualProxy? {
        when (proxySetting) {
            ProxySetting.NO_PROXY -> null
            ProxySetting.SYSTEM_PROXY -> {
                val systemProxy =
                    ManualProxy(
                        System.getProperty("http.proxyHost") ?: "",
                        NumberUtils.toInt(System.getProperty("http.proxyPort"), 80),
                        System.getProperty("http.proxyUser") ?: "",
                        System.getProperty("http.proxyPassword") ?: "",
                    )
                return systemProxy
            }

            ProxySetting.MANUAL_PROXY -> {
                return manualProxySettings ?: ManualProxy("", 80, "", "")
            }

            null -> null
        }
        return null
    }

    fun getProxy(
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy?,
    ): ProxyConfig {
        when (proxySetting) {
            ProxySetting.NO_PROXY -> {}
            ProxySetting.SYSTEM_PROXY -> {
                val systemProxy =
                    ManualProxy(
                        System.getProperty("http.proxyHost") ?: "",
                        NumberUtils.toInt(System.getProperty("http.proxyPort"), 80),
                        System.getProperty("http.proxyUser") ?: "",
                        System.getProperty("http.proxyPassword") ?: "",
                    )
                var authenticator: Authenticator? = null
                if (systemProxy.username.isNotEmpty() &&
                    systemProxy.password.isNotEmpty()
                ) {
                    authenticator =
                        Authenticator { _: Route?, response: Response ->
                            if (hasRetried(response)) {
                                return@Authenticator null
                            }
                            val credential =
                                basic(
                                    systemProxy.username ?: "",
                                    systemProxy.password ?: "",
                                )
                            response.request.newBuilder()
                                .header("Proxy-Authorization", credential)
                                .header("Authorization", credential)
                                .build()
                        }
                }
                return getProxyConfig(systemProxy, authenticator).join()
            }

            ProxySetting.MANUAL_PROXY -> {
                val authenticator =
                    Authenticator { _: Route?, response: Response ->
                        if (hasRetried(response)) {
                            return@Authenticator null
                        }
                        val credential =
                            manualProxySettings?.username.let { username ->
                                manualProxySettings?.password.let { password ->
                                    if (username != null) {
                                        if (password != null) {
                                            basic(
                                                username,
                                                password,
                                            )
                                        } else {
                                            null
                                        }
                                    } else {
                                        null
                                    }
                                }
                            }
                        if (credential != null) {
                            response.request.newBuilder()
                                .header("Proxy-Authorization", credential)
                                .header("Authorization", credential)
                                .build()
                        } else {
                            null
                        }
                    }
                return getProxyConfig(manualProxySettings, authenticator).join()
            }

            null -> {}
        }
        return ProxyConfig(null, Authenticator.NONE, null)
    }

    private fun getProxyConfig(
        manualProxy: ManualProxy?,
        authenticator: Authenticator?,
    ): CompletableFuture<ProxyConfig> {
        return CompletableFuture.supplyAsync {
            if (!manualProxy?.host.isNullOrEmpty()) {
                val proxy =
                    Proxy(
                        Proxy.Type.HTTP,
                        manualProxy?.port?.let { InetSocketAddress(manualProxy.host, it) },
                    )
                return@supplyAsync ProxyConfig(
                    proxy,
                    authenticator ?: Authenticator.NONE,
                    manualProxy,
                )
            }
            ProxyConfig(null, authenticator ?: Authenticator.NONE, null)
        }
    }

    fun getProxySetting(context: Context): ProxySetting? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val proxySettingPreference =
            sharedPreferences.getString(
                context.getString(R.string.main_settings_proxy_setting_key),
                ProxySetting.NO_PROXY.name,
            )
        return proxySettingPreference?.let { ProxySetting.valueOf(it) }
    }

    fun getManualProxySettings(context: Context): ManualProxy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val host = sharedPreferences.getString(context.getString(R.string.main_settings_proxy_host_key), "") ?: ""
        val port =
            sharedPreferences.getInt(context.getString(R.string.main_settings_proxy_port_key), 80)
        val username =
            sharedPreferences.getString(
                context.getString(R.string.main_settings_proxy_username_key),
                "",
            )
                ?: ""
        val password: String =
            try {
                EncryptedPreferences.getEncryptedPreferences(context)
                    .getString(context.getString(R.string.main_settings_proxy_password_key), "") ?: ""
            } catch (e: IOException) {
                ""
            } catch (e: GeneralSecurityException) {
                ""
            }
        return ManualProxy(host, port, username, password)
    }

    private fun hasRetried(response: Response): Boolean {
        return !response.isSuccessful &&
            response.request.header("Proxy-Authorization") != null
    }
}
