@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.test.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.sid.dto.response.SessionStatusResponseProcessStatus
import ee.ria.DigiDoc.network.sid.dto.response.SmartIDServiceResponse
import ee.ria.DigiDoc.smartId.SmartSignService
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
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
    lateinit var signedContainterObserver: Observer<SignedContainer?>

    @Mock
    lateinit var statusObserver: Observer<SessionStatusResponseProcessStatus?>

    @Mock
    lateinit var challengeObserver: Observer<String?>

    @Mock
    lateinit var selectDeviceObserver: Observer<Boolean?>

    private val configurationProvider =
        ConfigurationProvider(
            metaInf =
                ConfigurationProvider.MetaInf(
                    url = "https://www.example.com",
                    date = "2021-09-01",
                    serial = 1,
                    version = 1,
                ),
            configUrl = "https://www.example.com",
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
        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
                    Initialization.init(InstrumentationRegistry.getInstrumentation().targetContext)
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
                context,
                dataStore,
                smartSignService,
                configurationRepository,
            )
        viewModel.errorState.observeForever(errorStateObserver)
        viewModel.status.observeForever(statusObserver)
        viewModel.signedContainer.observeForever(signedContainterObserver)
        viewModel.challenge.observeForever(challengeObserver)
        viewModel.selectDevice.observeForever(selectDeviceObserver)
    }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_errorState() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container))
                }

            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(smartSignService.response).thenReturn(MutableLiveData<SmartIDServiceResponse?>(null))
            `when`(smartSignService.status).thenReturn(MutableLiveData<SessionStatusResponseProcessStatus?>(null))
            `when`(smartSignService.challenge).thenReturn(MutableLiveData<String?>(null))
            `when`(smartSignService.cancelled).thenReturn(MutableLiveData<Boolean?>(true))
            `when`(smartSignService.selectDevice).thenReturn(MutableLiveData<Boolean?>(null))
            `when`(smartSignService.errorState).thenReturn(MutableLiveData<String?>("Some error occurred"))

            viewModel.performSmartIdWorkRequest(signedContainer, "45611283812", 0, null)

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(eq(context), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged("Some error occurred")
            verify(signedContainterObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(null)
            verify(challengeObserver, atLeastOnce()).onChanged(null)
            verify(selectDeviceObserver, atLeastOnce()).onChanged(false)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusOK() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container))
                }

            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any()),
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

            viewModel.performSmartIdWorkRequest(signedContainer, "45611283812", 0, null)

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(eq(context), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged(null)
            verify(signedContainterObserver, atLeastOnce()).onChanged(any<SignedContainer>())
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.OK)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(false)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusElse() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container))
                }

            `when`(configurationRepository.getConfiguration()).thenReturn(null)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any()),
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

            viewModel.performSmartIdWorkRequest(signedContainer, "45611283812", 0, null)

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(eq(context), any(), eq(null), any(), any(), any(), any(), any())
            verify(errorStateObserver, atLeastOnce()).onChanged(context.getString(R.string.no_internet_connection))
            verify(signedContainterObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.NO_RESPONSE)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
        }

    @Test
    fun smartIdViewModel_performSmartIdWorkRequest_responseStatusUserCancelled() =
        runTest {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            val signedContainer =
                runBlocking {
                    SignedContainer.openOrCreate(context, container, listOf(container))
                }

            `when`(configurationRepository.getConfiguration()).thenReturn(configurationProvider)
            `when`(
                fileOpeningRepository.openOrCreateContainer(eq(context), eq(contentResolver), any()),
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

            viewModel.performSmartIdWorkRequest(signedContainer, "45611283812", 0, null)

            verify(smartSignService, atLeastOnce()).resetValues()
            verify(
                smartSignService,
                atLeastOnce(),
            ).processSmartIdRequest(eq(context), any(), eq(null), any(), any(), any(), any(), any())
            verify(
                errorStateObserver,
                atLeastOnce(),
            ).onChanged(context.getString(R.string.signature_update_mobile_id_status_user_cancel))
            verify(signedContainterObserver, atLeastOnce()).onChanged(null)
            verify(statusObserver, atLeastOnce()).onChanged(SessionStatusResponseProcessStatus.USER_REFUSED)
            verify(challengeObserver, atLeastOnce()).onChanged("0660")
            verify(selectDeviceObserver, atLeastOnce()).onChanged(true)
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
            verify(signedContainterObserver, atLeastOnce()).onChanged(null)
        }

    @Test
    fun smartIdViewModel_cancelSmartIdWorkRequest_success() =
        runTest {
            viewModel.cancelSmartIdWorkRequest()
            verify(smartSignService, atLeastOnce()).setCancelled(true)
        }

    @Test
    fun smartIdViewModel_positiveButtonEnabled_true() =
        runTest {
            val result = viewModel.positiveButtonEnabled(0, "38308263913")
            assertTrue(result)
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
}
