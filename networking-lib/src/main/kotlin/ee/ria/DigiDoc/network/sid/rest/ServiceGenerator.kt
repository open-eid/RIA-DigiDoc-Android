@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.rest

import android.content.Context
import com.takisoft.preferencex.BuildConfig
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyConfig
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.network.utils.ProxyUtil
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
import java.util.Base64
import java.util.concurrent.TimeUnit

interface ServiceGenerator {
    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    fun createService(
        context: Context,
        sidSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): SIDRestServiceClient
}

class ServiceGeneratorImpl : ServiceGenerator {
    private val logTag = javaClass.simpleName
    private lateinit var loggingInterceptor: HttpLoggingInterceptor

    @Throws(CertificateException::class, NoSuchAlgorithmException::class, IllegalArgumentException::class)
    override fun createService(
        context: Context,
        sidSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): SIDRestServiceClient {
        debugLog(logTag, "Creating new retrofit instance")
        return Retrofit
            .Builder()
            .baseUrl("$sidSignServiceUrl")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                buildHttpClient(
                    context,
                    sidSignServiceUrl,
                    certBundle,
                    proxySetting,
                    manualProxySettings,
                ),
            ).build()
            .create(SIDRestServiceClient::class.java)
    }

    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    private fun buildHttpClient(
        context: Context,
        sidSignServiceUrl: String?,
        certBundle: ArrayList<String>,
        proxySetting: ProxySetting?,
        manualProxySettings: ManualProxy,
    ): OkHttpClient {
        debugLog(logTag, "Building new httpClient")

        val proxyConfig: ProxyConfig = ProxyUtil.getProxy(proxySetting, manualProxySettings)

        val httpClientBuilder: OkHttpClient.Builder =
            OkHttpClient
                .Builder()
                .proxy(if (proxySetting === ProxySetting.NO_PROXY) Proxy.NO_PROXY else proxyConfig.proxy())
                .proxyAuthenticator(
                    if (proxySetting === ProxySetting.NO_PROXY) Authenticator.NONE else proxyConfig.authenticator(),
                ).connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .pingInterval(3, TimeUnit.SECONDS)
                .certificatePinner(trustedCertificates(sidSignServiceUrl, certBundle))
                .cache(null)
        addLoggingInterceptor(httpClientBuilder, context)
        return httpClientBuilder.build()
    }

    private fun addLoggingInterceptor(
        httpClientBuilder: OkHttpClient.Builder,
        context: Context,
    ) {
        val isDebug = BuildConfig.BUILD_TYPE.contentEquals("debug")

        if (isLoggingEnabled(context) || isDebug) {
            debugLog(logTag, "Adding logging interceptor to HTTP client")
            loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            if (!httpClientBuilder.interceptors().contains(loggingInterceptor)) {
                httpClientBuilder.addInterceptor(loggingInterceptor)
            }
            httpClientBuilder.addInterceptor(
                Interceptor { chain: Interceptor.Chain ->
                    val request = chain.request()
                    if (isDebug) {
                        debugLog(logTag, request.method + " " + request.url)
                        val headers = request.headers
                        debugLog(logTag, "Headers: " + arrayOf(headers).contentDeepToString())
                        val requestBody = request.body
                        if (requestBody != null) {
                            Buffer().use { buffer ->
                                requestBody.writeTo(buffer)
                                debugLog(logTag, "Body: " + buffer.readUtf8())
                            }
                        }
                    }
                    chain.proceed(request)
                },
            )
        }
    }

    @Throws(CertificateException::class, NoSuchAlgorithmException::class)
    private fun trustedCertificates(
        sidSignServiceUrl: String?,
        certBundle: ArrayList<String>,
    ): CertificatePinner {
        val uri: URI
        try {
            uri = URI(sidSignServiceUrl)
        } catch (_: URISyntaxException) {
            debugLog(logTag, "Failed to convert URI from URL")
            return CertificatePinner.Builder().build()
        }
        val sha256Certificates = arrayOfNulls<String>(certBundle.size)
        try {
            for (i in certBundle.indices) {
                sha256Certificates[i] = "sha256/" +
                    getSHA256FromCertificate(
                        CertificateUtil.x509Certificate(
                            Base64.getDecoder().decode(certBundle[i]),
                        ),
                    )
            }
        } catch (e: CertificateException) {
            errorLog(logTag, "Failed to convert to Certificate object", e)
            throw e
        } catch (e: NoSuchAlgorithmException) {
            errorLog(logTag, "Failed to convert to Certificate object", e)
            throw e
        }
        return CertificatePinner
            .Builder()
            .add(uri.host, *sha256Certificates.filterNotNull().toTypedArray())
            .build()
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun getSHA256FromCertificate(cert: Certificate): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val encodedHash = digest.digest(cert.publicKey.encoded)
            val base64EncodedHash = Base64.getEncoder().encode(encodedHash)
            return String(base64EncodedHash, StandardCharsets.UTF_8)
        } catch (e: NoSuchAlgorithmException) {
            errorLog(logTag, "Unable to get instance of algorithm", e)
            throw e
        }
    }
}
