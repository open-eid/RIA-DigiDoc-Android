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

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.CertificatePolicies
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.PolicyInformation
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

class SignatureWrapperTest {
    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    val configurationLoader =
                        ConfigurationLoaderImpl(
                            Gson(),
                            CentralConfigurationRepositoryImpl(
                                CentralConfigurationServiceImpl(context, ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    val configurationRepository = ConfigurationRepositoryImpl(configurationLoader)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context
    private lateinit var container: File

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        container = getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)
    }

    @Test
    fun signatureWrapper_writeObject_roundTripsSignatureReadFromContainer() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)
            val signature =
                signedContainer.getSignatures().firstOrNull { it.signerRoles.isNotEmpty() }
            assertNotNull("No signature with signer roles found in example.asice", signature)
            requireNotNull(signature)

            val bytes = ByteArrayOutputStream()
            ObjectOutputStream(bytes).use { it.writeObject(signature) }
            val restored =
                ObjectInputStream(ByteArrayInputStream(bytes.toByteArray())).use {
                    it.readObject() as SignatureInterface
                }

            assertEquals(signature.id, restored.id)
            assertEquals(signature.signedBy, restored.signedBy)
            assertEquals(listOf("Roll"), restored.signerRoles)
            assertEquals(signature.validator.status, restored.validator.status)
            assertEquals(signature.validator.diagnostics, restored.validator.diagnostics)
            assertArrayEquals(signature.dataToSign, restored.dataToSign)
            assertArrayEquals(signature.messageImprint, restored.messageImprint)
            assertArrayEquals(signature.signingCertificateDer, restored.signingCertificateDer)
            assertArrayEquals(signature.ocspCertificateDer, restored.ocspCertificateDer)
            assertArrayEquals(signature.timeStampCertificateDer, restored.timeStampCertificateDer)
            assertArrayEquals(
                signature.archiveTimeStampCertificateDer,
                restored.archiveTimeStampCertificateDer,
            )
            assertEquals(signature.isDigitalSeal, restored.isDigitalSeal)
        }

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
