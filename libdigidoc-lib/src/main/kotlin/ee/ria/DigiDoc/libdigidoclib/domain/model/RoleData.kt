@file:Suppress("PackageName")

package ee.ria.DigiDoc.libdigidoclib.domain.model

import java.io.Serializable

class RoleData(
    val roles: List<String>,
    val city: String,
    val state: String,
    val country: String,
    val zip: String,
) : Serializable {
    override fun toString(): String {
        return "RoleData{" +
            "roles='" + java.lang.String.join(", ", roles) + '\'' +
            ", city='" + city + '\'' +
            ", state='" + state + '\'' +
            ", country='" + country + '\'' +
            ", zip='" + zip + '\'' +
            '}'
    }

    companion object {
        fun create(
            roles: List<String>,
            city: String,
            state: String,
            country: String,
            zip: String,
        ): RoleData {
            return RoleData(roles, city, state, country, zip)
        }
    }
}
