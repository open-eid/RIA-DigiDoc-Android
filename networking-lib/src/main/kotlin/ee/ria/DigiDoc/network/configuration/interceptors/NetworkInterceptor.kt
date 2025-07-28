@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.configuration.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
