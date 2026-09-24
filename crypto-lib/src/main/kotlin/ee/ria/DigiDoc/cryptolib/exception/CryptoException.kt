// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

open class CryptoException(
    message: String,
    cause: Throwable,
) : Exception(message, cause) {
    constructor() : this("", Throwable())
    constructor(message: String) : this(message, Throwable())
}
