@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib

import ee.ria.DigiDoc.idcard.Token
import ee.ria.cdoc.CDoc
import ee.ria.cdoc.CryptoBackend
import ee.ria.cdoc.DataBuffer

open class SmartCardTokenWrapper(
    private val pin: ByteArray,
    private val smartToken: Token,
) : CryptoBackend() {
    var lastError: Throwable? = null

    override fun deriveECDH1(
        dst: DataBuffer,
        publicKey: ByteArray,
        idx: Int,
    ): Long {
        var data = byteArrayOf()
        try {
            data = smartToken.decrypt(pin, publicKey, true)
            dst.data = data
        } catch (e: Exception) {
            lastError = e
        }

        return if (data.isNotEmpty()) {
            CDoc.OK.toLong()
        } else {
            CDoc.CRYPTO_ERROR.toLong()
        }
    }

    override fun decryptRSA(
        dst: DataBuffer,
        data: ByteArray,
        oaep: Boolean,
        idx: Int,
    ): Long {
        var decryptedData = byteArrayOf()
        try {
            decryptedData = smartToken.decrypt(pin, data, true)
            dst.data = decryptedData
        } catch (e: Exception) {
            lastError = e
        }

        return if (decryptedData.isNotEmpty()) {
            CDoc.OK.toLong()
        } else {
            CDoc.CRYPTO_ERROR.toLong()
        }
    }

    override fun sign(
        dst: DataBuffer,
        algorithm: HashAlgorithm,
        digest: ByteArray,
        idx: Int,
    ): Long {
        var data = byteArrayOf()
        try {
            data = smartToken.authenticate(pin, digest)
            dst.data = data
        } catch (e: Exception) {
            lastError = e
        }

        return if (data.isNotEmpty()) {
            CDoc.OK.toLong()
        } else {
            CDoc.CRYPTO_ERROR.toLong()
        }
    }
}
