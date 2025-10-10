@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.IdCardDataCreator.Companion.createMockIdCardData
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.PersonalData
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.smartcardreader.SmartCardReader
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderManager
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.bouncycastle.asn1.x509.ExtendedKeyUsage
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.util.encoders.Hex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class IdCardViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var resources: Resources

    @Mock
    private lateinit var idCardService: IdCardService

    @Mock
    private lateinit var certificateService: CertificateService

    @Mock
    private lateinit var smartCardReaderManager: SmartCardReaderManager

    @Mock
    private lateinit var x509CertificateHolder: X509CertificateHolder

    @Mock
    private lateinit var token: Token

    @Mock
    private lateinit var mockSmartCardReader: SmartCardReader

    @Mock
    private lateinit var mockComponentActivity: ComponentActivity

    private lateinit var container: File

    private lateinit var context: Context

    private lateinit var viewModel: IdCardViewModel

    private lateinit var cdoc2Settings: CDOC2Settings

    private lateinit var configurationRepository: ConfigurationRepository

    companion object {
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    val configurationRepository = mock(ConfigurationRepository::class.java)
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.IDLE))
        `when`(mockContext.resources).thenReturn(resources)
        `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)
        `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
        cdoc2Settings = CDOC2Settings(context)
        configurationRepository = mock(ConfigurationRepository::class.java)
        viewModel = IdCardViewModel(smartCardReaderManager, idCardService, cdoc2Settings, configurationRepository)

        container =
            AssetFile.getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )
    }

    @Test
    fun idCardViewModel_idCardStatus_success() {
        runTest {
            val idCardStatus = viewModel.idCardStatus.value

            assertEquals(SmartCardReaderStatus.IDLE, idCardStatus)
        }
    }

    @Test
    fun idCardViewModel_loadPersonalData_success() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val mockIdCardData = createMockIdCardData()
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)

            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockIdCardData.personalData)

            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockIdCardData.personalData,
                    mockIdCardData.authCertificate,
                    mockIdCardData.signCertificate,
                    3,
                    3,
                    3,
                    true,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)

            viewModel.loadPersonalData()

            val userDataValue = viewModel.userData.value
            assertEquals(mockIdCardData, userDataValue)
        }

    @Test
    fun idCardViewModel_loadPersonalData_resetValuesAfterException() =
        runTest {
            viewModel.loadPersonalData()

            assertNull(viewModel.signStatus.value)
            assertNull(viewModel.dialogError.value)
            assertEquals(SmartCardReaderStatus.IDLE, viewModel.idCardStatus.value)
            assertNull(viewModel.userData.value)
            assertNull(viewModel.errorState.value)
            assertNull(viewModel.pinErrorState.value)
            assertNull(viewModel.signStatus.value)
            assertNull(viewModel.signedContainer.value)
        }

    @Test
    fun idCardViewModel_sign_success() =
        runBlocking {
            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)
            `when`(resources.getString(any())).thenReturn("Mocked String")
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)

            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(
                idCardService.signContainer(any(), eq(signedContainer), eq(pin2), eq(null)),
            ).thenReturn(signedContainer)

            val testData = byteArrayOf(1, 2, 3)
            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            val certificate = ExtendedCertificate.create(testData, certificateService)

            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockPersonalData,
                    certificate,
                    certificate,
                    3,
                    3,
                    3,
                    true,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            val signStatus = viewModel.signStatus.value

            if (signStatus != null) {
                assertTrue(signStatus)
            } else {
                fail("signStatus is null")
            }

            assertNotNull(viewModel.signedContainer.value)
        }

    @Test
    fun idCardViewModel_sign_handleWrongPin2Exception2Retries() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)

            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            val testData = byteArrayOf(1, 2, 3)
            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            val certificate = ExtendedCertificate.create(testData, certificateService)

            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockPersonalData,
                    certificate,
                    certificate,
                    3,
                    2,
                    3,
                    true,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                CodeVerificationException(CodeType.PIN2, 2),
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            val signStatusValue = viewModel.signStatus.value
            if (signStatusValue != null) {
                assertFalse(signStatusValue)
            } else {
                fail("signStatusValue is null")
            }
            assertNull(viewModel.signedContainer.value)
            assertEquals(
                Triple(R.string.id_card_sign_pin_invalid, CodeType.PIN2.name, 2),
                viewModel.pinErrorState.value,
            )
        }

    @Test
    fun idCardViewModel_sign_handleWrongPin2Exception1Retry() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)

            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            val testData = byteArrayOf(1, 2, 3)
            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            val certificate = ExtendedCertificate.create(testData, certificateService)

            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockPersonalData,
                    certificate,
                    certificate,
                    3,
                    1,
                    3,
                    true,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                CodeVerificationException(CodeType.PIN2, 1),
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            val signStatusValue = viewModel.signStatus.value
            if (signStatusValue != null) {
                assertFalse(signStatusValue)
            } else {
                fail("signStatusValue is null")
            }
            assertNull(viewModel.signedContainer.value)
            assertEquals(
                Triple(R.string.id_card_sign_pin_invalid_final, CodeType.PIN2.name, null),
                viewModel.pinErrorState.value,
            )
        }

    @Test
    fun idCardViewModel_sign_handleWrongPin2ExceptionPinLocked() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)

            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            val testData = byteArrayOf(1, 2, 3)
            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)

            val certificate = ExtendedCertificate.create(testData, certificateService)

            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockPersonalData,
                    certificate,
                    certificate,
                    3,
                    0,
                    3,
                    true,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                CodeVerificationException(CodeType.PIN2, 0),
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            val signStatusValue = viewModel.signStatus.value
            if (signStatusValue != null) {
                assertFalse(signStatusValue)
            } else {
                fail("signStatusValue is null")
            }
            assertNull(viewModel.signedContainer.value)
            assertEquals(
                Triple(R.string.id_card_sign_pin_locked, CodeType.PIN2.name, null),
                viewModel.pinErrorState.value,
            )
        }

    @Test
    fun idCardViewModel_sign_handlePin2NotChangedException() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("PIN2 has not been changed")

            val mockPersonalData = mock(PersonalData::class.java)
            val keyUsage = mock(KeyUsage::class.java)
            val extendedKeyUsage = mock(ExtendedKeyUsage::class.java)
            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            val testData = byteArrayOf(1, 2, 3)
            val testName = "Test name"

            `when`(certificateService.parseCertificate(anyOrNull()))
                .thenReturn(x509CertificateHolder)
            `when`(certificateService.extractEIDType(any()))
                .thenReturn(EIDType.ID_CARD)
            `when`(certificateService.extractFriendlyName(anyOrNull())).thenReturn(testName)
            `when`(certificateService.isEllipticCurve(anyOrNull())).thenReturn(true)
            `when`(certificateService.extractKeyUsage(any())).thenReturn(keyUsage)
            `when`(certificateService.extractExtendedKeyUsage(any())).thenReturn(extendedKeyUsage)
            val certificate = ExtendedCertificate.create(testData, certificateService)
            val idCardData =
                IdCardData(
                    EIDType.ID_CARD,
                    mockPersonalData,
                    certificate,
                    certificate,
                    3,
                    0,
                    3,
                    false,
                )

            `when`(idCardService.data(anyOrNull())).thenReturn(idCardData)
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.dialogError.value)
            assertEquals(exception.message, viewModel.dialogError.value)
        }

    @Test
    fun idCardViewModel_sign_handleWrongPin2ExceptionWhenUnableToGetPinRetryCount() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                CodeVerificationException(CodeType.PIN2, 0),
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertFalse(viewModel.signStatus.value == true)
            assertNull(viewModel.signedContainer.value)
            assertEquals(
                Triple(R.string.id_card_sign_pin_locked, CodeType.PIN2.name, null),
                viewModel.pinErrorState.value,
            )
        }

    @Test
    fun idCardViewModel_sign_handleTooManyRequests() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("Too Many Requests")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.dialogError.value)
            assertEquals(exception.message, viewModel.dialogError.value)
        }

    @Test
    fun idCardViewModel_sign_handleOcspResponseNotInValidTimeSlot() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("OCSP response not in valid time slot")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.dialogError.value)
            assertEquals(exception.message, viewModel.dialogError.value)
        }

    @Test
    fun idCardViewModel_sign_handleCertificateStatusRevoked() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("Certificate status: revoked")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.errorState.value)
            assertEquals(
                Triple(R.string.signature_update_signature_error_message_certificate_revoked, null, null),
                viewModel.errorState.value,
            )
        }

    @Test
    fun idCardViewModel_sign_handleFailedToConnect() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("Failed to connect")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.errorState.value)
            assertEquals(Triple(R.string.no_internet_connection, null, null), viewModel.errorState.value)
        }

    @Test
    fun idCardViewModel_sign_handleFailedToCreateConnectionWithHost() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("Failed to create connection with host")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.errorState.value)
            assertEquals(Triple(R.string.no_internet_connection, null, null), viewModel.errorState.value)
        }

    @Test
    fun idCardViewModel_sign_handleFailedToCreateProxyConnectionWithHost() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("Failed to create proxy connection with host")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.errorState.value)
            assertEquals(Triple(R.string.main_settings_proxy_invalid_settings, null, null), viewModel.errorState.value)
        }

    @Test
    fun idCardViewModel_sign_handleGeneralError() =
        runTest {
            `when`(smartCardReaderManager.status()).thenReturn(Observable.just(SmartCardReaderStatus.CARD_DETECTED))

            val pin2 = byteArrayOf(1, 2, 3)
            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            val exception = Exception("General error")

            val mockPersonalData = mock(PersonalData::class.java)

            val mockSmartCardReader = mock(SmartCardReader::class.java)
            `when`(mockSmartCardReader.atr()).thenReturn(Hex.decode("3bdb960080b1fe451f830012233f536549440f9000f1"))
            `when`(smartCardReaderManager.connectedReader()).thenReturn(mockSmartCardReader)

            `when`(token.personalData()).thenReturn(mockPersonalData)

            `when`(mockComponentActivity.resources).thenReturn(resources)
            `when`(resources.configuration).thenReturn(context.resources.configuration)

            `when`(idCardService.data(anyOrNull())).thenThrow(Exception())
            `when`(idCardService.signContainer(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenThrow(
                exception,
            )

            viewModel.sign(mockComponentActivity, signedContainer, pin2, null)

            assertNotNull(viewModel.errorState.value)
            assertEquals(Triple(R.string.error_general_client, null, null), viewModel.errorState.value)
        }

    @Test
    fun idCardViewModel_removePendingSignature_success() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer = SignedContainer.openOrCreate(context, container, listOf(container), true)

            viewModel.removePendingSignature(signedContainer)

            assertEquals(1, signedContainer.getSignatures(Main).size)
        }

    @Test
    fun idCardViewModel_resetSignStatus_success() =
        runTest {
            val resetSignStatusObserver: Observer<Boolean?> = mock()
            viewModel.signStatus.observeForever(resetSignStatusObserver)

            viewModel.resetSignStatus()
            verify(resetSignStatusObserver, atLeastOnce()).onChanged(null)

            viewModel.signStatus.removeObserver(resetSignStatusObserver)
        }

    @Test
    fun idCardViewModel_resetDecryptStatus_success() =
        runTest {
            val resetDecryptStatusObserver: Observer<Boolean?> = mock()
            viewModel.decryptStatus.observeForever(resetDecryptStatusObserver)

            viewModel.resetDecryptStatus()
            verify(resetDecryptStatusObserver, atLeastOnce()).onChanged(null)

            viewModel.decryptStatus.removeObserver(resetDecryptStatusObserver)
        }

    @Test
    fun idCardViewModel_resetErrorState_success() =
        runTest {
            val errorStateObserver: Observer<Triple<Int, String?, Int?>?> = mock()
            viewModel.errorState.observeForever(errorStateObserver)

            viewModel.resetErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(null)

            viewModel.errorState.removeObserver(errorStateObserver)
        }

    @Test
    fun idCardViewModel_resetDialogErrorState_success() =
        runTest {
            val dialogErrorObserver: Observer<String?> = mock()
            viewModel.dialogError.observeForever(dialogErrorObserver)

            viewModel.resetDialogErrorState()
            verify(dialogErrorObserver, atLeastOnce()).onChanged(null)

            viewModel.dialogError.removeObserver(dialogErrorObserver)
        }

    @Test
    fun idCardViewModel_resetSignedContainer_success() =
        runTest {
            val signedContainerObserver: Observer<SignedContainer?> = mock()
            viewModel.signedContainer.observeForever(signedContainerObserver)

            viewModel.resetSignedContainer()
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)

            viewModel.signedContainer.removeObserver(signedContainerObserver)
        }

    @Test
    fun idCardViewModel_resetCryptoContainer_success() =
        runTest {
            val cryptoContainerObserver: Observer<CryptoContainer?> = mock()
            viewModel.cryptoContainer.observeForever(cryptoContainerObserver)

            viewModel.resetCryptoContainer()
            verify(cryptoContainerObserver, atLeastOnce()).onChanged(null)

            viewModel.cryptoContainer.removeObserver(cryptoContainerObserver)
        }

    @Test
    fun idCardViewModel_resetPersonalUserData_success() =
        runTest {
            val userDataObserver: Observer<IdCardData?> = mock()
            viewModel.userData.observeForever(userDataObserver)

            viewModel.resetPersonalUserData()
            verify(userDataObserver, atLeastOnce()).onChanged(null)

            viewModel.userData.removeObserver(userDataObserver)
        }

    @Test
    fun idCardViewModel_resetPINErrorState_success() =
        runTest {
            val pinErrorStateObserver: Observer<Triple<Int, String?, Int?>?> = mock()
            viewModel.pinErrorState.observeForever(pinErrorStateObserver)

            viewModel.resetPINErrorState()
            verify(pinErrorStateObserver, atLeastOnce()).onChanged(null)

            viewModel.pinErrorState.removeObserver(pinErrorStateObserver)
        }

    @Test
    fun idCardViewModel_resetShouldHandleError_success() =
        runTest {
            val emittedValues = mutableListOf<Boolean>()
            val job =
                launch {
                    viewModel.shouldHandleError
                        .take(1)
                        .toList(emittedValues)
                }

            yield()
            viewModel.resetShouldHandleError()

            job.join()

            assertEquals(listOf(false), emittedValues)
        }

    @Test
    fun idCardViewModel_setShouldHandleError_success() =
        runTest {
            val emittedValues = mutableListOf<Boolean>()
            val job =
                launch {
                    viewModel.shouldHandleError
                        .take(2)
                        .toList(emittedValues)
                }

            yield()
            viewModel.setShouldHandleError(true)

            job.join()

            assertEquals(listOf(false, true), emittedValues)
        }
}
