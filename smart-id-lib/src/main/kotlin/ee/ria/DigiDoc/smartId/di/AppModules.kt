@file:Suppress("PackageName")

package ee.ria.DigiDoc.smartId.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.network.sid.rest.ServiceGenerator
import ee.ria.DigiDoc.network.sid.rest.ServiceGeneratorImpl
import ee.ria.DigiDoc.smartId.SmartSignService
import ee.ria.DigiDoc.smartId.SmartSignServiceImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideServiceGenerator(): ServiceGenerator = ServiceGeneratorImpl()

    @Provides
    fun provideSmartSignService(
        serviceGenerator: ServiceGenerator,
        containerWrapper: ContainerWrapper,
    ): SmartSignService =
        SmartSignServiceImpl(
            serviceGenerator = serviceGenerator,
            containerWrapper = containerWrapper,
        )
}
