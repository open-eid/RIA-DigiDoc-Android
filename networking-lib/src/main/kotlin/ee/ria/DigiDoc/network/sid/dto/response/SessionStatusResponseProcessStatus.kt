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

package ee.ria.DigiDoc.network.sid.dto.response

@Suppress("unused")
enum class SessionStatusResponseProcessStatus {
    OK,
    TIMEOUT,
    USER_REFUSED,
    DOCUMENT_UNUSABLE,
    WRONG_VC,

    REQUIRED_INTERACTION_NOT_SUPPORTED_BY_APP,
    USER_REFUSED_DISPLAYTEXTANDPIN,
    USER_REFUSED_VC_CHOICE,
    USER_REFUSED_CONFIRMATIONMESSAGE,
    USER_REFUSED_CONFIRMATIONMESSAGE_WITH_VC_CHOICE,
    USER_REFUSED_CERT_CHOICE,
    ACCOUNT_NOT_FOUND,
    SESSION_NOT_FOUND,
    MISSING_SESSIONID,
    EXCEEDED_UNSUCCESSFUL_REQUESTS,
    TOO_MANY_REQUESTS,
    NOT_QUALIFIED,
    INVALID_ACCESS_RIGHTS,
    OCSP_INVALID_TIME_SLOT,
    CERTIFICATE_REVOKED,
    OLD_API,
    UNDER_MAINTENANCE,
    GENERAL_ERROR,
    NO_RESPONSE,
    INVALID_SSL_HANDSHAKE,
    TECHNICAL_ERROR,

    USER_CANCELLED,
}
