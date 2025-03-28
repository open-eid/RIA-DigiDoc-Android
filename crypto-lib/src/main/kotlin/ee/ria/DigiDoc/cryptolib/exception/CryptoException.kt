@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.exception

open class CryptoException(message: String, cause: Throwable) : Exception(message, cause) {
    constructor() : this("", Throwable())
    constructor(message: String) : this(message, Throwable())
}
