@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile.Companion.getResourceFileAsFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.libdigidoclib.SignedContainer.Companion.openOrCreate
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerDataFilesEmptyException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import ee.ria.DigiDoc.utilsLib.extensions.mimeType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.NoSuchFileException

@RunWith(MockitoJUnitRunner::class)
class SignedContainerTest {
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

    private lateinit var testFile: File
    private lateinit var dataFile1: File
    private lateinit var dataFile2: File
    private lateinit var dataFile3: File
    private lateinit var container: File
    private lateinit var signedPdfDocument: File

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testFile = File.createTempFile("testFile", ".txt", context.filesDir)
        dataFile1 = File.createTempFile("dataFile1", ".txt", context.filesDir)
        dataFile2 = File.createTempFile("dataFile2", ".txt", context.filesDir)
        dataFile3 = File.createTempFile("dataFile3", ".txt", context.filesDir)
        container = AssetFile.getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)
        signedPdfDocument =
            AssetFile.getResourceFileAsFile(
                context, "example_signed_pdf.pdf",
                ee.ria.DigiDoc.common.R.raw.example_signed_pdf,
            )
    }

    @After
    fun tearDown() {
        testFile.delete()
        dataFile1.delete()
        dataFile2.delete()
        dataFile3.delete()
    }

    @Test
    fun signedContainer_rawContainer_success() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result = signedContainer.rawContainer()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_rawContainerFile_success() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result = signedContainer.getContainerFile()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_isExistingContainer_false() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result = signedContainer.isExistingContainer()

            assertFalse(result)
        }

    @Test
    fun signedContainer_isExistingContainer_true() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            val result = signedContainer.isExistingContainer()

            assertTrue(result)
        }

    @Test
    fun signedContainer_containerMimetype_returnsValidMimetype() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result = signedContainer.containerMimetype()

            assertEquals(ASICE_MIMETYPE, result)
        }

    @Test
    fun signedContainer_setName_successWhenSettingName() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            signedContainer.setName("testName")

            assertEquals("testName.asice", signedContainer.getName())
        }

    @Test
    fun signedContainer_mimeType_returnContainerMimeType() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result =
                signedContainer.getContainerFile()?.let {
                    signedContainer.getContainerFile()?.mimeType(context)
                }

            assertEquals(ASICE_MIMETYPE, result)
        }

    @Test
    fun signedContainer_getDataFile_successWhenGettingDataFile() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            val result =
                signedContainer.getDataFile(
                    signedContainer.getDataFiles().first(),
                    signedContainer.getContainerFile()?.let {
                        ContainerUtil.getContainerDataFilesDir(
                            context,
                            it,
                        )
                    },
                )

            assertEquals(testFile.name, result?.name)
        }

    @Test
    fun signedContainer_removeSignature_successWhenRemovingSignature() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals(2, signedContainer.getSignatures().size)

            signedContainer.removeSignature(signedContainer.getSignatures().first())

            assertEquals(1, signedContainer.getSignatures().size)
        }

    @Test
    fun signedContainer_removeDataFile_successWhenRemovingDataFile() =
        runTest {
            val dataFiles = listOf(testFile, dataFile1, dataFile2)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            assertEquals(3, signedContainer.getDataFiles().size)

            signedContainer.removeDataFile(signedContainer.getDataFiles().first())

            assertEquals(2, signedContainer.getDataFiles().size)
        }

    @Test(expected = ContainerDataFilesEmptyException::class)
    fun signedContainer_removeDataFile_throwsException() =
        runTest {
            val dataFiles = listOf(testFile)
            val signedContainer = openOrCreate(context, testFile, dataFiles, true)
            signedContainer.removeDataFile(signedContainer.getDataFiles().first())
        }

    @Test
    fun signedContainer_addDataFiles_successWhenAddingDataFiles() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            SignedContainer.addDataFiles(context, signedContainer, listOf(dataFile1, dataFile2))

            assertEquals(3, signedContainer.getDataFiles().size)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithSingleDataFile() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals(dataFiles[0].name, signedContainer.getDataFiles()[0].fileName)
            assertEquals(emptyList<SignatureInterface>(), signedContainer.getSignatures())
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithMultipleDataFiles() =
        runTest {
            val dataFiles =
                listOf(
                    dataFile1,
                    dataFile2,
                    dataFile3,
                )

            val signedContainer = openOrCreate(context, testFile, dataFiles, true)

            dataFiles.forEach { file ->
                file.delete()
            }

            assertEquals(3, signedContainer.getDataFiles().size)
            dataFiles.zip(signedContainer.getDataFiles()).forEach { (file1, file2) ->
                assertEquals(file1.name, file2.fileName)
            }
            assertEquals(emptyList<SignatureInterface>(), signedContainer.getSignatures())
        }

    @Test
    fun signedContainer_openOrCreate_successWhenOpeningExistingContainer() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals(true, signedContainer.isExistingContainer())
            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals(2, signedContainer.getSignatures().size)
        }

    @Test(expected = NoSuchElementException::class)
    fun signedContainer_openOrCreate_throwExceptionWhenCreatingContainerWithEmptyDataFiles() =
        runTest {
            openOrCreate(context, testFile, emptyList(), true)
        }

    @Test(expected = Exception::class)
    fun signedContainer_openOrCreate_throwExceptionWhenCreatingContainerWithInvalidDataFile() =
        runTest {
            openOrCreate(context, testFile, listOf(File("testDataFile")), true)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerAndFilteringDuplicateDataFiles() =
        runTest {
            val createdContainer = openOrCreate(context, testFile, listOf(dataFile1, dataFile1), true)

            assertEquals(1, createdContainer.getDataFiles().size)
        }

    @Test(expected = NoSuchFileException::class)
    fun signedContainer_openOrCreate_throwExceptionWhenOpeningNonExistentContainer() =
        runTest {
            openOrCreate(context, File("nonExistentFile"), listOf(File("nonExistentDataFile")), true)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithCustomFileName() =
        runTest {
            val customFileName = File.createTempFile("customTestFile", ".qwerty", context.filesDir)

            val signedContainer = openOrCreate(context, customFileName, listOf(dataFile1), true)

            customFileName.delete()

            assertEquals(
                "${customFileName.nameWithoutExtension}.$DEFAULT_CONTAINER_EXTENSION",
                signedContainer.getName(),
            )
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithSpecialCharacters() =
        runTest {
            val testDataFile = File.createTempFile("dataFile1!@#$€%&=?", ".txt", context.filesDir)

            val createdContainer = openOrCreate(context, testFile, listOf(testDataFile), true)

            testDataFile.delete()

            assertNotNull(createdContainer)
            assertEquals(testDataFile.name, createdContainer.getDataFiles().first().fileName)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithNoExtensionDataFile() =
        runTest {
            val testDataFile = File.createTempFile("dataFile1", "", context.filesDir)

            val signedContainer = openOrCreate(context, testDataFile, listOf(testDataFile), true)

            testDataFile.delete()

            assertEquals("${testDataFile.name}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
        }

    @Test
    fun signedContainer_container_successWhenGettingNewlyCreatedInitializedContainer() =
        runTest {
            val signedContainer = openOrCreate(context, testFile, listOf(dataFile1), true)

            assertEquals(false, signedContainer.isExistingContainer())
            assertEquals(
                "${testFile.nameWithoutExtension}.$DEFAULT_CONTAINER_EXTENSION",
                signedContainer.getName(),
            )
            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals(0, signedContainer.getSignatures().size)
        }

    @Test
    fun signedContainer_container_successWhenGettingExistingInitializedContainer() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals(true, signedContainer.isExistingContainer())
            assertEquals("example.asice", signedContainer.getName())
            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals(2, signedContainer.getSignatures().size)
        }

    @Test
    fun signedContainer_container_matchesContainerSignatureData() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals(
                "O’CONNEŽ-ŠUSLIK TESTNUMBER,MARY ÄNN,60001019906",
                signedContainer.getSignatures().first().signedBy,
            )
            assertEquals("2022-03-21T12:03:12Z", signedContainer.getSignatures().first().claimedSigningTime)
            assertEquals("2022-03-21T12:03:22Z", signedContainer.getSignatures().first().trustedSigningTime)
            assertEquals(
                "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256",
                signedContainer.getSignatures().first().signatureMethod,
            )
            assertNotNull(signedContainer.getSignatures().first().dataToSign)
            assertEquals("", signedContainer.getSignatures().first().policy)
            assertEquals("", signedContainer.getSignatures().first().spUri)
            assertEquals("BES/time-stamp", signedContainer.getSignatures().first().profile)
            assertEquals("Linn", signedContainer.getSignatures().first().city)
            assertEquals("Maakond", signedContainer.getSignatures().first().stateOrProvince)
            assertEquals("12345", signedContainer.getSignatures().first().postalCode)
            assertEquals("EE", signedContainer.getSignatures().first().countryName)
            assertEquals(1, signedContainer.getSignatures().first().signerRoles.size)
            assertEquals("Roll", signedContainer.getSignatures().first().signerRoles[0])
            assertEquals("2022-03-21T12:03:23Z", signedContainer.getSignatures().first().ocspProducedAt)
            assertEquals("2022-03-21T12:03:22Z", signedContainer.getSignatures().first().timeStampTime)
            assertEquals("", signedContainer.getSignatures().first().archiveTimeStampTime)
            assertEquals("", signedContainer.getSignatures().first().streetAddress)
            assertNotNull(signedContainer.getSignatures().first().messageImprint)
            assertNotNull(signedContainer.getSignatures().first().signingCertificateDer)
            assertNotNull(signedContainer.getSignatures().first().ocspCertificateDer)
            assertNotNull(signedContainer.getSignatures().first().timeStampCertificateDer)
            assertNotNull(signedContainer.getSignatures().first().archiveTimeStampCertificateDer)
        }

    @Test
    fun signedContainer_container_matchesContainerDataFileData() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals("text.txt", signedContainer.getDataFiles().first().fileName)
            assertEquals(3, signedContainer.getDataFiles().first().fileSize)
            assertEquals("application/octet-stream", signedContainer.getDataFiles().first().mediaType)
        }

    @Test
    fun signedContainer_container_matchesContainerValidatorData() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            assertEquals(ValidatorInterface.Status.Unknown, signedContainer.getSignatures().first().validator.status)
            assertNotSame("", signedContainer.getSignatures().first().validator.diagnostics)
        }

    @Test
    fun signedContainer_openOrCreate_successCreatingNewContainerWithPDFNoSignatures() =
        runTest {
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(100, 100, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            pdfDocument.finishPage(page)

            val testPDFFile = File(context.filesDir, "testFile.pdf")
            try {
                val fileOutputStream = FileOutputStream(testPDFFile)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                pdfDocument.close()
            }

            val signedContainer = openOrCreate(context, testPDFFile, listOf(testPDFFile), true)

            assertFalse(signedContainer.getName().endsWith("pdf"))
        }

    @Test
    fun signedContainer_getTimestamps_success() =
        runTest {
            val signedContainer =
                SignedContainer(context, null, null, true, listOf(mock(SignatureInterface::class.java)))

            val result = signedContainer.getTimestamps()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_getTimestamps_returnNullWithEmptyTimestampList() =
        runTest {
            val signedContainer = SignedContainer(context, null, null, true, listOf())

            val result = signedContainer.getTimestamps()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_getSignaturesStatusCount_success() =
        runTest {
            val signedContainer = openOrCreate(context, container, listOf(container), true)

            val signaturesStatuses = signedContainer.getSignaturesStatusCount()

            assertNotNull(signaturesStatuses)
            assertEquals(0, signaturesStatuses[ValidatorInterface.Status.Valid])
            assertEquals(2, signaturesStatuses[ValidatorInterface.Status.Unknown])
            assertEquals(0, signaturesStatuses[ValidatorInterface.Status.Invalid])
        }

    @Test
    fun signedContainer_getSignaturesStatusCount_successWithNoSignatures() =
        runTest {
            val noSignaturesContainer =
                getResourceFileAsFile(
                    context,
                    "example_no_signatures.asice",
                    ee.ria.DigiDoc.common.R.raw.example_no_signatures,
                )

            val signedContainer = openOrCreate(context, noSignaturesContainer, listOf(noSignaturesContainer), true)

            val signaturesStatuses = signedContainer.getSignaturesStatusCount()

            assertNotNull(signaturesStatuses)
            assertEquals(0, signaturesStatuses[ValidatorInterface.Status.Valid])
            assertEquals(0, signaturesStatuses[ValidatorInterface.Status.Unknown])
            assertEquals(0, signaturesStatuses[ValidatorInterface.Status.Invalid])
        }

    // Requires internet access, emulator should be running with internet access and RIA VPN on.
    @Test
    fun signedContainer_openOrCreate_successCreatingNewContainerWithPDFSignatures() =
        runTest {
            val isTestEnabled = System.getenv("WITH_EXTRA_DIGIDOC_TESTS")?.toBoolean() ?: false
            assumeTrue("Is test enabled: $isTestEnabled", isTestEnabled)

            val pdfContainer = openOrCreate(context, signedPdfDocument, listOf(signedPdfDocument), true)

            assertTrue(pdfContainer.getName().endsWith("pdf"))
        }
}
