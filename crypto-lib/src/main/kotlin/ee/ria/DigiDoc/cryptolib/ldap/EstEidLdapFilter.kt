@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.ldap

import java.util.Locale

class EstEidLdapFilter(query: String) : LdapFilter(query) {
    override fun filterString(): String {
        return if (isSerialNumberSearch()) {
            String.format(Locale.US, "(serialNumber= PNOEE-%s)", getQuery())
        } else {
            String.format(Locale.US, "(cn= %s)", getQuery())
        }
    }
}
