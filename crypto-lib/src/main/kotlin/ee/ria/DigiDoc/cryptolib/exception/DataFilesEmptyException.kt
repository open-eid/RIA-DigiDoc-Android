// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

class DataFilesEmptyException(
    message: String,
    cause: Throwable,
) : CryptoException(message, cause) {
    constructor() : this("", Throwable())
    constructor(message: String) : this(message, Throwable())
}
