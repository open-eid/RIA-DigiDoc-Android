@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import java.util.Base64

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

    fun isBase64(encoded: String): Boolean =
        try {
            Base64.getDecoder().decode(encoded)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
}
