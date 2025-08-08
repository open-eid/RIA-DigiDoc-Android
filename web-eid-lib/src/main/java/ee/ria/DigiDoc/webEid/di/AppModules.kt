@file:Suppress("PackageName")

package ee.ria.DigiDoc.webEid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.webEid.WebEidAuthService
import ee.ria.DigiDoc.webEid.WebEidAuthServiceImpl
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParser
import ee.ria.DigiDoc.webEid.domain.model.WebEidAuthParserImpl

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    fun provideWebEidAuthParser(): WebEidAuthParser = WebEidAuthParserImpl()

    @Provides
    fun provideWebEidAuthService(parser: WebEidAuthParser): WebEidAuthService = WebEidAuthServiceImpl(parser)
}
