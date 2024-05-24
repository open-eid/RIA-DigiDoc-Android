@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.signing

import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object TrustManagerUtil {
    @get:Throws(
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
    )
    val trustManagers: Array<TrustManager>
        get() {
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(null as KeyStore?)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                (
                    "Unexpected default trust managers:" +
                        trustManagers.contentToString()
                )
            }
            return trustManagerFactory.trustManagers
        }
}
