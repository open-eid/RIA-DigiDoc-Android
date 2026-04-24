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
