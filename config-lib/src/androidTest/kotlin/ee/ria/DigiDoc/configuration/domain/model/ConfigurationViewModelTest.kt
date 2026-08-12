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

package ee.ria.DigiDoc.configuration.domain.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class ConfigurationViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var repository: ConfigurationRepository

    @Mock
    private lateinit var configurationProviderObserver: Observer<ConfigurationProvider?>

    private lateinit var proxySetting: ProxySetting
    private lateinit var manualProxy: ManualProxy

    private lateinit var viewModel: ConfigurationViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = ConfigurationViewModel(repository)

        proxySetting = ProxySetting.NO_PROXY
        manualProxy = ManualProxy("", 80, "", "")
    }

    @Test
    fun configurationViewModel_getConfiguration_success() =
        runBlocking {
            `when`(repository.getConfiguration())
                .thenReturn(createMockConfigurationProvider())
            viewModel.configuration.observeForever(configurationProviderObserver)

            val configuration = viewModel.getConfiguration()

            Assert.assertNotNull(configuration)
            Assert.assertEquals(1, configuration?.metaInf?.serial)
        }

    @Test
    fun configurationViewModel_fetchConfiguration_configurationIsFetchedAndChanged() =
        runBlocking {
            val configurationProvider = createMockConfigurationProvider()
            `when`(repository.getCentralConfiguration(proxySetting, manualProxy))
                .thenReturn(configurationProvider)
            viewModel.configuration.observeForever(configurationProviderObserver)

            viewModel.fetchConfiguration(0L, proxySetting, manualProxy)

            verify(repository, times(1)).getCentralConfiguration(proxySetting, manualProxy)
            verify(configurationProviderObserver, times(1))
                .onChanged(configurationProvider)
        }

    @Test
    fun configurationViewModel_fetchConfiguration_configurationFetchedButNotChanged() =
        runBlocking {
            val configurationProvider = createMockConfigurationProvider()
            `when`(repository.getCentralConfiguration(proxySetting, manualProxy))
                .thenReturn(configurationProvider)
            viewModel.configuration.observeForever(configurationProviderObserver)

            viewModel.fetchConfiguration(Date().time, proxySetting, manualProxy)

            verify(repository, times(1)).getCentralConfiguration(proxySetting, manualProxy)
            verify(configurationProviderObserver, never())
                .onChanged(configurationProvider)
        }

    private fun createMockConfigurationProvider(): ConfigurationProvider =
        ConfigurationProvider(
            metaInf = ConfigurationProvider.MetaInf("url", "date", 1, 1),
            sivaUrl = "sivaUrl",
            cdoc2Conf =
                mapOf(
                    DEFAULT_UUID_VALUE to
                        ConfigurationProvider.CDOC2Conf(
                            uuid = UUID.randomUUID(),
                            name = "RIA",
                            post = "https://cdoc2.id.ee:8443",
                            fetch = "https://cdoc2.id.ee:8444",
                        ),
                ),
            cdoc2Default = false,
            cdoc2UseKeyServer = false,
            cdoc2DefaultKeyServer = DEFAULT_UUID_VALUE,
            tslUrl = "tslUrl",
            tslCerts = emptyList(),
            tsaUrl = "tsaUrl",
            ldapPersonUrl = "ldapPersonUrl",
            ldapPersonUrls = listOf("ldapPersonUrl"),
            ldapCorpUrl = "ldapCorpUrl",
            midRestUrl = "midRestUrl",
            midSkRestUrl = "midSkRestUrl",
            sidV2RestUrl = "sidV2RestUrl",
            sidV2SkRestUrl = "sidV2SkRestUrl",
            certBundle = emptyList(),
            configurationLastUpdateCheckDate = Date(),
            configurationUpdateDate = Date(),
        )
}
