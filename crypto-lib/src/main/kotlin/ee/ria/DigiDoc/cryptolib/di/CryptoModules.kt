@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepository
import ee.ria.DigiDoc.cryptolib.repository.RecipientRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
class CryptoModules {
    @Provides
    fun provideRecipientRepository(
        configurationRepository: ConfigurationRepository,
        certificateService: CertificateService,
    ): RecipientRepository = RecipientRepositoryImpl(configurationRepository, certificateService)
}
