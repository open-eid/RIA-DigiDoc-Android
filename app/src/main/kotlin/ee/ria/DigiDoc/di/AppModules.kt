@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import android.app.Application
import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.repository.SomeRepository
import ee.ria.DigiDoc.domain.repository.SomeRepositoryImpl
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.domain.service.FileOpeningServiceImpl
import ee.ria.DigiDoc.domain.service.SomeService
import ee.ria.DigiDoc.domain.service.SomeServiceImpl
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.mobileId.MobileSignServiceImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideSomeService(): SomeService = SomeServiceImpl()

    @Provides
    fun provideSomeRepository(someService: SomeService): SomeRepository =
        SomeRepositoryImpl(
            someService = someService,
        )

    @Provides
    fun provideContentResolver(application: Application): ContentResolver {
        return application.contentResolver
    }

    @Provides
    fun provideFileOpeningService(): FileOpeningService = FileOpeningServiceImpl()

    @Provides
    fun provideFileOpeningRepository(fileOpeningService: FileOpeningService): FileOpeningRepository =
        FileOpeningRepositoryImpl(
            fileOpeningService = fileOpeningService,
        )

    @Provides
    fun provideMobileSignService(): MobileSignService = MobileSignServiceImpl()
}
