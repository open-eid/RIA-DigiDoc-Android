@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapperImpl
import ee.ria.libdigidocpp.Container
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideContainerWrapper(): ContainerWrapper = ContainerWrapperImpl()

    @Singleton
    @Provides
    fun provideSignedContainer(
        container: Container,
        file: File,
        isExistingContainer: Boolean,
    ): SignedContainer =
        SignedContainer(
            container = container,
            containerFile = file,
            isExistingContainer = isExistingContainer,
        )
}
