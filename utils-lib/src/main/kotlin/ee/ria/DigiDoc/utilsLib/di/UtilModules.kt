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
