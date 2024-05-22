@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.text

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
}
