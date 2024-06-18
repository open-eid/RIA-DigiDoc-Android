@file:Suppress("PackageName")

package ee.ria.DigiDoc.common.extensions

import android.text.TextUtils
import com.google.common.base.Splitter
import java.util.Locale

fun String.removeWhitespaces(): String = this.replace("\\s+".toRegex(), "")

fun String.formatHexString(): String =
    TextUtils
        .join(
            " ",
            Splitter
                .fixedLength(2)
                .split(this),
        ).trim { it <= ' ' }
        .uppercase(Locale.getDefault())
