@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

data class SomeObject(
    // @field:SerializedName("id")
    val id: Int? = null,
    // @field:SerializedName("name")
    val name: String? = null,
    // @field:SerializedName("description")
    val description: String? = null,
)
