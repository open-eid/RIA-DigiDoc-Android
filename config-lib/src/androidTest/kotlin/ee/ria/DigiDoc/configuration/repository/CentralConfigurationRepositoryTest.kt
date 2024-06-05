@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.repository

import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class CentralConfigurationRepositoryTest {
    @Mock
    private lateinit var configurationService: CentralConfigurationService

    private lateinit var centralConfigurationRepository: CentralConfigurationRepositoryImpl

    @Before
    fun setUp() {
        configurationService = mock(CentralConfigurationService::class.java)
        centralConfigurationRepository = CentralConfigurationRepositoryImpl(configurationService)
    }

    @Test
    fun centralConfigurationRepository_fetchConfiguration_success(): Unit =
        runBlocking {
            val expectedConfiguration = "Example Configuration"
            `when`(configurationService.fetchConfiguration()).thenReturn(expectedConfiguration)

            val result = centralConfigurationRepository.fetchConfiguration()

            assertEquals(expectedConfiguration, result)
            verify(configurationService).fetchConfiguration()
        }

    @Test
    fun centralConfigurationRepository_fetchPublicKey_success(): Unit =
        runBlocking {
            val expectedPublicKey = "Example Public Key"
            `when`(configurationService.fetchPublicKey()).thenReturn(expectedPublicKey)

            val result = centralConfigurationRepository.fetchPublicKey()

            assertEquals(expectedPublicKey, result)
            verify(configurationService).fetchPublicKey()
        }

    @Test
    fun centralConfigurationRepository_fetchSignature_success(): Unit =
        runBlocking {
            val expectedSignature = "Example Signature"
            `when`(configurationService.fetchSignature()).thenReturn(expectedSignature)

            val result = centralConfigurationRepository.fetchSignature()

            assertEquals(expectedSignature, result)
            verify(configurationService).fetchSignature()
        }
}
