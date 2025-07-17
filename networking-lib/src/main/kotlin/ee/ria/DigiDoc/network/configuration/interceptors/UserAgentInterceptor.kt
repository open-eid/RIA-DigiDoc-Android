@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.configuration.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor(
    private val userAgent: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
            chain
                .request()
                .newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", userAgent)
                .build()
        return chain.proceed(request)
    }
}
