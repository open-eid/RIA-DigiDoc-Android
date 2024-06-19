@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.text

import android.text.TextUtils
import org.apache.commons.lang3.StringUtils
import java.util.stream.Collectors

object TextUtil {
    fun removeEmptyStrings(strings: List<String>?): List<String> {
        val stringList = strings?.let { ArrayList(it) }
        if (stringList != null) {
            return stringList.stream()
                .filter { s: String? ->
                    !StringUtils.isBlank(
                        s,
                    )
                }
                .collect(Collectors.toList())
        }
        return emptyList()
    }

    fun splitTextAndJoin(
        text: String?,
        delimiter: String?,
        joinDelimiter: String?,
    ): String? {
        val nameComponents = TextUtils.split(text, delimiter)
        return joinDelimiter?.let { TextUtils.join(it, nameComponents) }
    }
}
