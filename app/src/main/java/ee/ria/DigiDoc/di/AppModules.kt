@file:Suppress("PackageName")

package ee.ria.DigiDoc.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ee.ria.DigiDoc.domain.repository.SomeRepository
import ee.ria.DigiDoc.domain.repository.SomeRepositoryImpl
import ee.ria.DigiDoc.domain.service.SomeService
import ee.ria.DigiDoc.domain.service.SomeServiceImpl

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
}
