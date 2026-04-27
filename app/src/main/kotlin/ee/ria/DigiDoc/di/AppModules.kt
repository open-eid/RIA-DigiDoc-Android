/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.common.BuildVersionProvider
import ee.ria.DigiDoc.common.BuildVersionProviderImpl
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.configuration.deserializer.Cdoc2ConfDeserializer
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.cryptolib.init.CryptoInitialization
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapperImpl
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepository
import ee.ria.DigiDoc.domain.repository.fileopening.FileOpeningRepositoryImpl
import ee.ria.DigiDoc.domain.repository.siva.SivaRepository
import ee.ria.DigiDoc.domain.repository.siva.SivaRepositoryImpl
import ee.ria.DigiDoc.domain.service.IdCardService
import ee.ria.DigiDoc.domain.service.IdCardServiceImpl
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningService
import ee.ria.DigiDoc.domain.service.fileopening.FileOpeningServiceImpl
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.domain.service.siva.SivaServiceImpl
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.init.Initialization
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.manager.ActivityManagerImpl
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.root.RootChecker
import ee.ria.DigiDoc.root.RootCheckerImpl
import ee.ria.DigiDoc.smartcardreader.nfc.NfcSmartCardReaderManager
import ee.ria.DigiDoc.utils.locale.LocaleUtil
import ee.ria.DigiDoc.utils.locale.LocaleUtilImpl
import ee.ria.DigiDoc.utils.monitoring.CrashDetector
import ee.ria.DigiDoc.utils.monitoring.CrashDetectorImpl
import ee.ria.DigiDoc.utils.notification.NotificationUtil
import ee.ria.DigiDoc.utils.notification.NotificationUtilImpl
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideContentResolver(application: Application): ContentResolver = application.contentResolver

    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore =
        DataStore(
            context = context,
        )

    @Provides
    fun provideCDOC2Settings(
        @ApplicationContext context: Context,
        configurationRepository: ConfigurationRepository,
    ): CDOC2Settings =
        CDOC2Settings(
            context = context,
            configurationRepository,
        )

    @Provides
    fun provideFileOpeningService(): FileOpeningService = FileOpeningServiceImpl()

    @Provides
    fun provideFileOpeningRepository(
        fileOpeningService: FileOpeningService,
        sivaService: SivaService,
        cdoc2Settings: CDOC2Settings,
    ): FileOpeningRepository =
        FileOpeningRepositoryImpl(
            fileOpeningService = fileOpeningService,
            sivaService = sivaService,
            cdoc2Settings = cdoc2Settings,
        )

    @Provides
    fun provideNfcSmartCardReaderManager(): NfcSmartCardReaderManager = NfcSmartCardReaderManager()

    @Provides
    fun provideSivaService(mimeTypeResolver: MimeTypeResolver): SivaService = SivaServiceImpl(mimeTypeResolver)

    @Provides
    fun provideSivaRepository(sivaService: SivaService): SivaRepository =
        SivaRepositoryImpl(
            sivaService = sivaService,
        )

    @Provides
    @Singleton
    fun provideInitialization(configurationRepository: ConfigurationRepository): Initialization =
        Initialization(configurationRepository)

    @Provides
    @Singleton
    fun provideCryptoInitialization(): CryptoInitialization = CryptoInitialization()

    @Provides
    fun provideBuildVersionProvider(): BuildVersionProvider = BuildVersionProviderImpl()

    @Provides
    fun provideUserAgent(
        @ApplicationContext context: Context,
        buildVersionProvider: BuildVersionProvider,
    ): String = UserAgentUtil.getUserAgent(context, buildVersionProvider)

    @Provides
    @Singleton
    fun provideActivityManager(): ActivityManager = ActivityManagerImpl()

    @Provides
    @Singleton
    fun provideGson(): Gson {
        val type = object : TypeToken<Map<String, ConfigurationProvider.CDOC2Conf>>() {}.type

        return GsonBuilder()
            .registerTypeAdapter(type, Cdoc2ConfDeserializer())
            .create()
    }

    @Provides
    fun provideRootChecker(): RootChecker = RootCheckerImpl()

    @Provides
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    fun provideCrashDetector(crashlytics: FirebaseCrashlytics): CrashDetector = CrashDetectorImpl(crashlytics)

    @Provides
    fun provideIdCardService(
        @ApplicationContext context: Context,
        containerWrapper: ContainerWrapper,
        certificateService: CertificateService,
    ): IdCardService = IdCardServiceImpl(context, containerWrapper, certificateService)

    @Provides
    fun provideTextToSpeechWrapper(
        @ApplicationContext context: Context,
    ): TextToSpeechWrapper = TextToSpeechWrapperImpl(context)

    @Provides
    @Singleton
    fun provideLocaleUtil(): LocaleUtil = LocaleUtilImpl()

    @Provides
    fun provideNotificationPermissionUtil(
        @ApplicationContext context: Context,
    ): NotificationUtil = NotificationUtilImpl(context)
}
