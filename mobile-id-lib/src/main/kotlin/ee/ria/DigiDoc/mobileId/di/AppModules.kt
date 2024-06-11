@file:Suppress("PackageName")

package ee.ria.DigiDoc.mobileId.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.mobileId.MobileSignServiceImpl
import ee.ria.DigiDoc.network.mid.rest.ServiceGenerator
import ee.ria.DigiDoc.network.mid.rest.ServiceGeneratorImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideServiceGenerator(): ServiceGenerator = ServiceGeneratorImpl()

    @Provides
    fun provideMobileSignService(
        serviceGenerator: ServiceGenerator,
        containerWrapper: ContainerWrapper,
    ): MobileSignService =
        MobileSignServiceImpl(
            serviceGenerator = serviceGenerator,
            containerWrapper = containerWrapper,
        )
}
