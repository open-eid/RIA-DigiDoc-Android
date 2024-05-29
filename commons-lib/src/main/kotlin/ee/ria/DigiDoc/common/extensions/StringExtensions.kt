@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.extensions

fun String.removeWhitespaces(): String {
    return this.replace("\\s+".toRegex(), "")
}
