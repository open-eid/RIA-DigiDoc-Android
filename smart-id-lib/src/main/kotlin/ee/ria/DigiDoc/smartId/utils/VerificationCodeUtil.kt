@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId.utils

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object VerificationCodeUtil {
    @Throws(NoSuchAlgorithmException::class)
    fun calculateSmartIdVerificationCode(hash: ByteArray?): String {
        val codeDigest = hash?.let { MessageDigest.getInstance("SHA-256").digest(it) }
        val code =
            (
                codeDigest
                    ?.let {
                        ByteBuffer
                            .wrap(it)
                            .getShort((codeDigest.size) - 2)
                            .toInt()
                    }?.and(0xffff)
            ).toString()
        val paddedCode = "0000$code"
        return paddedCode.substring(code.length)
    }
}
