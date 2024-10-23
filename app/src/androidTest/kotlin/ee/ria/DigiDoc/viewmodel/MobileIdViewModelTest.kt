@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.MobileIdServiceResponse
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.network.mid.dto.MobileCertificateResultType
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq

@RunWith(MockitoJUnitRunner::class)
class MobileIdViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var mobileIdService: MobileSignService

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    lateinit var configurationRepository: ConfigurationRepository

    @Mock
    lateinit var errorStateObserver: Observer<String?>

    @Mock
    lateinit var signedContainerObserver: Observer<SignedContainer?>

    @Mock
    lateinit var statusObserver: Observer<MobileCreateSignatureProcessStatus?>

    @Mock
    lateinit var roleDataRequestedObserver: Observer<Boolean?>

    @Mock
    lateinit var challengeObserver: Observer<String?>

    private lateinit var scenario: ActivityScenario<ComponentActivity>
    private lateinit var activity: ComponentActivity

    private lateinit var signedContainer: SignedContainer

    private val configurationProvider =
        ConfigurationProvider(
            metaInf =
                ConfigurationProvider.MetaInf(
                    url = "https://www.example.com",
                    date = "2021-09-01",
                    serial = 1,
                    version = 1,
                ),
            sivaUrl = "https://www.example.com",
            tslUrl = "https://www.example.com",
            tslCerts = listOf("CERT0000111122224444"),
            tsaUrl = "https://www.example.com",
            ocspUrls = mapOf("issuer" to "https://www.example.com"),
            ldapPersonUrl = "https://www.example.com",
            ldapCorpUrl = "https://www.example.com",
            midRestUrl = "https://www.example.com",
            midSkRestUrl = "https://www.example.com",
            sidV2RestUrl = "https://www.example.com",
            sidV2SkRestUrl = "https://www.example.com",
            certBundle = listOf("CERT0000111122224444"),
            configurationLastUpdateCheckDate = null,
            configurationUpdateDate = null,
        )

    companion object {
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    val context = InstrumentationRegistry.getInstrumentation().targetContext
                    configurationLoader =
                        ConfigurationLoaderImpl(
                            Gson(),
                            CentralConfigurationRepositoryImpl(
                                CentralConfigurationServiceImpl("Tests", ConfigurationProperty()),
                            ),
                            ConfigurationProperty(),
                            ConfigurationPropertiesImpl(),
                            ConfigurationSignatureVerifierImpl(),
                        )
                    configurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    private lateinit var context: Context

    private lateinit var dataStore: DataStore

    private lateinit var viewModel: MobileIdViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        viewModel =
            MobileIdViewModel(
                dataStore,
                mobileIdService,
                configurationRepository,
            )
        viewModel.errorState.observeForever(errorStateObserver)
        viewModel.status.observeForever(statusObserver)
        viewModel.roleDataRequested.observeForever(roleDataRequestedObserver)
        viewModel.signedContainer.observeForever(signedContainerObserver)
        viewModel.challenge.observeForever(challengeObserver)

        scenario = ActivityScenario.launch(ComponentActivity::class.java)

        scenario.onActivity { activityInstance ->
            activity = activityInstance
        }

        val container =
            AssetFile.getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )

        signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, container, listOf(container), true)
            }
    }

    @After
    fun cleanup() {
        scenario.close()
    }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_errorState() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(mobileIdService.response).thenReturn(MutableLiveData<MobileIdServiceResponse?>(null))
            `when`(mobileIdService.status).thenReturn(MutableLiveData<MobileCreateSignatureProcessStatus?>(null))
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>(null))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(mobileIdService.result).thenReturn(MutableLiveData<MobileCertificateResultType?>(null))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>("Some error occurred"))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged("Some error occurred")
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(null)
            verify(challengeObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_responseStatusOK() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any(), anyBoolean()),
            ).thenReturn(signedContainer)

            `when`(mobileIdService.response).thenReturn(
                MutableLiveData<MobileIdServiceResponse?>(
                    MobileIdServiceResponse(
                        MobileCreateSignatureProcessStatus.OK,
                        null,
                        "signature",
                    ),
                ),
            )
            `when`(
                mobileIdService.status,
            ).thenReturn(MutableLiveData<MobileCreateSignatureProcessStatus?>(MobileCreateSignatureProcessStatus.OK))
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(false))
            `when`(mobileIdService.result).thenReturn(MutableLiveData<MobileCertificateResultType?>(null))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
            verify(signedContainerObserver, atLeastOnce()).onChanged(any<SignedContainer>())
            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.OK)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
        }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_responseStatusElse() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(null)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any(), anyBoolean()),
            ).thenReturn(signedContainer)
            `when`(mobileIdService.response).thenReturn(
                MutableLiveData<MobileIdServiceResponse?>(
                    MobileIdServiceResponse(
                        MobileCreateSignatureProcessStatus.NO_RESPONSE,
                        null,
                        "signature",
                    ),
                ),
            )
            `when`(mobileIdService.status).thenReturn(MutableLiveData<MobileCreateSignatureProcessStatus?>(null))
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(false))
            `when`(mobileIdService.result).thenReturn(MutableLiveData<MobileCertificateResultType?>(null))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged(context.getString(R.string.no_internet_connection))
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.NO_RESPONSE)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
        }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_responseStatusNOT_MID_CLIENT() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any(), anyBoolean()),
            ).thenReturn(signedContainer)

            `when`(mobileIdService.response).thenReturn(
                MutableLiveData<MobileIdServiceResponse?>(
                    MobileIdServiceResponse(
                        MobileCreateSignatureProcessStatus.NOT_MID_CLIENT,
                        null,
                        "signature",
                    ),
                ),
            )
            `when`(
                mobileIdService.status,
            ).thenReturn(
                MutableLiveData<MobileCreateSignatureProcessStatus?>(MobileCreateSignatureProcessStatus.NOT_MID_CLIENT),
            )
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(mobileIdService.result).thenReturn(MutableLiveData<MobileCertificateResultType?>(null))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(context.getString(R.string.signature_update_mobile_id_status_expired_transaction))
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.NOT_MID_CLIENT)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
        }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_responseStatusUserCancelled() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any(), anyBoolean()),
            ).thenReturn(signedContainer)

            `when`(mobileIdService.response).thenReturn(
                MutableLiveData<MobileIdServiceResponse?>(
                    MobileIdServiceResponse(
                        MobileCreateSignatureProcessStatus.USER_CANCELLED,
                        null,
                        "signature",
                    ),
                ),
            )
            `when`(
                mobileIdService.status,
            ).thenReturn(
                MutableLiveData<MobileCreateSignatureProcessStatus?>(MobileCreateSignatureProcessStatus.USER_CANCELLED),
            )
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(mobileIdService.result).thenReturn(MutableLiveData<MobileCertificateResultType?>(null))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(null)
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(MobileCreateSignatureProcessStatus.USER_CANCELLED)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
        }

    @Test
    fun mobileIdViewModel_performMobileIdWorkRequest_resultNotFound() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any(), anyBoolean()),
            ).thenReturn(signedContainer)

            `when`(mobileIdService.response).thenReturn(MutableLiveData<MobileIdServiceResponse?>(null))
            `when`(
                mobileIdService.status,
            ).thenReturn(
                MutableLiveData<MobileCreateSignatureProcessStatus?>(MobileCreateSignatureProcessStatus.USER_CANCELLED),
            )
            `when`(mobileIdService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(mobileIdService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(
                mobileIdService.result,
            ).thenReturn(MutableLiveData<MobileCertificateResultType?>(MobileCertificateResultType.NOT_FOUND))
            `when`(mobileIdService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performMobileIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                "5629421",
                null,
            )

            verify(mobileIdService, atLeastOnce()).resetValues()
            verify(
                mobileIdService,
                atLeastOnce(),
            ).processMobileIdRequest(any(), any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any())
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(context.getString(R.string.signature_update_mobile_id_error_not_mobile_id_user))
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(null)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
        }

    @Test
    fun mobileIdViewModel_resetErrorState_success() =
        runTest {
            viewModel.resetErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun mobileIdViewModel_resetStatus_success() =
        runTest {
            viewModel.resetStatus()
            verify(statusObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun mobileIdViewModel_resetSignedContainer_success() =
        runTest {
            viewModel.resetSignedContainer()
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun mobileIdViewModel_resetRoleDataRequested_success() =
        runTest {
            viewModel.resetRoleDataRequested()
            verify(roleDataRequestedObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun mobileIdViewModel_setRoleDataRequested_success() =
        runTest {
            viewModel.setRoleDataRequested(true)
            verify(roleDataRequestedObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun mobileIdViewModel_cancelmobileIdWorkRequest_success() =
        runTest {
            viewModel.cancelMobileIdWorkRequest(signedContainer)
            verify(mobileIdService, atLeastOnce()).setCancelled(signedContainer, true)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_nullReturnTrue() =
        runTest {
            val result = viewModel.isPhoneNumberValid(null)
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_emptyReturnTrue() =
        runTest {
            val result = viewModel.isPhoneNumberValid("")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_lengthIsShorterReturnFalse() =
        runTest {
            val result = viewModel.isPhoneNumberValid("370")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_countryCodeIsIncorrectReturnFalse() =
        runTest {
            val result = viewModel.isPhoneNumberValid("37151")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_countryCodeIsCorrectReturnFalse() =
        runTest {
            val result = viewModel.isPhoneNumberValid("372510998")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_phoneNumberIs10DigitsAndCountryCodeIsInvalidReturnFalse() =
        runTest {
            val result = viewModel.isPhoneNumberValid("3715109980")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPhoneNumberValid_phoneNumberIs10DigitsAndCountryCodeIsValidReturnTrue() =
        runTest {
            val result = viewModel.isPhoneNumberValid("3725109980")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isCountryCodeMissing_emptyReturnFalse() =
        runTest {
            val result = viewModel.isCountryCodeMissing("")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isCountryCodeMissing_lengthIsShorterReturnFalse() =
        runTest {
            val result = viewModel.isCountryCodeMissing("370")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isCountryCodeMissing_countryCodeIsIncorrectReturnTrue() =
        runTest {
            val result = viewModel.isCountryCodeMissing("37151")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isCountryCodeMissing_countryCodeIsCorrectReturnFalse() =
        runTest {
            val result = viewModel.isCountryCodeMissing("372510998")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isCountryCodeMissing_phoneNumberIs10DigitsReturnFalse() =
        runTest {
            val result = viewModel.isCountryCodeMissing("3715109980")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPersonalCodeValid_true() =
        runTest {
            val result = viewModel.isPersonalCodeValid("38207253718")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isPersonalCodeValid_hashIsInvalidReturnFalse() =
        runTest {
            val result = viewModel.isPersonalCodeValid("38308263913")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPersonalCodeValid_lengthIsLessThan11DigitsReturnFalse() =
        runTest {
            val result = viewModel.isPersonalCodeValid("3830826391")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_isPersonalCodeValid_isNullReturnTrue() =
        runTest {
            val result = viewModel.isPersonalCodeValid(null)
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_isPersonalCodeValid_isEmptyReturnTrue() =
        runTest {
            val result = viewModel.isPersonalCodeValid("")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_true() =
        runTest {
            val result = viewModel.positiveButtonEnabled("3725629421", "38207253718")
            assertTrue(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_personalCodeHashInvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("3725629421", "38308263913")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_phoneNumberInvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("372562942", "3830826391")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_false() =
        runTest {
            val result = viewModel.positiveButtonEnabled("3725629421", "3830826391")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_countryCodeInvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("3735629421", "38308263913")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_personalCodeNullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled("3725629421", null)
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_phoneNumberNullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(null, "38308263913")
            assertFalse(result)
        }

    @Test
    fun mobileIdViewModel_positiveButtonEnabled_bothNullsReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(null, null)
            assertFalse(result)
        }
}
