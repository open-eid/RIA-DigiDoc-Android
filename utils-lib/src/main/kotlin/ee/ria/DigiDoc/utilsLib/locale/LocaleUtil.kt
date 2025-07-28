@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.locale

import android.os.Build
import java.util.Locale

object LocaleUtil {
    fun getLocale(lang: String): Locale =
        if (Build.VERSION.SDK_INT >= 36) {
            Locale.of(lang)
        } else {
            Locale(lang)
        }

    fun getLocale(
        lang: String,
        country: String,
    ): Locale =
        if (Build.VERSION.SDK_INT >= 36) {
            Locale.of(lang, country)
        } else {
            Locale(lang, country)
        }
}
