@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib

import android.content.Context
import android.graphics.pdf.PdfDocument
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.PDSignature
import ee.ria.DigiDoc.common.Constant.CONTAINER_MIME_TYPE
import ee.ria.DigiDoc.common.Constant.DEFAULT_CONTAINER_EXTENSION
import ee.ria.DigiDoc.common.Constant.DEFAULT_MIME_TYPE
import ee.ria.DigiDoc.common.test.AssetFile
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
import ee.ria.DigiDoc.libdigidoclib.exceptions.ContainerUninitializedException
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.utilsLib.container.ContainerUtil
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testFile = File.createTempFile("testFile", ".txt", context.filesDir)
        dataFile1 = File.createTempFile("dataFile1", ".txt", context.filesDir)
        dataFile2 = File.createTempFile("dataFile2", ".txt", context.filesDir)
        dataFile3 = File.createTempFile("dataFile3", ".txt", context.filesDir)
        container = AssetFile.getResourceFileAsFile(context, "example.asice", ee.ria.DigiDoc.common.R.raw.example)
    }

    @After
    fun tearDown() {
        testFile.delete()
        dataFile1.delete()
        dataFile2.delete()
        dataFile3.delete()
        SignedContainer.cleanup()
    }

    @Test
    fun signedContainer_rawContainer_success() =
        runTest {
            val dataFiles = listOf(testFile)

            openOrCreate(context, testFile, dataFiles)

            val result = SignedContainer.rawContainer()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_rawContainerFile_success() =
        runTest {
            val dataFiles = listOf(testFile)

            openOrCreate(context, testFile, dataFiles)

            val result = SignedContainer.rawContainerFile()

            assertNotNull(result)
        }

    @Test
    fun signedContainer_isExistingContainer_false() =
        runTest {
            val dataFiles = listOf(testFile)

            openOrCreate(context, testFile, dataFiles)

            val result = SignedContainer.isExistingContainer()

            assertFalse(result)
        }

    @Test
    fun signedContainer_isExistingContainer_true() =
        runTest {
            openOrCreate(context, container, listOf(container))

            val result = SignedContainer.isExistingContainer()

            assertTrue(result)
        }

    @Test
    fun signedContainer_containerMimetype_returnsValidMimetype() =
        runTest {
            val dataFiles = listOf(testFile)

            openOrCreate(context, testFile, dataFiles)

            val result = SignedContainer.containerMimetype()

            assertEquals("application/vnd.etsi.asic-e+zip", result)
        }

    @Test
    fun signedContainer_containerMimetype_returnNullWithNoContainer() =
        runTest {
            val result = SignedContainer.containerMimetype()

            assertNull(result)
        }

    @Test
    fun signedContainer_setName_successWhenSettingName() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

            signedContainer.setName("testName")

            assertEquals("testName.asice", signedContainer.getName())
        }

    @Test
    fun signedContainer_mimeType_returnContainerMimeType() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

            val result = signedContainer.getContainerFile()?.let { SignedContainer.mimeType(it) }

            assertEquals(CONTAINER_MIME_TYPE, result)
        }

    @Test
    fun signedContainer_mimeType_returnFileMimeType() =
        runTest {
            val file = File.createTempFile("testFile", ".bmp", context.filesDir)
            val result = SignedContainer.mimeType(file)

            assertEquals("image/x-ms-bmp", result)
        }

    @Test
    fun signedContainer_mimeType_returnDefaultMimeType() =
        runTest {
            val file = File.createTempFile("testFile", ".xyzabc", context.filesDir)
            val result = SignedContainer.mimeType(file)

            assertEquals(DEFAULT_MIME_TYPE, result)
        }

    @Test
    fun signedContainer_getDataFile_successWhenGettingDataFile() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

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
            val signedContainer = openOrCreate(context, container, listOf(container))

            assertEquals(2, signedContainer.getSignatures().size)

            val result = signedContainer.removeSignature(signedContainer.getSignatures().first())

            assertEquals(1, result.getSignatures().size)
        }

    @Test
    fun signedContainer_removeDataFile_successWhenRemovingDataFile() =
        runTest {
            val dataFiles = listOf(testFile, dataFile1, dataFile2)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

            assertEquals(3, signedContainer.getDataFiles().size)

            val result = signedContainer.removeDataFile(signedContainer.getDataFiles().first())

            assertEquals(2, result.getDataFiles().size)
        }

    @Test(expected = ContainerDataFilesEmptyException::class)
    fun signedContainer_removeDataFile_throwsException() =
        runTest {
            val dataFiles = listOf(testFile)
            val signedContainer = openOrCreate(context, testFile, dataFiles)
            signedContainer.removeDataFile(signedContainer.getDataFiles().first())
        }

    @Test
    fun signedContainer_addDataFiles_successWhenAddingDataFiles() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

            val result = signedContainer.addDataFiles(listOf(dataFile1, dataFile2))

            assertEquals(3, result.getDataFiles().size)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithSingleDataFile() =
        runTest {
            val dataFiles = listOf(testFile)

            val signedContainer = openOrCreate(context, testFile, dataFiles)

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

            val signedContainer = openOrCreate(context, testFile, dataFiles)

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
            val signedContainer = openOrCreate(context, container, listOf(container))

            assertEquals(true, signedContainer.isExistingContainer())
            assertEquals(1, signedContainer.getDataFiles().size)
            assertEquals(2, signedContainer.getSignatures().size)
        }

    @Test(expected = NoSuchElementException::class)
    fun signedContainer_openOrCreate_throwExceptionWhenCreatingContainerWithEmptyDataFiles() =
        runTest {
            openOrCreate(context, testFile, emptyList())
        }

    @Test(expected = Exception::class)
    fun signedContainer_openOrCreate_throwExceptionWhenCreatingContainerWithInvalidDataFile() =
        runTest {
            openOrCreate(context, testFile, listOf(File("testDataFile")))
        }

    @Test(expected = IOException::class)
    fun signedContainer_openOrCreate_throwExceptionWhenOpeningInvalidContainer() =
        runTest {
            val invalidContainer = File.createTempFile("testFile", ".asice", context.filesDir)
            openOrCreate(context, invalidContainer, listOf(invalidContainer))
            invalidContainer.delete()
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerAndFilteringDuplicateDataFiles() =
        runTest {
            val createdContainer = openOrCreate(context, testFile, listOf(dataFile1, dataFile1))

            assertEquals(1, createdContainer.getDataFiles().size)
        }

    @Test(expected = NoSuchElementException::class)
    fun signedContainer_openOrCreate_throwExceptionWhenOpeningNonExistentContainer() =
        runTest {
            openOrCreate(context, File("nonExistentFile"), listOf(File("nonExistentDataFile")))
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithCustomFileName() =
        runTest {
            val customFileName = File.createTempFile("customTestFile", ".qwerty", context.filesDir)

            val signedContainer = openOrCreate(context, customFileName, listOf(dataFile1))

            customFileName.delete()

            assertEquals("${customFileName.name}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithSpecialCharacters() =
        runTest {
            val testDataFile = File.createTempFile("dataFile1!@#$€%&=?", ".txt", context.filesDir)

            val createdContainer = openOrCreate(context, testFile, listOf(testDataFile))

            testDataFile.delete()

            assertNotNull(createdContainer)
            assertEquals(testDataFile.name, createdContainer.getDataFiles().first().fileName)
        }

    @Test
    fun signedContainer_openOrCreate_successWhenCreatingContainerWithNoExtensionDataFile() =
        runTest {
            val testDataFile = File.createTempFile("dataFile1", "", context.filesDir)

            val signedContainer = openOrCreate(context, testDataFile, listOf(testDataFile))

            testDataFile.delete()

            assertEquals("${testDataFile.name}.$DEFAULT_CONTAINER_EXTENSION", signedContainer.getName())
        }

    @Test
    fun signedContainer_container_successWhenGettingNewlyCreatedInitializedContainer() =
        runTest {
            openOrCreate(context, testFile, listOf(dataFile1))

            val initializedContainer = SignedContainer.container()

            assertEquals(false, initializedContainer.isExistingContainer())
            assertEquals("${testFile.name}.$DEFAULT_CONTAINER_EXTENSION", initializedContainer.getName())
            assertEquals(1, initializedContainer.getDataFiles().size)
            assertEquals(0, initializedContainer.getSignatures().size)
        }

    @Test
    fun signedContainer_container_successWhenGettingExistingInitializedContainer() =
        runTest {
            openOrCreate(context, container, listOf(container))

            val initializedContainer = SignedContainer.container()

            assertEquals(true, initializedContainer.isExistingContainer())
            assertEquals("example.asice", initializedContainer.getName())
            assertEquals(1, initializedContainer.getDataFiles().size)
            assertEquals(2, initializedContainer.getSignatures().size)
        }

    @Test(expected = ContainerUninitializedException::class)
    fun signedContainer_container_throwExceptionWhenGettingUninitializedContainer() =
        runTest {
            SignedContainer.container()
        }

    @Test
    fun signedContainer_container_matchesContainerSignatureData() =
        runTest {
            openOrCreate(context, container, listOf(container))
            val signedContainer = SignedContainer.container()

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
            openOrCreate(context, container, listOf(container))
            val signedContainer = SignedContainer.container()

            assertEquals("text.txt", signedContainer.getDataFiles().first().fileName)
            assertEquals(3, signedContainer.getDataFiles().first().fileSize)
            assertEquals("application/octet-stream", signedContainer.getDataFiles().first().mediaType)
        }

    @Test
    fun signedContainer_container_matchesContainerValidatorData() =
        runTest {
            openOrCreate(context, container, listOf(container))
            val signedContainer = SignedContainer.container()

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

            val signedContainer = openOrCreate(context, testPDFFile, listOf(testPDFFile))

            testPDFFile.delete()

            assertFalse(signedContainer.getName().endsWith("pdf"))
        }

    // Requires internet access, emulator should be running with internet access and RIA VPN on.
    @Test
    fun signedContainer_openOrCreate_successCreatingNewContainerWithPDFSignatures() =
        runTest {
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(100, 100, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            pdfDocument.finishPage(page)

            val testPDFFile = File(context.filesDir, "testFile.pdf")
            val signedTestPDFFile = File(context.filesDir, "signedTestFile.pdf")
            try {
                val fileOutputStream = FileOutputStream(testPDFFile)
                pdfDocument.writeTo(fileOutputStream)
                fileOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                pdfDocument.close()
            }

            try {
                val document = PDDocument.load(testPDFFile)

                val signature = PDSignature()
                signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE)
                signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED)
                signature.name = "Test Signature"
                signature.location = "Location"
                signature.reason = "Reason"

                document.addSignature(signature)

                document.save(signedTestPDFFile)
                document.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val pdfContainer = openOrCreate(context, signedTestPDFFile, listOf(signedTestPDFFile))

            testPDFFile.delete()
            signedTestPDFFile.delete()

            assertTrue(pdfContainer.getName().endsWith("pdf"))
        }
}
