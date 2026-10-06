// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.CertType
import ee.ria.DigiDoc.cryptolib.CryptoContainer
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class EncryptViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var sivaRepository: SivaRepository

    @Mock
    private lateinit var mimeTypeResolver: MimeTypeResolver

    @Mock
    private lateinit var fileOpeningRepository: FileOpeningRepository

    @Mock
    private lateinit var configurationRepository: ConfigurationRepository

    private lateinit var context: Context
    private lateinit var viewModel: EncryptViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel =
            EncryptViewModel(
                sivaRepository,
                mimeTypeResolver,
                context.contentResolver,
                fileOpeningRepository,
                CDOC2Settings(context, configurationRepository),
                DataStore(context),
            )
    }

    @Test
    fun encryptViewModel_isDecryptButtonShown_returnTrueForCdoc1WhenAllRecipientsExpired() {
        val container = encryptedContainer("cdoc", recipient(yearsFromNow = -1))

        assertTrue(viewModel.isDecryptButtonShown(container, false))
    }

    @Test
    fun encryptViewModel_isDecryptButtonShown_returnFalseForCdoc2WhenAllRecipientsExpired() {
        val container = encryptedContainer("cdoc2", recipient(yearsFromNow = -1))

        assertFalse(viewModel.isDecryptButtonShown(container, false))
    }

    @Test
    fun encryptViewModel_isDecryptButtonShown_returnTrueForCdoc2WhenAnyRecipientValid() {
        val container = encryptedContainer("cdoc2", recipient(yearsFromNow = -1), recipient(yearsFromNow = 1))

        assertTrue(viewModel.isDecryptButtonShown(container, false))
    }

    @Test
    fun encryptViewModel_isDecryptButtonShown_returnFalseForNestedCdoc1WhenAllRecipientsExpired() {
        val container = encryptedContainer("cdoc", recipient(yearsFromNow = -1))

        assertFalse(viewModel.isDecryptButtonShown(container, true))
    }

    private fun encryptedContainer(
        extension: String,
        vararg recipients: Addressee,
    ): CryptoContainer =
        CryptoContainer(
            context = context,
            file = File(context.cacheDir, "container.$extension"),
            dataFiles = arrayListOf(),
            recipients = arrayListOf(*recipients),
            decrypted = false,
            encrypted = true,
        )

    private fun recipient(yearsFromNow: Int): Addressee =
        Addressee(
            data = byteArrayOf(),
            identifier = "47101010033",
            serialNumber = null,
            givenName = null,
            surname = "TestSurname",
            certType = CertType.IDCardType,
            validTo = Calendar.getInstance().apply { add(Calendar.YEAR, yearsFromNow) }.time,
            concatKDFAlgorithmURI = null,
        )
}
