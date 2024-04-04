@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import ee.ria.DigiDoc.domain.model.SomeObject
import ee.ria.DigiDoc.domain.service.SomeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SomeRepositoryImpl
    @Inject
    constructor(
        private val someService: SomeService,
    ) : SomeRepository {
        override fun getAllObjects(): Flow<List<SomeObject>> =
            flow {
                emit(someService.getAllObjects())
            }.flowOn(Dispatchers.IO)

        override fun getObjectById(id: Int): Flow<SomeObject> =
            flow {
                emit(someService.get(id))
            }.flowOn(Dispatchers.IO)
    }
