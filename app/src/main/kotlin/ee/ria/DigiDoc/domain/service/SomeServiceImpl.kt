@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.domain.model.SomeObject
import javax.inject.Singleton

@Singleton
class SomeServiceImpl : SomeService {
    override fun getAllObjects(): List<SomeObject> {
        return listOf(SomeObject())
    }

    override fun get(id: Int): SomeObject {
        return SomeObject(id = id)
    }
}
