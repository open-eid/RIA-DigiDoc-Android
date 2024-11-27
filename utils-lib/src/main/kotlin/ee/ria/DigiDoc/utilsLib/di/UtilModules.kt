@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeCache
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeCacheImpl
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolverImpl
import java.util.logging.Logger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilModules {
    @Provides
    @Singleton
    fun provideLoggingUtil(
        @ApplicationContext context: Context,
    ): LoggingUtil =
        LoggingUtil().apply {
            LoggingUtil.initialize(context, Logger.getLogger(UtilModules::class.java.name), false)
        }

    @Provides
    @Singleton
    fun provideMimeTypeCache(
        @ApplicationContext context: Context,
    ): MimeTypeCache = MimeTypeCacheImpl(context)

    @Provides
    @Singleton
    fun provideMimeTypeResolver(mimeTypeCache: MimeTypeCache): MimeTypeResolver = MimeTypeResolverImpl(mimeTypeCache)
}
