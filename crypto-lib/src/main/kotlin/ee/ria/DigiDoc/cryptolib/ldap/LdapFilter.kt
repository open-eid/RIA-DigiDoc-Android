@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.ldap

import com.google.common.base.CharMatcher
import java.util.Locale

open class LdapFilter(private val query: String) {
    private val serialNumberSearch: Boolean = CharMatcher.inRange('0', '9').matchesAllOf(query)

    fun isSerialNumberSearch(): Boolean {
        return serialNumberSearch
    }

    fun getQuery(): String {
        return query
    }

    open fun filterString(): String {
        return if (isSerialNumberSearch()) {
            String.format(Locale.US, "(serialNumber=%s)", query)
        } else {
            String.format(Locale.US, "(cn=*%s*)", query)
        }
    }
}
