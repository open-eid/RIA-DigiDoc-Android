@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository

import ee.ria.DigiDoc.domain.model.SomeObject
import kotlinx.coroutines.flow.Flow

interface SomeRepository {
    fun getAllObjects(): Flow<List<SomeObject>>

    fun getObjectById(id: Int): Flow<SomeObject>
}
