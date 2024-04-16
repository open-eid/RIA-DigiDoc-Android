@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.utils

import ee.ria.DigiDoc.utilslib.logging.LoggingUtil.errorLog
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo
import org.bouncycastle.crypto.params.RSAKeyParameters
import org.bouncycastle.crypto.util.PublicKeyFactory
import org.bouncycastle.openssl.PEMParser
import java.io.IOException
import java.io.StringReader
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.RSAPublicKeySpec

object SignatureVerifier {
    private val LOG_TAG = javaClass.simpleName

    fun verify(
        signature: ByteArray,
        publicKeyPEM: String,
        signedContent: String,
    ): Boolean {
        val publicKeyInfo: SubjectPublicKeyInfo =
            parsePublicKeyInfo(publicKeyPEM)
        val publicKey = convertPublicKeyInfoToPublicKey(publicKeyInfo)
        return verifySignature(signature, publicKey, signedContent)
    }

    private fun convertPublicKeyInfoToPublicKey(publicKeyInfo: SubjectPublicKeyInfo): PublicKey {
        return try {
            val keyParams: RSAKeyParameters =
                PublicKeyFactory.createKey(publicKeyInfo) as RSAKeyParameters
            val publicKeySpec = RSAPublicKeySpec(keyParams.modulus, keyParams.exponent)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(publicKeySpec)
        } catch (e: InvalidKeySpecException) {
            errorLog(LOG_TAG, "PublicKey conversion failed", e)
            throw IllegalStateException(
                "Failed to convert org.bouncycastle.asn1.x509.SubjectPublicKeyInfo to java.security.PublicKey",
                e,
            )
        } catch (e: NoSuchAlgorithmException) {
            errorLog(LOG_TAG, "PublicKey conversion failed", e)
            throw IllegalStateException(
                "Failed to convert org.bouncycastle.asn1.x509.SubjectPublicKeyInfo to java.security.PublicKey",
                e,
            )
        } catch (e: IOException) {
            errorLog(LOG_TAG, "PublicKey conversion failed", e)
            throw IllegalStateException(
                "Failed to convert org.bouncycastle.asn1.x509.SubjectPublicKeyInfo to java.security.PublicKey",
                e,
            )
        }
    }

    private fun parsePublicKeyInfo(PKCS1PublicKeyPEM: String): SubjectPublicKeyInfo {
        try {
            PEMParser(StringReader(PKCS1PublicKeyPEM))
                .use { pemParser -> return pemParser.readObject() as SubjectPublicKeyInfo }
        } catch (e: IOException) {
            throw IllegalStateException("Failed to parse PEM encoded PKCS#1 public key", e)
        }
    }

    private fun verifySignature(
        signatureBytes: ByteArray,
        publicKey: PublicKey,
        signedContent: String,
    ): Boolean {
        return try {
            val signature = Signature.getInstance("SHA512withRSA")
            signature.initVerify(publicKey)
            signature.update(signedContent.toByteArray(StandardCharsets.UTF_8))
            signature.verify(signatureBytes)
        } catch (e: NoSuchAlgorithmException) {
            errorLog(LOG_TAG, "Signature verification failed", e)
            throw IllegalStateException("Failed to verify signature", e)
        } catch (e: SignatureException) {
            errorLog(LOG_TAG, "Signature verification failed", e)
            throw IllegalStateException("Failed to verify signature", e)
        } catch (e: InvalidKeyException) {
            errorLog(LOG_TAG, "Signature verification failed", e)
            throw IllegalStateException("Failed to verify signature", e)
        }
    }
}
