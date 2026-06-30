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

package ee.ria.DigiDoc.libdigidoclib.domain.model

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class SignatureWrapperTest {
    @Test
    fun signatureWrapper_isCertificateDigitalSeal_trueForESealOid_7_3() {
        val certDer = generateCertWithPolicy("1.3.6.1.4.1.10015.7.3")
        assertTrue(SignatureWrapper.isCertificateDigitalSeal(certDer))
    }

    @Test
    fun signatureWrapper_isCertificateDigitalSeal_trueForESealOid_7_1() {
        val certDer = generateCertWithPolicy("1.3.6.1.4.1.10015.7.1")
        assertTrue(SignatureWrapper.isCertificateDigitalSeal(certDer))
    }

    @Test
    fun signatureWrapper_isCertificateDigitalSeal_trueForESealOid_2_1() {
        val certDer = generateCertWithPolicy("1.3.6.1.4.1.10015.2.1")
        assertTrue(SignatureWrapper.isCertificateDigitalSeal(certDer))
    }

    @Test
    fun signatureWrapper_isCertificateDigitalSeal_falseForIdCardOid() {
        val certDer = generateCertWithPolicy("1.3.6.1.4.1.10015.1.1")
        assertFalse(SignatureWrapper.isCertificateDigitalSeal(certDer))
    }

    @Test
    fun signatureWrapper_isCertificateDigitalSeal_falseForEmptyByteArray() {
        assertFalse(SignatureWrapper.isCertificateDigitalSeal(ByteArray(0)))
    }

    private fun generateCertWithPolicy(policyOid: String): ByteArray {
        val keyPairGen = KeyPairGenerator.getInstance("RSA")
        keyPairGen.initialize(2048)
        val keyPair = keyPairGen.generateKeyPair()

        val now = Date()
        val notAfter = Date.from(Instant.now().plus(365, ChronoUnit.DAYS))
        val subject = X500Name("CN=Test")

        val builder = JcaX509v3CertificateBuilder(subject, BigInteger.ONE, now, notAfter, subject, keyPair.public)
        builder.addExtension(
            Extension.certificatePolicies,
            false,
            CertificatePolicies(arrayOf(PolicyInformation(ASN1ObjectIdentifier(policyOid)))),
        )

        val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
        return builder.build(signer).encoded
    }
}
