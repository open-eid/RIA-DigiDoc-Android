@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration

import java.util.Date

data class ConfigurationProvider(
    val metaInf: MetaInf,
    val configUrl: String,
    val sivaUrl: String,
    val tslUrl: String,
    val tslCerts: List<String>,
    val tsaUrl: String,
    val ldapPersonUrl: String,
    val ldapCorpUrl: String,
    val midRestUrl: String,
    val midSkRestUrl: String,
    val sidV2RestUrl: String,
    val sidV2SkRestUrl: String,
    val ocspUrls: Map<String, String>,
    val certBundle: List<String>,
    val configurationLastUpdateCheckDate: Date?,
    val configurationUpdateDate: Date?,
) {
    data class MetaInf(
        val url: String,
        val date: String,
        val serial: Int,
        val version: Int,
    )
}
