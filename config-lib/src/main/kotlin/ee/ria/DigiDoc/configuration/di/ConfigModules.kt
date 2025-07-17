@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideConfigurationRepository(
        @ApplicationContext context: Context,
        configurationLoader: ConfigurationLoader,
    ): ConfigurationRepository = ConfigurationRepositoryImpl(context, configurationLoader)

    @Provides
    fun provideCentralConfigurationRepository(
        centralConfigurationService: CentralConfigurationService,
    ): CentralConfigurationRepository = CentralConfigurationRepositoryImpl(centralConfigurationService)

    @Provides
    fun provideCentralConfigurationService(
        userAgent: String,
        configurationProperty: ConfigurationProperty,
    ): CentralConfigurationService = CentralConfigurationServiceImpl(userAgent, configurationProperty)

    @Singleton
    @Provides
    fun provideConfigurationProperty(): ConfigurationProperty = ConfigurationProperty()
}
