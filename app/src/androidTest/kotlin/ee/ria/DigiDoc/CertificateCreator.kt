@file:Suppress("PackageName")

package ee.ria.DigiDoc

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class CertificateCreator {
    companion object {
        @Throws(CertificateException::class, NoSuchAlgorithmException::class)
        fun createSelfSignedCertificate(): X509Certificate? {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048)
            val keyPair: KeyPair = keyPairGenerator.generateKeyPair()

            val notBefore = Date()
            val notAfter = Date(notBefore.time + 10 * 60 * 1000L)

            val serialNumber = BigInteger.valueOf(System.currentTimeMillis())

            val nameBuilder = X500NameBuilder(BCStyle.INSTANCE)
            nameBuilder.addRDN(BCStyle.C, "EE")
            nameBuilder.addRDN(BCStyle.O, "Test Organization")
            nameBuilder.addRDN(BCStyle.CN, "Test Common Name")

            val jcaX509v3CertificateBuilder =
                JcaX509v3CertificateBuilder(
                    X500Principal(nameBuilder.build().toString()),
                    serialNumber,
                    notBefore,
                    notAfter,
                    X500Principal(nameBuilder.build().toString()),
                    keyPair.public,
                )

            jcaX509v3CertificateBuilder.addExtension(
                ASN1ObjectIdentifier(BCStyle.C.id),
                false,
                DEROctetString("EE".toByteArray()),
            )

            val jcaContentSigner = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
            val certificateHolder = jcaX509v3CertificateBuilder.build(jcaContentSigner)
            return JcaX509CertificateConverter().getCertificate(certificateHolder)
        }
    }
}
