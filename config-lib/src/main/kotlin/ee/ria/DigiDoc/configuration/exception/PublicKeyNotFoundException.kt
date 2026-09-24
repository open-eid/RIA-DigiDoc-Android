// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.exception

class PublicKeyNotFoundException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
