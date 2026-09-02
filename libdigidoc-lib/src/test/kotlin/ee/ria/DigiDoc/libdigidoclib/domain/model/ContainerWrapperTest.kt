@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.cert.CertificateException
import java.security.cert.CertificateExpiredException
import java.security.cert.CertificateNotYetValidException
import java.util.Calendar
import java.util.Date

class ContainerWrapperTest {
    private val containerWrapper = ContainerWrapperImpl()
    private val signer = mock<ExternalSigner>()

    @Test
    fun containerWrapper_prepareSignature_throwsWhenSigningCertificateHasExpired() {
        val expiredCertificate = certificate(fromYearsFromNow = -2, toYearsFromNow = -1)

        assertThrows(CertificateExpiredException::class.java) {
            runBlocking { containerWrapper.prepareSignature(signer, null, expiredCertificate, null) }
        }
    }

    @Test
    fun containerWrapper_prepareSignature_throwsWhenSigningCertificateIsNotYetValid() {
        val futureCertificate = certificate(fromYearsFromNow = 1, toYearsFromNow = 2)

        assertThrows(CertificateNotYetValidException::class.java) {
            runBlocking { containerWrapper.prepareSignature(signer, null, futureCertificate, null) }
        }
    }

    @Test
    fun containerWrapper_prepareSignature_throwsCertificateExceptionWhenCertificateMissing() {
        val exception =
            assertThrows(CertificateException::class.java) {
                runBlocking { containerWrapper.prepareSignature(signer, null, null, null) }
            }

        assertTrue(exception.message?.contains("missing signing certificate") == true)
    }

    @Test
    fun containerWrapper_prepareSignature_passesCertificateCheckWhenCertificateIsCurrentlyValid() {
        val validCertificate = certificate(fromYearsFromNow = -1, toYearsFromNow = 1)

        val exception =
            assertThrows(IllegalStateException::class.java) {
                runBlocking { containerWrapper.prepareSignature(signer, null, validCertificate, null) }
            }

        assertTrue(exception.message?.contains("Unable to get container") == true)
    }

    private fun certificate(
        fromYearsFromNow: Int,
        toYearsFromNow: Int,
    ): ByteArray {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
        val name = X500Name("CN=RIA DigiDoc test")
        val builder =
            JcaX509v3CertificateBuilder(
                name,
                BigInteger.valueOf(1),
                yearsFromNow(fromYearsFromNow),
                yearsFromNow(toYearsFromNow),
                name,
                keyPair.public,
            )
        val contentSigner = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
        return JcaX509CertificateConverter().getCertificate(builder.build(contentSigner)).encoded
    }

    private fun yearsFromNow(years: Int): Date =
        Calendar
            .getInstance()
            .apply { add(Calendar.YEAR, years) }
            .time
}
