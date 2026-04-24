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
