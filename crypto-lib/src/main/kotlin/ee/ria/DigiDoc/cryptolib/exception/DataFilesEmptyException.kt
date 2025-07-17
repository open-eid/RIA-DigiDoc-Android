@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

class DataFilesEmptyException(
    message: String,
    cause: Throwable,
) : CryptoException(message, cause) {
    constructor() : this("", Throwable())
    constructor(message: String) : this(message, Throwable())
}
