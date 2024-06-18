@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.common.BuildVersionProviderImpl
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.service.FileOpeningService
import ee.ria.DigiDoc.domain.service.FileOpeningServiceImpl
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import javax.inject.Singleton

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
    fun provideDataStore(application: Application): DataStore =
        DataStore(
            application = application,
        )

    @Provides
    fun provideFileOpeningService(): FileOpeningService = FileOpeningServiceImpl()

    @Provides
    fun provideFileOpeningRepository(fileOpeningService: FileOpeningService): FileOpeningRepository =
        FileOpeningRepositoryImpl(
            fileOpeningService = fileOpeningService,
        )

    @Provides
    @Singleton
    fun provideInitialization(configurationRepository: ConfigurationRepository): Initialization {
        return Initialization(configurationRepository)
    }

    @Provides
    fun provideBuildVersionProvider(): BuildVersionProvider {
        return BuildVersionProviderImpl()
    }

    @Provides
    fun provideUserAgent(
        context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): String {
        return UserAgentUtil.getUserAgent(context, buildVersionProvider)
    }

    @Provides
    fun provideGson(): Gson = Gson()
}
