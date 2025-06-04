@file:Suppress("PackageName")

package ee.ria.DigiDoc.idcardlib

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.PEM_BEGIN_CERT
import ee.ria.DigiDoc.common.Constant.PEM_END_CERT
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.service.IdCardServiceImpl
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.PersonalData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.DigiDoc.utilsLib.signing.CertificateUtil
import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.runBlocking
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class IdCardServiceImplTest {
    private lateinit var context: Context
    private lateinit var idCardService: IdCardServiceImpl
    private lateinit var certificateService: CertificateService
    private lateinit var containerWrapper: ContainerWrapper
    private lateinit var containerFile: File

    private val token = mock(Token::class.java)
    private lateinit var existingContainer: SignedContainer

    private val cert =
        "MIIGGzCCBQOgAwIBAgIQDmRuJmtGcd4j6HiqQzw0hzANBgkqhkiG9w0BAQsFADBZ\n" +
            "MQswCQYDVQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypE\n" +
            "aWdpQ2VydCBHbG9iYWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjMw\n" +
            "ODMxMDAwMDAwWhcNMjQwOTMwMjM1OTU5WjBXMQswCQYDVQQGEwJFRTEQMA4GA1UE\n" +
            "BxMHVGFsbGlubjEhMB8GA1UECgwYUmlpZ2kgSW5mb3PDvHN0ZWVtaSBBbWV0MRMw\n" +
            "EQYDVQQDDAoqLmVlc3RpLmVlMHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEIbJjZD5M\n" +
            "fjpd2P6FDuNclnN0hp/1ANWr05wK6/Nl/BIR/rr702rV2Y17uoBukHA4TvChN3P8\n" +
            "YMHloK+TcXmjy+CQpRQtYUvm+meobN0NWSdKGASqtX9C4E6RYQKcs2mXo4IDjTCC\n" +
            "A4kwHwYDVR0jBBgwFoAUdIWAwGbH3zfez70pN6oDHb7tzRcwHQYDVR0OBBYEFB/b\n" +
            "eFjCUl4v17Qy2g1AgqvJwOaHMB8GA1UdEQQYMBaCCiouZWVzdGkuZWWCCGVlc3Rp\n" +
            "LmVlMA4GA1UdDwEB/wQEAwIHgDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUH\n" +
            "AwIwgZ8GA1UdHwSBlzCBlDBIoEagRIZCaHR0cDovL2NybDMuZGlnaWNlcnQuY29t\n" +
            "L0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3JsMEigRqBE\n" +
            "hkJodHRwOi8vY3JsNC5kaWdpY2VydC5jb20vRGlnaUNlcnRHbG9iYWxHMlRMU1JT\n" +
            "QVNIQTI1NjIwMjBDQTEtMS5jcmwwPgYDVR0gBDcwNTAzBgZngQwBAgIwKTAnBggr\n" +
            "BgEFBQcCARYbaHR0cDovL3d3dy5kaWdpY2VydC5jb20vQ1BTMIGHBggrBgEFBQcB\n" +
            "AQR7MHkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBRBggr\n" +
            "BgEFBQcwAoZFaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xv\n" +
            "YmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3J0MAkGA1UdEwQCMAAwggF+Bgor\n" +
            "BgEEAdZ5AgQCBIIBbgSCAWoBaAB2AO7N0GTV2xrOxVy3nbTNE6Iyh0Z8vOzew1FI\n" +
            "WUZxH7WbAAABikpp0YgAAAQDAEcwRQIhAOuRDRbH2F/4xj+4psS1uN7agonxJpSX\n" +
            "7l1m9CpJX/gkAiBFDEGuoEijUPdQ3M5ibV6YsXW4648t7mkR0W56XiNZYAB2AEiw\n" +
            "42vapkc0D+VqAvqdMOscUgHLVt0sgdm7v6s52IRzAAABikppz/EAAAQDAEcwRQIh\n" +
            "ALEE3j07957wr2WLsozkjmXPepYu5p/iTZx65kYtO47aAiAKS1VoZ0mMssYUcwmY\n" +
            "s5FB79zNnVW5rXD4heRSFvpT9AB2ANq2v2s/tbYin5vCu1xr6HCRcWy7UYSFNL2k\n" +
            "PTBI1/urAAABikpp0BkAAAQDAEcwRQIgOuq96euO9Aade5R6HfpNGEciZUfbgW+o\n" +
            "MmstOl3YqAUCIQDsafdu8nlmkNrN7h8uuqVXBqyv9J/u0WU80dAxPCGBiTANBgkq\n" +
            "hkiG9w0BAQsFAAOCAQEAaCYTTF6Sps1YXdD6kKiYkslaxzql6D/F9Imog4pJXRZH\n" +
            "7ye5kHuGOPFfnUQEqOziOspZCusX2Bz4DK4I/oc4cQnMxQHIDdF4H/GS/2aBbU/R\n" +
            "4Ustgxkd4PCdxOn6lVux8aFDCRKrNBrUF1/970StNuh8tatyYvDEenwC0F3l2hRB\n" +
            "Q3FYZMYkR9H8FM314a/sGST6lQiKJq2hrziMWilOwKxc88MBz9H9CYrEsCMI65iH\n" +
            "vWA8njofxSYdM5NHhxTxhHKn6qZxHSjiQvF9edUYTQ4wwTczmHuqYY2qxYh6WUzR\n" +
            "yaKSeng9fe8ZVZdjOwmCa9ZdgjQYMZbDezMt+oRp2Q=="

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
                                CentralConfigurationServiceImpl("Tests", ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    val configurationRepository =
                        ConfigurationRepositoryImpl(context, configurationLoader)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        runBlocking {
            context = InstrumentationRegistry.getInstrumentation().targetContext
            certificateService = mock(CertificateService::class.java)
            containerWrapper = mock(ContainerWrapper::class.java)
            idCardService = IdCardServiceImpl(context, containerWrapper, certificateService)

            containerFile =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            existingContainer =
                SignedContainer.openOrCreate(context, containerFile, listOf(containerFile), true)
        }
    }

    @Test
    fun idCardService_signContainer_success() {
        val certPemString = (PEM_BEGIN_CERT + "\n" + cert + "\n" + PEM_END_CERT).trimIndent()

        val testData =
            certPemString.let {
                CertificateUtil.x509Certificate(it).encoded
            }

        val mockPersonalData = mock(PersonalData::class.java)
        val keyUsage = mock(KeyUsage::class.java)
        val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)

        `when`(token.personalData()).thenReturn(mockPersonalData)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenReturn(mock(X509CertificateHolder::class.java))
        `when`(certificateService.extractEIDType(any()))
            .thenReturn(EIDType.ID_CARD)
        `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
        `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

        `when`(token.calculateSignature(anyOrNull(), anyOrNull(), anyBoolean())).thenReturn(testData)

        `when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
        `when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)

        doNothing().`when`(
            containerWrapper,
        ).finalizeSignature(any<ExternalSigner>(), eq(existingContainer), eq(testData))

        runBlocking {
            val signedContainer =
                idCardService.signContainer(
                    token,
                    existingContainer,
                    testData,
                    null,
                )

            assertNotNull(signedContainer)
        }
    }

    @Test
    fun idCardService_signContainer_successWithRoleData() {
        val certPemString = (PEM_BEGIN_CERT + "\n" + cert + "\n" + PEM_END_CERT).trimIndent()

        val testData =
            certPemString.let {
                CertificateUtil.x509Certificate(it).encoded
            }

        val mockPersonalData = mock(PersonalData::class.java)
        val keyUsage = mock(KeyUsage::class.java)
        val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)

        `when`(token.personalData()).thenReturn(mockPersonalData)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenReturn(mock(X509CertificateHolder::class.java))
        `when`(certificateService.extractEIDType(any()))
            .thenReturn(EIDType.ID_CARD)
        `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
        `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

        `when`(token.calculateSignature(anyOrNull(), anyOrNull(), anyBoolean())).thenReturn(testData)

        `when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
        `when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)

        doNothing().`when`(
            containerWrapper,
        ).finalizeSignature(any<ExternalSigner>(), eq(existingContainer), eq(testData))

        runBlocking {
            val signedContainer =
                idCardService.signContainer(
                    token,
                    existingContainer,
                    testData,
                    RoleData(
                        listOf("role"),
                        "city",
                        "state",
                        "country",
                        "zip",
                    ),
                )

            assertNotNull(signedContainer)
        }
    }

    @Test
    fun idCardService_data_success() =
        runBlocking {
            val testData = byteArrayOf(1, 2, 3)

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(mock(X509CertificateHolder::class.java))
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            `when`(token.calculateSignature(anyOrNull(), anyOrNull(), anyBoolean())).thenReturn(testData)

            `when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
            `when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)
            `when`(token.codeRetryCounter(CodeType.PIN1)).thenReturn(1)
            `when`(token.codeRetryCounter(CodeType.PIN2)).thenReturn(2)
            `when`(token.codeRetryCounter(CodeType.PUK)).thenReturn(3)

            val result = idCardService.data(token)

            assertEquals(token.personalData(), result.personalData)
            assertEquals(
                ExtendedCertificate.create(token.certificate(CertificateType.AUTHENTICATION), certificateService),
                result.authCertificate,
            )
            assertEquals(
                ExtendedCertificate.create(token.certificate(CertificateType.SIGNING), certificateService),
                result.signCertificate,
            )
            assertEquals(1, result.pin1RetryCount)
            assertEquals(2, result.pin2RetryCount)
            assertEquals(3, result.pukRetryCount)
        }

    @Test(expected = SmartCardReaderException::class)
    fun idCardService_data_throwExceptionWithPersonalData() {
        `when`(token.personalData()).thenThrow(SmartCardReaderException("Cannot get personal data"))

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

            doNothing().`when`(token).changeCode(codeType, currentPin, newPin)

            val result = idCardService.editPin(token, codeType, currentPin, newPin)

            assertTrue(result)
            verify(token).changeCode(codeType, currentPin, newPin)
        }

    @Test(expected = CodeVerificationException::class)
    fun idCardService_editPin_throwCodeVerificationException() {
        val codeType = CodeType.PIN1
        val currentPin = byteArrayOf(1, 2, 3)
        val newPin = byteArrayOf(4, 5, 6)

        doThrow(CodeVerificationException(CodeType.PIN1, 2))
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

        doThrow(SmartCardReaderException("Reader error"))
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

            doNothing().`when`(token).unblockAndChangeCode(currentPuk, codeType, newPin)

            val result = idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)

            assertTrue(result)
            verify(token).unblockAndChangeCode(currentPuk, codeType, newPin)
        }

    @Test(expected = CodeVerificationException::class)
    fun idCardService_unblockAndEditPin_throwCodeVerificationException() {
        val codeType = CodeType.PIN1
        val currentPuk = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val newPin = byteArrayOf(4, 5, 6)

        doThrow(CodeVerificationException(CodeType.PIN1, 2))
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

        doThrow(SmartCardReaderException("Reader error"))
            .`when`(token)
            .unblockAndChangeCode(currentPuk, codeType, newPin)

        runBlocking {
            idCardService.unblockAndEditPin(token, codeType, currentPuk, newPin)
        }
    }
}
