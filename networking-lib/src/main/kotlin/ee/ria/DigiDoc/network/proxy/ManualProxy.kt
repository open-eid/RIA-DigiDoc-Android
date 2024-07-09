@file:Suppress("PackageName")

package ee.ria.DigiDoc.network.proxy

data class ManualProxy(var host: String, var port: Int, var username: String, var password: String)
