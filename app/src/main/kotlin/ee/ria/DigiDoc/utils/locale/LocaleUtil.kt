@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.locale

import android.content.Context
import java.util.Locale

interface LocaleUtil {
    fun getPreferredLanguage(context: Context?): String

    fun updateLocale(
        context: Context,
        locale: Locale,
    ): Context
}
