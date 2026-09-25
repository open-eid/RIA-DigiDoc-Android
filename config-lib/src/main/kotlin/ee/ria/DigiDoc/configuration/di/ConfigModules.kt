// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.configuration.ConfigurationProperty
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifier
import ee.ria.DigiDoc.configuration.ConfigurationSignatureVerifierImpl
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoader
import ee.ria.DigiDoc.configuration.loader.ConfigurationLoaderImpl
import ee.ria.DigiDoc.configuration.properties.ConfigurationProperties
import ee.ria.DigiDoc.configuration.properties.ConfigurationPropertiesImpl
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.CentralConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.configuration.service.CentralConfigurationService
import ee.ria.DigiDoc.configuration.service.CentralConfigurationServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ConfigModules {
    @Provides
    @Singleton
    fun provideConfigurationLoader(
        gson: Gson,
        centralConfigurationRepository: CentralConfigurationRepository,
        configurationProperty: ConfigurationProperty,
        configurationProperties: ConfigurationProperties,
        configurationSignatureVerifier: ConfigurationSignatureVerifier,
    ): ConfigurationLoader =
        ConfigurationLoaderImpl(
            gson,
            centralConfigurationRepository,
            configurationProperty,
            configurationProperties,
            configurationSignatureVerifier,
        )

    @Provides
    @Singleton
    fun provideConfigurationProperties(): ConfigurationProperties = ConfigurationPropertiesImpl()

    @Provides
    fun provideConfigurationSignatureVerifier(): ConfigurationSignatureVerifier = ConfigurationSignatureVerifierImpl()

    @Provides
    fun provideConfigurationRepository(configurationLoader: ConfigurationLoader): ConfigurationRepository =
        ConfigurationRepositoryImpl(configurationLoader)

    @Provides
    fun provideCentralConfigurationRepository(
        centralConfigurationService: CentralConfigurationService,
    ): CentralConfigurationRepository = CentralConfigurationRepositoryImpl(centralConfigurationService)

    @Provides
    fun provideCentralConfigurationService(impl: CentralConfigurationServiceImpl): CentralConfigurationService = impl

    @Singleton
    @Provides
    fun provideConfigurationProperty(): ConfigurationProperty = ConfigurationProperty()
}
