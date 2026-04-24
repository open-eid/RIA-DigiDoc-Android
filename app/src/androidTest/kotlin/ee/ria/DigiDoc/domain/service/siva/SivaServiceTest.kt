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

package ee.ria.DigiDoc.domain.service.siva

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import ee.ria.DigiDoc.common.Constant.ASICE_MIMETYPE
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.common.testfiles.asset.AssetFile
import ee.ria.DigiDoc.common.testfiles.file.TestFileUtil.Companion.createZipWithTextFile
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.libdigidoclib.init.LibdigidocLibraryLoader
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeCacheImpl
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolverImpl
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import java.io.File

class SivaServiceTest {
    @Mock
    private lateinit var mimeTypeResolver: MimeTypeResolver

    private lateinit var sivaService: SivaService
    private lateinit var containerFile: File
    private lateinit var signedPdfDocument: File

    @Before
    fun setUp() =
        runBlocking {
            mimeTypeResolver = MimeTypeResolverImpl(MimeTypeCacheImpl(context))
            context = InstrumentationRegistry.getInstrumentation().targetContext
            sivaService = SivaServiceImpl(mimeTypeResolver)
            signedPdfDocument =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example_signed_pdf.pdf",
                    ee.ria.DigiDoc.common.R.raw.example_signed_pdf,
                )

            containerFile =
                AssetFile.getResourceFileAsFile(
                    context,
                    "example.asice",
                    ee.ria.DigiDoc.common.R.raw.example,
                )
        }

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
                    LibdigidocLibraryLoader().init(context)
                    Initialization(configurationRepository).init(context)
                } catch (_: Exception) {
                }
            }
        }
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnTrueForDDOCContainer() {
        val file = createZipWithTextFile(DDOC_MIMETYPE, "mimetype")
        val files = listOf(file)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnTrueForASICSContainer() {
        val file = createZipWithTextFile(ASICS_MIMETYPE, "mimetype")
        val files = listOf(file)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnTrueForSignedPDF() {
        val files = listOf(signedPdfDocument)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnFalseForXades() {
        val file = createZipWithTextFile(ASICE_MIMETYPE, "signatures.xml")
        val files = listOf(file)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertFalse(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnTrueForCades() {
        val file = createZipWithTextFile(ASICE_MIMETYPE, "signatures001.p7s")
        val files = listOf(file)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertTrue(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnFalse() {
        val file = createZipWithTextFile(ASICE_MIMETYPE)
        val files = listOf(file)
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertFalse(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isSivaConfirmationNeeded_returnFalseForMultipleFiles() {
        val file = createZipWithTextFile(ASICE_MIMETYPE)
        val files = listOf(file, mock(File::class.java))
        val isSivaConfirmationNeeded = sivaService.isSivaConfirmationNeeded(context, files)
        assertFalse(isSivaConfirmationNeeded)
    }

    @Test
    fun sivaService_isTimestampedContainer_returnFalse() =
        runTest {
            val signedContainer = SignedContainer.openOrCreate(context, containerFile, listOf(containerFile), true)
            val isSivaConfirmationNeeded = sivaService.isTimestampedContainer(signedContainer)
            assertFalse(isSivaConfirmationNeeded)
        }

    @Test
    fun sivaService_isTimestampedContainer_returnFalseForMultipleFiles() =
        runTest {
            val signedContainer = SignedContainer.openOrCreate(context, containerFile, listOf(containerFile), true)
            val isSivaConfirmationNeeded = sivaService.isTimestampedContainer(signedContainer)
            assertFalse(isSivaConfirmationNeeded)
        }

    @Test
    fun sivaService_isTimestampedContainer_returnFalseIfSivaNotConfirmed() =
        runTest {
            val signedContainer = SignedContainer.openOrCreate(context, containerFile, listOf(containerFile), true)
            val isSivaConfirmationNeeded = sivaService.isTimestampedContainer(signedContainer)
            assertFalse(isSivaConfirmationNeeded)
        }
}
