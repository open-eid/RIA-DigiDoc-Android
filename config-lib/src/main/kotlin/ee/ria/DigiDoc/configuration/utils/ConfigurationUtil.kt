@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object ConfigurationUtil {
    fun isSerialNewerThanCached(
        cachedSerial: Int?,
        newSerial: Int,
    ): Boolean =
        when {
            cachedSerial == null -> true
            else -> {
                newSerial > cachedSerial
            }
        }

    @OptIn(ExperimentalEncodingApi::class)
    fun isBase64(encoded: String): Boolean =
        try {
            Base64.decode(encoded)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
}
