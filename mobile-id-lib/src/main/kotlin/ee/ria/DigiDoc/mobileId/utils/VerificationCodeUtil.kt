@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.utils

import org.bouncycastle.util.encoders.Base64
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Locale

object VerificationCodeUtil {
    @Throws(NoSuchAlgorithmException::class)
    fun calculateSmartIdVerificationCode(base64Hash: String?): String {
        val hash: ByteArray = Base64.decode(base64Hash)
        val codeDigest = MessageDigest.getInstance("SHA-256").digest(hash)
        val code =
            (
                ByteBuffer.wrap(codeDigest).getShort(codeDigest.size - 2)
                    .toInt() and 0xffff
            ).toString()
        val paddedCode = "0000$code"
        return paddedCode.substring(code.length)
    }

    fun calculateMobileIdVerificationCode(hash: ByteArray?): String {
        return String.format(
            Locale.ROOT,
            "%04d",
            if (hash != null) ((0xFC and hash[0].toInt()) shl 5) or (hash[hash.size - 1].toInt() and 0x7F) else 0,
        )
    }
}
