@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.sid.rest

import ee.ria.DigiDoc.network.sid.dto.request.PostCertificateRequest
import ee.ria.DigiDoc.network.sid.dto.response.SessionResponse
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SIDRestServiceClient {
    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @POST("certificatechoice/etsi/{semanticsIdentifier}")
    fun getCertificateV2(
        @Path(value = "semanticsIdentifier", encoded = true) semanticsIdentifier: String?,
        @Body body: PostCertificateRequest?,
    ): Call<SessionResponse>

    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @POST("signature/document/{documentnumber}")
    fun getCreateSignature(
        @Path(value = "documentnumber", encoded = true) documentnumber: String?,
        @Body body: String?,
    ): Call<SessionResponse>

    @Headers(CONTENT_TYPE_HEADER, CONTENT_TYPE_ACCEPT)
    @GET("session/{session_id}")
    fun getSessionStatus(
        @Path(value = "session_id", encoded = true) sessionId: String?,
        @Query("timeoutMs") timeoutMs: Long,
    ): Call<SessionStatusResponse>

    companion object {
        const val CONTENT_TYPE_HEADER: String = "Content-Type: application/json; charset=utf-8"
        const val CONTENT_TYPE_ACCEPT: String = "Accept: application/json"
    }
}
