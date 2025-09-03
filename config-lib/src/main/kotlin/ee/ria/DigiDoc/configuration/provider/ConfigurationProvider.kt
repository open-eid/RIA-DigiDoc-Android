@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.provider

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ConfigurationProvider(
    @SerializedName("META-INF") val metaInf: MetaInf,
    @SerializedName("SIVA-URL") val sivaUrl: String,
    @SerializedName("CDOC2-CONF") val cdoc2Conf: Map<String, CDOC2Conf>,
    @SerializedName("CDOC2-USE-KEYSERVER") val cdoc2UseKeyServer: Boolean,
    @SerializedName("CDOC2-DEFAULT-KEYSERVER") val cdoc2DefaultKeyServer: String,
    @SerializedName("TSL-URL") val tslUrl: String,
    @SerializedName("TSL-CERTS") val tslCerts: List<String>,
    @SerializedName("TSA-URL") val tsaUrl: String,
    @SerializedName("OCSP-URL-ISSUER") val ocspUrls: Map<String, String>,
    @SerializedName("LDAP-PERSON-URL") val ldapPersonUrl: String,
    @SerializedName("LDAP-PERSON-URLS") val ldapPersonUrls: List<String>,
    @SerializedName("LDAP-CORP-URL") val ldapCorpUrl: String,
    @SerializedName("MID-PROXY-URL") val midRestUrl: String,
    @SerializedName("MID-SK-URL") val midSkRestUrl: String,
    @SerializedName("SIDV2-PROXY-URL") val sidV2RestUrl: String,
    @SerializedName("SIDV2-SK-URL") val sidV2SkRestUrl: String,
    @SerializedName("CERT-BUNDLE") val certBundle: List<String>,
    var configurationLastUpdateCheckDate: Date?,
    var configurationUpdateDate: Date?,
) {
    data class MetaInf(
        @SerializedName("URL") val url: String,
        @SerializedName("DATE") val date: String,
        @SerializedName("SERIAL") val serial: Int,
        @SerializedName("VER") val version: Int,
    )

    data class CDOC2Conf(
        @SerializedName("NAME") val name: String,
        @SerializedName("POST") val post: String,
        @SerializedName("FETCH") val fetch: String,
    )
}
