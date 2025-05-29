@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
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

    private fun createMockConfigurationProvider(): ConfigurationProvider {
        return ConfigurationProvider(
            ConfigurationProvider.MetaInf("url", "date", 1, 1),
            "sivaUrl",
            mapOf(
                "00000000-0000-0000-0000-000000000000" to
                    ConfigurationProvider.CDOC2Conf(
                        name = "RIA",
                        post = "https://cdoc2.id.ee:8443",
                        fetch = "https://cdoc2.id.ee:8444",
                    ),
            ),
            false,
            "00000000-0000-0000-0000-000000000000",
            "tslUrl",
            emptyList(),
            "tsaUrl",
            emptyMap(),
            "ldapPersonUrl",
            "ldapCorpUrl",
            "midRestUrl",
            "midSkRestUrl",
            "sidV2RestUrl",
            "sidV2SkRestUrl",
            emptyList(),
            Date(),
            Date(),
        )
    }
}
