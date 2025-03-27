@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import ee.ria.cdoc.CDoc
import ee.ria.cdoc.CryptoBackend
import ee.ria.cdoc.DataBuffer

interface AbstractSmartToken {
    @Throws(Exception::class)
    fun getCertificate(): ByteArray

    @Throws(Exception::class)
    fun derive(
        publicKey: ByteArray,
        pin1: String,
    ): ByteArray

    @Throws(Exception::class)
    fun decrypt(
        data: ByteArray,
        pin1: String,
    ): ByteArray

    @Throws(Exception::class)
    fun authenticate(
        digest: ByteArray,
        pin1: String,
    ): ByteArray
}

class SmartCardTokenWrapper(
    private val password: String,
    private val smartToken: AbstractSmartToken,
) {
    private var lastError: Throwable? = null

    fun getLastError(): Throwable? = lastError

    fun cert(): ByteArray {
        return try {
            smartToken.getCertificate().also {
                lastError = null
            }
        } catch (e: Exception) {
            lastError = e
            byteArrayOf()
        }
    }

    fun deriveECDH1(
        dst: DataBuffer,
        publicKey: ByteArray,
        idx: Int,
    ): Int {
        var data = byteArrayOf()
        try {
            data = smartToken.derive(publicKey, password)
            dst.data = data
        } catch (e: Exception) {
            lastError = e
        }

        return if (data.isNotEmpty()) {
            CDoc.OK
        } else {
            CDoc.CRYPTO_ERROR
        }
    }

    fun decryptRSA(
        dst: DataBuffer,
        data: ByteArray,
        oaep: Boolean,
        idx: Int,
    ): Int {
        var decryptedData = byteArrayOf()
        try {
            decryptedData = smartToken.decrypt(data, password)
            dst.data = decryptedData
        } catch (e: Exception) {
            lastError = e
        }

        return if (decryptedData.isNotEmpty()) {
            CDoc.OK
        } else {
            CDoc.CRYPTO_ERROR
        }
    }

    fun sign(
        dst: DataBuffer,
        algorithm: CryptoBackend.HashAlgorithm,
        digest: ByteArray,
        idx: Int,
    ): Int {
        var data = byteArrayOf()
        try {
            data = smartToken.authenticate(digest, password)
            dst.data = data
        } catch (e: Exception) {
            lastError = e
        }

        return if (data.isNotEmpty()) {
            CDoc.OK
        } else {
            CDoc.CRYPTO_ERROR
        }
    }
}
