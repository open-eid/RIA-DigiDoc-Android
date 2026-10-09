// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.WebEidAuthServiceImpl
import ee.ria.DigiDoc.webEid.WebEidSignService
import ee.ria.DigiDoc.webEid.WebEidSignServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    @Singleton
    fun provideWebEidAuthService(
        @ApplicationContext context: Context,
    ): WebEidAuthService = WebEidAuthServiceImpl(context)

    @Provides
    @Singleton
    fun provideWebEidSignService(): WebEidSignService = WebEidSignServiceImpl()
}
