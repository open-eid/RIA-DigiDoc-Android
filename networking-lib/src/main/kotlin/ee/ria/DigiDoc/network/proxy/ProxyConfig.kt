@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.proxy

import okhttp3.Authenticator
import java.net.Proxy

class ProxyConfig(
    private val proxy: Proxy?,
    private val authenticator: Authenticator,
    private val manualProxy: ManualProxy?,
) {
    fun proxy(): Proxy? {
        return proxy
    }

    fun authenticator(): Authenticator {
        return authenticator
    }

    fun manualProxy(): ManualProxy? {
        return manualProxy
    }
}
