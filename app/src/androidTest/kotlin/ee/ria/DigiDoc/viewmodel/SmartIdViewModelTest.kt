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
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SmartIDServiceResponse
import ee.ria.DigiDoc.smartId.SmartSignService
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
class SmartIdViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var contentResolver: ContentResolver

    @Mock
    lateinit var smartSignService: SmartSignService

    @Mock
    lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    lateinit var configurationRepository: ConfigurationRepository

    @Mock
    lateinit var errorStateObserver: Observer<String?>

    @Mock
    lateinit var dialogErrorObserver: Observer<Int>

    @Mock
    lateinit var signedContainerObserver: Observer<SignedContainer?>

    @Mock
    lateinit var statusObserver: Observer<SessionStatusResponseProcessStatus?>

    @Mock
    lateinit var roleDataRequestedObserver: Observer<Boolean?>

    @Mock
    lateinit var challengeObserver: Observer<String?>

    @Mock
    lateinit var selectDeviceObserver: Observer<Boolean?>

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

    private lateinit var viewModel: SmartIdViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        dataStore = DataStore(context)
        viewModel =
            SmartIdViewModel(
                dataStore,
                smartSignService,
                configurationRepository,
            )
        viewModel.errorState.observeForever(errorStateObserver)
        viewModel.dialogError.observeForever(dialogErrorObserver)
        viewModel.status.observeForever(statusObserver)
        viewModel.signedContainer.observeForever(signedContainerObserver)
        viewModel.roleDataRequested.observeForever(roleDataRequestedObserver)
        viewModel.challenge.observeForever(challengeObserver)
        viewModel.selectDevice.observeForever(selectDeviceObserver)

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
    fun smartIdViewModel_performSmartIdWorkRequest_errorState() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(smartSignService.status).thenReturn(MutableLiveData<SessionStatusResponseProcessStatus?>(null))
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>(null))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(null))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>("Some error occurred"))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(errorStateObserver, atLeastOnce()).onChanged("Some error occurred")
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(null)
            verify(challengeObserver, atLeastOnce()).onChanged(null)
            verify(selectDeviceObserver, atLeastOnce()).onChanged(false)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusOK() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(
                MutableLiveData<SmartIDServiceResponse?>(
                    SmartIDServiceResponse(SessionStatusResponseProcessStatus.OK),
                ),
            )
            `when`(smartSignService.status).thenReturn(
                MutableLiveData<SessionStatusResponseProcessStatus?>(
                    SessionStatusResponseProcessStatus.OK,
                ),
            )
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(false))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(false))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
            verify(signedContainerObserver, atLeastOnce()).onChanged(any<SignedContainer>())
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.OK)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(false)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusElse() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(null)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(
                MutableLiveData<SmartIDServiceResponse?>(
                    SmartIDServiceResponse(SessionStatusResponseProcessStatus.NO_RESPONSE),
                ),
            )
            `when`(smartSignService.status).thenReturn(MutableLiveData<SessionStatusResponseProcessStatus?>(null))
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(false))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(errorStateObserver, atLeastOnce()).onChanged(context.getString(R.string.no_internet_connection))
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.NO_RESPONSE)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusOCSP_INVALID_TIME_SLOT() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(
                smartSignService.status,
            ).thenReturn(
                MutableLiveData<SessionStatusResponseProcessStatus?>(
                    SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT,
                ),
            )
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(
                dialogErrorObserver,
                atLeastOnce(),
            ).onChanged(R.string.invalid_time_slot_message)
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.OCSP_INVALID_TIME_SLOT)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusTOO_MANY_REQUESTS() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(
                smartSignService.status,
            ).thenReturn(
                MutableLiveData<SessionStatusResponseProcessStatus?>(
                    SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS,
                ),
            )
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(
                dialogErrorObserver,
                atLeastOnce(),
            ).onChanged(R.string.too_many_requests_message)
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.TOO_MANY_REQUESTS)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusUserRefused() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(
                smartSignService.status,
            ).thenReturn(
                MutableLiveData<SessionStatusResponseProcessStatus?>(SessionStatusResponseProcessStatus.USER_REFUSED),
            )
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(context.getString(R.string.signature_update_mobile_id_status_user_cancel))
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.USER_REFUSED)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusUserCancelled() =
        runTest {
            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(
                    eq(context),
                    eq(contentResolver),
                    any(),
                    anyBoolean(),
                    anyBoolean(),
                ),
            ).thenReturn(signedContainer)

            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(
                smartSignService.status,
            ).thenReturn(
                MutableLiveData<SessionStatusResponseProcessStatus?>(SessionStatusResponseProcessStatus.USER_CANCELLED),
            )
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>("0660"))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>(null))

            viewModel.performSmartIdWorkRequest(
                activity,
                context,
                "test message",
                signedContainer,
                "45611283812",
                0,
                null,
            )

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(
                eq(
                    context,
                ),
                any<SignedContainer>(), any(), eq(null), any(), any(), any(), any(), any(),
            )
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(null)
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.USER_CANCELLED)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
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
    fun smartIdViewModel_resetDialogErrorState_success() =
        runTest {
            viewModel.resetDialogErrorState()
            verify(dialogErrorObserver, atLeastOnce()).onChanged(0)
        }

    @Test
    fun smartIdViewModel_resetErrorState_success() =
        runTest {
            viewModel.resetErrorState()
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun smartIdViewModel_resetStatus_success() =
        runTest {
            viewModel.resetStatus()
            verify(statusObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun smartIdViewModel_resetSignedContainer_success() =
        runTest {
            viewModel.resetSignedContainer()
            verify(signedContainerObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun smartIdViewModel_cancelSmartIdWorkRequest_success() =
        runTest {
            viewModel.cancelSmartIdWorkRequest(signedContainer)
            verify(smartSignService, atLeastOnce()).setCancelled(signedContainer, true)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_true() =
        runTest {
            val result = viewModel.positiveButtonEnabled(0, "38207253718")
            assertTrue(result)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_personalCodeHashInvalidReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(0, "38308263913")
            assertFalse(result)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_countryNotEEReturnTrue() =
        runTest {
            val result = viewModel.positiveButtonEnabled(1, "3830826391")
            assertTrue(result)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_false() =
        runTest {
            val result = viewModel.positiveButtonEnabled(0, "3830826391")
            assertFalse(result)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_personalCodeNullReturnFalse() =
        runTest {
            val result = viewModel.positiveButtonEnabled(0, null)
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
}
