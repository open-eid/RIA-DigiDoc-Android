@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.mid.rest

import ee.ria.DigiDoc.network.mid.dto.request.PostMobileCreateSignatureCertificateRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureCertificateResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionResponse
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureSessionStatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MIDRestServiceClient {
    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @POST("certificate")
    fun getCertificate(
        @Body body: PostMobileCreateSignatureCertificateRequest?,
    ): Call<MobileCreateSignatureCertificateResponse>

    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @POST("signature")
    fun getMobileCreateSession(
        @Body body: String?,
    ): Call<MobileCreateSignatureSessionResponse>

    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @GET("signature/session/{session_id}")
    fun getMobileCreateSignatureSessionStatus(
        @Path(
            value = "session_id",
            encoded = true,
        ) sessionId: String?,
        @Query("timeoutMs") timeoutMs: String?,
    ): Call<MobileCreateSignatureSessionStatusResponse>

    companion object {
        const val CONTENT_TYPE_HEADER: String = "Content-Type: application/json; charset=utf-8"
        const val CONTENT_TYPE_ACCEPT: String = "Accept: application/json"
    }
}
