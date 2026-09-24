// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.exception

class WebEidException(
    val errorCode: WebEidErrorCode,
    override val message: String,
    val responseUri: String,
) : Exception()
