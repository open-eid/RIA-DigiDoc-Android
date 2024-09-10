@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.di

import android.content.Context
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
        context: Context,
        container: Container,
        file: File,
        isExistingContainer: Boolean,
    ): SignedContainer =
        SignedContainer(
            context = context,
            container = container,
            containerFile = file,
            isExistingContainer = isExistingContainer,
        )
}
