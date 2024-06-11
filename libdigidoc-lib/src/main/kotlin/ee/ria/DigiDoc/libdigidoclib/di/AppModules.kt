@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapperImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideContainerWrapper(): ContainerWrapper = ContainerWrapperImpl()
}
