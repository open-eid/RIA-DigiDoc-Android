@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.extensions

import com.google.common.base.Splitter
import java.util.Locale

fun String.removeWhitespaces(): String = this.replace("\\s+".toRegex(), "")

fun String.removeDoubleSpaces(): String = this.replace("\\s+".toRegex(), " ")

fun String.formatHexString(): String {
    if (this.matches(Regex("([0-9A-Fa-f]{2} )+[0-9A-Fa-f]{2}"))) {
        return this.uppercase(Locale.getDefault())
    }

    val formattedHex = Splitter.fixedLength(2).split(this)
    return formattedHex
        .joinToString(separator = " ")
        .trim()
        .removeDoubleSpaces()
        .uppercase(Locale.getDefault())
}
