@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.domain.model.SomeObject

interface SomeService {
    fun getAllObjects(): List<SomeObject>

    fun get(id: Int): SomeObject
}
