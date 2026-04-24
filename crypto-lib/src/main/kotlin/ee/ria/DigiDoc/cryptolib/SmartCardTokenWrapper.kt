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
