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

package ee.ria.DigiDoc.domain.service

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.PersonalData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing

@RunWith(MockitoJUnitRunner::class)
class IdCardServiceImplTest {
    private lateinit var idCardService: IdCardServiceImpl
    private lateinit var certificateService: CertificateService

    private val token = Mockito.mock(Token::class.java)

    companion object {
        private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
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
                    val configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        certificateService = Mockito.mock(CertificateService::class.java)
        idCardService = IdCardServiceImpl(certificateService)
    }

    @Test
    fun idCardService_data_success() =
        runBlocking {
            val testData = byteArrayOf(1, 2, 3)

            val mockPersonalData = Mockito.mock(PersonalData::class.java)
            val keyUsage = Mockito.mock(KeyUsage::class.java)
            val extendedKeyUsage = Mockito.mock(ExtendedKeyUsage::class.java)

            Mockito.`when`(token.personalData()).thenReturn(mockPersonalData)

            Mockito
                .`when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(Mockito.mock(X509CertificateHolder::class.java))
            Mockito
                .`when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            Mockito.`when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            Mockito
                .`when`(certificateService.extractExtendedKeyUsage(any()))
                .thenReturn(extendedKeyUsage)

            Mockito
                .`when`(
                    token.calculateSignature(
                        anyOrNull(),
                        anyOrNull(),
                        ArgumentMatchers.anyBoolean(),
                    ),
                ).thenReturn(testData)

            Mockito.`when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
            Mockito.`when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN1)).thenReturn(1)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN2)).thenReturn(2)
            Mockito.`when`(token.codeRetryCounter(CodeType.PUK)).thenReturn(3)

            val result = idCardService.data(token)

            Assert.assertEquals(token.personalData(), result.personalData)
            Assert.assertEquals(
                ExtendedCertificate.Companion.create(
                    token.certificate(CertificateType.AUTHENTICATION),
                    certificateService,
                ),
                result.authCertificate,
            )
            Assert.assertEquals(
                ExtendedCertificate.Companion.create(
                    token.certificate(CertificateType.SIGNING),
                    certificateService,
                ),
                result.signCertificate,
            )
            Assert.assertEquals(1, result.pin1RetryCount)
            Assert.assertEquals(2, result.pin2RetryCount)
            Assert.assertEquals(3, result.pukRetryCount)
        }

    @Test(expected = SmartCardReaderException::class)
    fun idCardService_data_throwExceptionWithPersonalData() {
        Mockito
            .`when`(token.personalData())
            .thenThrow(SmartCardReaderException("Cannot get personal data"))

        runBlocking {
            idCardService.data(token)
        }
    }

    @Test
    fun idCardService_editPin_success() =
        runBlocking {
            val codeType = CodeType.PIN1
            val currentPin = byteArrayOf(1, 2, 3)
            val newPin = byteArrayOf(4, 5, 6)

            val testData = byteArrayOf(1, 2, 3)

            val mockPersonalData = Mockito.mock(PersonalData::class.java)
            val keyUsage = Mockito.mock(KeyUsage::class.java)
            val extendedKeyUsage = Mockito.mock(ExtendedKeyUsage::class.java)

            Mockito.`when`(token.personalData()).thenReturn(mockPersonalData)

            Mockito
                .`when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(Mockito.mock(X509CertificateHolder::class.java))
            Mockito
                .`when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            Mockito.`when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            Mockito
                .`when`(certificateService.extractExtendedKeyUsage(any()))
                .thenReturn(extendedKeyUsage)

            Mockito
                .`when`(
                    token.calculateSignature(
                        anyOrNull(),
                        anyOrNull(),
                        ArgumentMatchers.anyBoolean(),
                    ),
                ).thenReturn(testData)

            Mockito.`when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
            Mockito.`when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN1)).thenReturn(1)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN2)).thenReturn(2)
            Mockito.`when`(token.codeRetryCounter(CodeType.PUK)).thenReturn(3)

            doNothing().`when`(token).changeCode(codeType, currentPin, newPin)

            val result = idCardService.editPin(token, codeType, currentPin, newPin)

            Assert.assertNotNull(result)
            Assert.assertEquals(mockPersonalData, result.personalData)
            Mockito.verify(token).changeCode(codeType, currentPin, newPin)
        }

    @Test(expected = CodeVerificationException::class)
    fun idCardService_editPin_throwCodeVerificationException() {
        val codeType = CodeType.PIN1
        val currentPin = byteArrayOf(1, 2, 3)
        val newPin = byteArrayOf(4, 5, 6)

        Mockito
            .doThrow(CodeVerificationException(CodeType.PIN1, 2))
            .`when`(token)
            .changeCode(codeType, currentPin, newPin)

        runBlocking {
            idCardService.editPin(token, codeType, currentPin, newPin)
        }
    }

    @Test(expected = SmartCardReaderException::class)
    fun idCardService_editPin_throwSmartCardReaderException() {
        val codeType = CodeType.PIN1
        val currentPin = byteArrayOf(1, 2, 3)
        val newPin = byteArrayOf(4, 5, 6)

        Mockito
            .doThrow(SmartCardReaderException("Reader error"))
            .`when`(token)
            .changeCode(codeType, currentPin, newPin)

        runBlocking {
            idCardService.editPin(token, codeType, currentPin, newPin)
        }
    }

    @Test
    fun idCardService_unblockAndEditPin_success() =
        runBlocking {
            val codeType = CodeType.PIN1
            val currentPuk = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
            val newPin = byteArrayOf(4, 5, 6)

            val testData = byteArrayOf(1, 2, 3)

            val mockPersonalData = Mockito.mock(PersonalData::class.java)
            val keyUsage = Mockito.mock(KeyUsage::class.java)
            val extendedKeyUsage = Mockito.mock(ExtendedKeyUsage::class.java)

            Mockito.`when`(token.personalData()).thenReturn(mockPersonalData)

            Mockito
                .`when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(Mockito.mock(X509CertificateHolder::class.java))
            Mockito
                .`when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            Mockito.`when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            Mockito
                .`when`(certificateService.extractExtendedKeyUsage(any()))
                .thenReturn(extendedKeyUsage)

            Mockito
                .`when`(
                    token.calculateSignature(
                        anyOrNull(),
                        anyOrNull(),
                        ArgumentMatchers.anyBoolean(),
                    ),
                ).thenReturn(testData)

            Mockito.`when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
            Mockito.`when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN1)).thenReturn(1)
            Mockito.`when`(token.codeRetryCounter(CodeType.PIN2)).thenReturn(2)
            Mockito.`when`(token.codeRetryCounter(CodeType.PUK)).thenReturn(3)

            doNothing().`when`(token).unblockAndChangeCode(currentPuk, codeType, newPin)

            val result = idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)

            Assert.assertNotNull(result)
            Assert.assertEquals(mockPersonalData, result.personalData)
            Mockito.verify(token).unblockAndChangeCode(currentPuk, codeType, newPin)
        }

    @Test(expected = CodeVerificationException::class)
    fun idCardService_unblockAndEditPin_throwCodeVerificationException() {
        val codeType = CodeType.PIN1
        val currentPuk = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val newPin = byteArrayOf(4, 5, 6)

        Mockito
            .doThrow(CodeVerificationException(CodeType.PIN1, 2))
            .`when`(token)
            .unblockAndChangeCode(currentPuk, codeType, newPin)

        runBlocking {
            idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)
        }
    }

    @Test(expected = SmartCardReaderException::class)
    fun idCardService_unblockAndChangeCode_throwSmartCardReaderException() {
        val codeType = CodeType.PIN1
        val currentPuk = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val newPin = byteArrayOf(4, 5, 6)

        Mockito
            .doThrow(SmartCardReaderException("Reader error"))
            .`when`(token)
            .unblockAndChangeCode(currentPuk, codeType, newPin)

        runBlocking {
            idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)
        }
    }
}
