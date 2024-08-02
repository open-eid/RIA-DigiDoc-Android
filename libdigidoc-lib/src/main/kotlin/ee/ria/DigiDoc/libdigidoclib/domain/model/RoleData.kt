@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

data class RoleData(
    val roles: List<String>,
    val city: String,
    val state: String,
    val country: String,
    val zip: String,
)
