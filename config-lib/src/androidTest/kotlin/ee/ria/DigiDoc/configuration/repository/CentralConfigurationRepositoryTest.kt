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
    fun centralConfigurationRepository_fetchSignature_success(): Unit =
        runBlocking {
            val expectedSignature = "Example Signature"
            `when`(configurationService.fetchSignature()).thenReturn(expectedSignature)

            val result = centralConfigurationRepository.fetchSignature()

            assertEquals(expectedSignature, result)
            verify(configurationService).fetchSignature()
        }
}
