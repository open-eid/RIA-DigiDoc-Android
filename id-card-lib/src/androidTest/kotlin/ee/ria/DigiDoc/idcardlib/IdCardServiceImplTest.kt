@file:Suppress("PackageName")

package ee.ria.DigiDoc.idcardlib

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.EIDType
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
import ee.ria.DigiDoc.idcard.PersonalData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import kotlinx.coroutines.runBlocking
import org.bouncycastle.cert.X509CertificateHolder
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
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
            idCardService = IdCardServiceImpl(containerWrapper, certificateService)

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
        val testData = byteArrayOf(1, 2, 3)

        val mockPersonalData = mock(PersonalData::class.java)

        `when`(token.personalData()).thenReturn(mockPersonalData)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenReturn(mock(X509CertificateHolder::class.java))
        `when`(certificateService.extractEIDType(any()))
            .thenReturn(EIDType.ID_CARD)

        `when`(token.calculateSignature(anyOrNull(), anyOrNull(), anyBoolean())).thenReturn(testData)

        `when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
        `when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)

        doNothing().`when`(containerWrapper).finalizeSignature(existingContainer, testData)

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
        val testData = byteArrayOf(1, 2, 3)

        val mockPersonalData = mock(PersonalData::class.java)

        `when`(token.personalData()).thenReturn(mockPersonalData)

        `when`(certificateService.parseCertificate(anyOrNull()))
            .thenReturn(mock(X509CertificateHolder::class.java))
        `when`(certificateService.extractEIDType(any()))
            .thenReturn(EIDType.ID_CARD)

        `when`(token.calculateSignature(anyOrNull(), anyOrNull(), anyBoolean())).thenReturn(testData)

        `when`(token.certificate(CertificateType.AUTHENTICATION)).thenReturn(testData)
        `when`(token.certificate(CertificateType.SIGNING)).thenReturn(testData)

        doNothing().`when`(containerWrapper).finalizeSignature(existingContainer, testData)

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
}
