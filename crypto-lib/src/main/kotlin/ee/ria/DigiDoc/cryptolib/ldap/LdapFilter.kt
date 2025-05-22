@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.ldap

import java.util.Locale

open class LdapFilter(private val query: String) {
    fun getQuery(): String {
        return query
    }

    open fun filterString(): String {
        return when {
            isPersonalCode(getQuery()) -> {
                String.format(Locale.US, "(serialNumber=PNOEE-%s)", query)
            }
            isCompanyCode(getQuery()) -> {
                String.format(Locale.US, "(serialNumber=%s)", query)
            }
            else -> {
                String.format(Locale.US, "(cn=*%s*)", query)
            }
        }
    }

    fun isPersonalCode(inputString: String): Boolean {
        return inputString.length == 11 && inputString.all { it.isDigit() }
    }

    fun isCompanyCode(inputString: String): Boolean {
        return inputString.length == 8 && inputString.all { it.isDigit() }
    }
}
