@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.certificate.CertificateServiceImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideCertificateService(): CertificateService = CertificateServiceImpl()
}
