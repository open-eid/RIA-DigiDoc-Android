// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UnsupportedVersionViewModelTest {
    @Mock
    private lateinit var configurationLoader: ConfigurationLoader

    private val configurationFlow = MutableStateFlow<ConfigurationProvider?>(null)

    private lateinit var viewModel: UnsupportedVersionViewModel

    @Before
    fun setUp() {
        `when`(configurationLoader.getConfigurationFlow()).thenReturn(configurationFlow)
        viewModel = UnsupportedVersionViewModel(configurationLoader)
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_returnTrueWhenAppVersionIsLower() {
        configurationFlow.value = configuration("3.2.0.200")

        assertTrue(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_returnFalseWhenAppVersionIsNotLower() {
        configurationFlow.value = configuration("3.2.0.100")

        assertFalse(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_returnFalseWhenKeyIsMissing() {
        configurationFlow.value = configuration(null)

        assertFalse(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_returnFalseWhenConfigurationIsNotLoaded() {
        assertFalse(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_ignoresConfigurationUpdatedAfterFirstCheck() {
        configurationFlow.value = configuration(null)
        assertFalse(viewModel.isAppVersionUnsupported("3.2.0.100"))

        configurationFlow.value = configuration("3.2.0.200")

        assertFalse(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    @Test
    fun unsupportedVersionViewModel_isAppVersionUnsupported_staysUnsupportedAfterConfigurationUpdate() {
        configurationFlow.value = configuration("3.2.0.200")
        assertTrue(viewModel.isAppVersionUnsupported("3.2.0.100"))

        configurationFlow.value = configuration(null)

        assertTrue(viewModel.isAppVersionUnsupported("3.2.0.100"))
    }

    private fun configuration(riaddUnsupported: String?): ConfigurationProvider {
        val configuration = mock(ConfigurationProvider::class.java)
        `when`(configuration.riaddUnsupported).thenReturn(riaddUnsupported)
        return configuration
    }
}
