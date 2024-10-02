@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.speech.tts.TextToSpeech
import com.google.firebase.crashlytics.FirebaseCrashlytics
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
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepositoryImpl
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningService
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningServiceImpl
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.domain.service.siva.SivaServiceImpl
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.manager.ActivityManagerImpl
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.root.RootChecker
import ee.ria.DigiDoc.root.RootCheckerImpl
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.utils.monitoring.CrashDetector
import ee.ria.DigiDoc.utils.monitoring.CrashDetectorImpl
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
    fun provideDataStore(context: Context): DataStore =
        DataStore(
            context = context,
        )

    @Provides
    @Singleton
    fun provideTextToSpeech(
        @ApplicationContext context: Context,
    ): TextToSpeech {
        return TextToSpeech(context, null)
    }

    @Provides
    fun provideFileOpeningService(): FileOpeningService = FileOpeningServiceImpl()

    @Provides
    fun provideFileOpeningRepository(
        fileOpeningService: FileOpeningService,
        sivaService: SivaService,
    ): FileOpeningRepository =
        FileOpeningRepositoryImpl(
            fileOpeningService = fileOpeningService,
            sivaService = sivaService,
        )

    @Provides
    fun provideNfcSmartCardReaderManager(): NfcSmartCardReaderManager = NfcSmartCardReaderManager()

    @Provides
    fun provideSivaService(): SivaService = SivaServiceImpl()

    @Provides
    fun provideSivaRepository(sivaService: SivaService): SivaRepository =
        SivaRepositoryImpl(
            sivaService = sivaService,
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
    @Singleton
    fun provideActivityManager(): ActivityManager {
        return ActivityManagerImpl()
    }

    @Provides
    fun provideGson(): Gson = Gson()

    @Provides
    fun provideRootChecker(): RootChecker = RootCheckerImpl()

    @Provides
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    fun provideCrashDetector(crashlytics: FirebaseCrashlytics): CrashDetector = CrashDetectorImpl(crashlytics)
}
