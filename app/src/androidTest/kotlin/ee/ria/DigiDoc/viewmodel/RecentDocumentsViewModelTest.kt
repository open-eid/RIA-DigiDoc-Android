@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class RecentDocumentsViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var sivaRepository: SivaRepository

    private lateinit var container: File
    private lateinit var signedContainer: SignedContainer

    private lateinit var sharedContainerViewModel: SharedContainerViewModel

    companion object {
        private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        private lateinit var configurationLoader: ConfigurationLoader
        private lateinit var configurationRepository: ConfigurationRepository

        @JvmStatic
        @BeforeClass
        fun setupOnce() {
            runBlocking {
                try {
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

    private lateinit var viewModel: RecentDocumentsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = RecentDocumentsViewModel(context, sivaRepository)

        container =
            AssetFile.getResourceFileAsFile(
                context,
                "example.asice",
                ee.ria.DigiDoc.common.R.raw.example,
            )

        signedContainer =
            runBlocking {
                SignedContainer.openOrCreate(context, container, listOf(container), true)
            }

        sharedContainerViewModel = SharedContainerViewModel(context, mock(ContentResolver::class.java))
    }

    @Test
    fun signingViewModel_openDocument_success() =
        runBlocking {
            val container =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )

            `when`(sivaRepository.isTimestampedContainer(anyOrNull(), anyBoolean())).thenReturn(false)

            val result = viewModel.openDocument(container, true)

            assertEquals(result.getDataFiles().size, 1)
        }

    @Test
    fun signingViewModel_openDocument_successWithTimestampedContainer(): Unit =
        runBlocking {
            val tempFile = File.createTempFile("testFile", ".asics", ContainerUtil.signatureContainersDir(context))
            tempFile.deleteOnExit()

            `when`(sivaRepository.isTimestampedContainer(anyOrNull(), anyBoolean())).thenReturn(true)

            viewModel.openDocument(tempFile, true)

            verify(sivaRepository).getTimestampedContainer(anyOrNull(), anyOrNull())
        }

    @Test
    fun signingViewModel_getRecentDocumentList_success() {
        ContainerUtil.signatureContainersDir(context).delete()
        val tempFile = File.createTempFile("testFile", ".asice", ContainerUtil.signatureContainersDir(context))
        tempFile.deleteOnExit()
        val list = viewModel.getRecentDocumentList()

        assertTrue(list.isNotEmpty())
    }

    @Test
    fun signingViewModel_handleSendToSigningViewWithSiva_sendToSigningViewWithSivaFalseWithOtherMimetype() =
        runTest {
            val mimeType = "some/other-mime"
            val mockFile = mock(File::class.java)

            viewModel.handleDocument(mockFile, mimeType, true, sharedContainerViewModel)

            val sendToSiva = viewModel.sendToSigningViewWithSiva.value
            if (sendToSiva != null) {
                assertFalse(sendToSiva)
            } else {
                fail("sendToSigningViewWithSiva cannot be null")
            }
        }

    @Test
    fun signingViewModel_handleSendToSigningViewWithSiva_sendToSigningViewWithSivaTrue() =
        runTest {
            viewModel.handleSendToSigningViewWithSiva(true)

            val sendToSiva = viewModel.sendToSigningViewWithSiva.value

            if (sendToSiva != null) {
                assertTrue(sendToSiva)
            } else {
                fail("sendToSigningViewWithSiva cannot be null")
            }
        }

    @Test
    fun signingViewModel_handleSendToSigningViewWithSiva_sendToSigningViewWithSivaFalse() =
        runTest {
            viewModel.handleSendToSigningViewWithSiva(false)

            val sendToSiva = viewModel.sendToSigningViewWithSiva.value

            if (sendToSiva != null) {
                assertFalse(sendToSiva)
            } else {
                fail("sendToSigningViewWithSiva cannot be null")
            }
        }
}
