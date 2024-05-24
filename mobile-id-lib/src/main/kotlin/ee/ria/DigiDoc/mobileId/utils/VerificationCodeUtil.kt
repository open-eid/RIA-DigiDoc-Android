@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.utils

import java.util.Locale

object VerificationCodeUtil {
    fun calculateMobileIdVerificationCode(hash: ByteArray?): String {
        return String.format(
            Locale.ROOT,
            "%04d",
            if (hash != null) ((0xFC and hash[0].toInt()) shl 5) or (hash[hash.size - 1].toInt() and 0x7F) else 0,
        )
    }
}
