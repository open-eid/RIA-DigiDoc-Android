@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.rest

import android.content.Context
import com.takisoft.preferencex.BuildConfig
import ee.ria.DigiDoc.common.Constant.MobileIdConstants.CERT_PEM_FOOTER
import ee.ria.DigiDoc.common.Constant.MobileIdConstants.CERT_PEM_HEADER
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.proxy.ProxyUtil
import ee.ria.DigiDoc.network.utils.isLoggingEnabled
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import okhttp3.Authenticator
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.bouncycastle.util.encoders.Base64
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.Proxy
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface ServiceGenerator {
    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    fun createService(
        context: Context,
        sslContext: SSLContext?,
        midSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        trustManagers: Array<TrustManager>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): MIDRestServiceClient
}

class ServiceGeneratorImpl : ServiceGenerator {
    private val logTag = javaClass.simpleName
    private lateinit var loggingInterceptor: HttpLoggingInterceptor

    @Throws(CertificateException::class, NoSuchAlgorithmException::class, IllegalArgumentException::class)
    override fun createService(
        context: Context,
        sslContext: SSLContext?,
        midSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        trustManagers: Array<TrustManager>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): MIDRestServiceClient {
        debugLog(logTag, "Creating new retrofit instance")
        return Retrofit.Builder()
            .baseUrl("$midSignServiceUrl/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                buildHttpClient(
                    context,
                    sslContext,
                    midSignServiceUrl,
                    certBundle,
                    trustManagers,
                    proxySetting,
                    manualProxySettings,
                ),
            )
            .build()
            .create(MIDRestServiceClient::class.java)
    }

    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    private fun buildHttpClient(
        context: Context,
        sslContext: SSLContext?,
        midSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        trustManagers: Array<TrustManager>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): OkHttpClient {
        debugLog(logTag, "Building new httpClient")

        val proxyConfig: ProxyConfig = ProxyUtil.getProxy(proxySetting, manualProxySettings)

        val httpClientBuilder: OkHttpClient.Builder =
            OkHttpClient.Builder()
                .proxy(if (proxySetting === ProxySetting.NO_PROXY) Proxy.NO_PROXY else proxyConfig.proxy())
                .proxyAuthenticator(
                    if (proxySetting === ProxySetting.NO_PROXY) Authenticator.NONE else proxyConfig.authenticator(),
                )
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .pingInterval(3, TimeUnit.SECONDS)
                .certificatePinner(trustedCertificates(midSignServiceUrl, certBundle))
        addLoggingInterceptor(httpClientBuilder, context)
        if (sslContext != null) {
            try {
                httpClientBuilder.sslSocketFactory(
                    sslContext.socketFactory,
                    trustManagers[0] as X509TrustManager,
                )
            } catch (e: Exception) {
                debugLog(logTag, "Error building httpClient with sslContext")
            }
        }
        return httpClientBuilder.build()
    }

    private fun addLoggingInterceptor(
        httpClientBuilder: OkHttpClient.Builder,
        context: Context,
    ) {
        if (isLoggingEnabled(context) || BuildConfig.DEBUG) {
            debugLog(logTag, "Adding logging interceptor to HTTP client")
            loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            if (!httpClientBuilder.interceptors().contains(loggingInterceptor)) {
                httpClientBuilder.interceptors().add(loggingInterceptor)
            }
            httpClientBuilder.addInterceptor(
                Interceptor { chain: Interceptor.Chain ->
                    val request = chain.request()
                    debugLog(logTag, "Request: " + request.method + " " + request.url)
                    val requestBody = request.body
                    if (requestBody != null) {
                        Buffer().use { buffer ->
                            requestBody.writeTo(buffer)
                            debugLog(logTag, " Body: " + buffer.readUtf8())
                        }
                    }
                    chain.proceed(request)
                },
            )
        }
    }

    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    private fun trustedCertificates(
        midSignServiceUrl: String?,
        certBundle: ArrayList<String>,
    ): CertificatePinner {
        val uri = midSignServiceUrl?.let { toURI(it) }

        if (uri != null) {
            val sha256Certificates = arrayOfNulls<String>(certBundle.size)
            try {
                for (i in certBundle.indices) {
                    val pemCert =
                        """
                        $CERT_PEM_HEADER
                        ${certBundle[i]}
                        $CERT_PEM_FOOTER
                        """.trimIndent()
                    sha256Certificates[i] =
                        "sha256/" + getSHA256FromCertificate(CertificateUtil.x509Certificate(pemCert))
                }
            } catch (e: CertificateException) {
                errorLog(logTag, "Failed to convert to Certificate object", e)
                throw e
            } catch (e: NoSuchAlgorithmException) {
                errorLog(logTag, "Failed to get SHA-256 from certificate", e)
                throw e
            }

            val certificatePinner: CertificatePinner.Builder =
                CertificatePinner.Builder()
                    .add(uri.host, *sha256Certificates.filterNotNull().toTypedArray())

            return certificatePinner.build()
        }

        return CertificatePinner.Builder().build()
    }

    private fun toURI(url: String): URI? {
        try {
            return URI(url)
        } catch (e: URISyntaxException) {
            errorLog(logTag, "Failed to convert URI from URL", e)
            return null
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun getSHA256FromCertificate(cert: Certificate): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val encodedHash = digest.digest(cert.publicKey.encoded)
            val base64EncodedHash: ByteArray =
                Base64.encode(encodedHash)

            return String(base64EncodedHash, StandardCharsets.UTF_8)
        } catch (e: NoSuchAlgorithmException) {
            errorLog(logTag, "Failed to get SHA-256 from certificate", e)
            throw e
        }
    }
}
