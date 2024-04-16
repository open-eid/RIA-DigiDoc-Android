@file:Suppress("PackageName")

package ee.ria.DigiDoc.common

interface BuildVersionProvider {
    fun getSDKInt(): Int
}
