@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.repository

import android.content.Context
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.exception.CryptoException

interface RecipientRepository {
    @Throws(CryptoException::class, NoInternetConnectionException::class)
    suspend fun find(
        context: Context,
        query: String,
    ): List<Addressee>
}
