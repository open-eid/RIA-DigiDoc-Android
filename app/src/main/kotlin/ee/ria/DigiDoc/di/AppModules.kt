@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepositoryImpl
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.domain.service.FileOpeningServiceImpl
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.mobileId.MobileSignServiceImpl
import ee.ria.DigiDoc.smartId.SmartSignService
import ee.ria.DigiDoc.smartId.SmartSignServiceImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideContentResolver(application: Application): ContentResolver {
        return application.contentResolver
    }

    @Provides
    fun provideContext(
        @ApplicationContext appContext: Context,
    ): Context {
        return appContext
    }

    @Provides
    fun provideDataStore(context: Context): DataStore =
        DataStore(
            context = context,
        )

    @Provides
    fun provideFileOpeningService(): FileOpeningService = FileOpeningServiceImpl()

    @Provides
    fun provideFileOpeningRepository(fileOpeningService: FileOpeningService): FileOpeningRepository =
        FileOpeningRepositoryImpl(
            fileOpeningService = fileOpeningService,
        )

    @Provides
    fun provideMobileSignService(): MobileSignService = MobileSignServiceImpl()

    @Provides
    fun provideSmartSignService(): SmartSignService = SmartSignServiceImpl()

    @Provides
    fun provideConfigurationRepository(): ConfigurationRepository = ConfigurationRepositoryImpl()
}
