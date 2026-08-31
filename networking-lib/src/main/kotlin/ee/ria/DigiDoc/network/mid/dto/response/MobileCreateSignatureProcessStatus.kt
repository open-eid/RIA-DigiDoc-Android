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

package ee.ria.DigiDoc.network.mid.dto.response

enum class MobileCreateSignatureProcessStatus {
    OK,
    TIMEOUT,
    NOT_MID_CLIENT,
    USER_CANCELLED,
    SIGNATURE_HASH_MISMATCH,
    PHONE_ABSENT,
    DELIVERY_ERROR,
    SIM_ERROR,
    TOO_MANY_REQUESTS,
    EXCEEDED_UNSUCCESSFUL_REQUESTS,
    INVALID_ACCESS_RIGHTS,
    OCSP_INVALID_TIME_SLOT,
    CERTIFICATE_REVOKED,
    CERTIFICATE_EXPIRED,
    CERTIFICATE_NOT_YET_VALID,
    GENERAL_ERROR,
    NO_RESPONSE,
    INVALID_COUNTRY_CODE,
    INVALID_SSL_HANDSHAKE,
    TECHNICAL_ERROR,
    INVALID_PROXY_SETTINGS,
}
