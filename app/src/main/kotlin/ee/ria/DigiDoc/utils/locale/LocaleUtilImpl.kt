@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.locale

import android.content.Context
import androidx.preference.PreferenceManager
import ee.ria.DigiDoc.common.Constant.KEY_LOCALE
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocaleUtilImpl
    @Inject
    constructor() : LocaleUtil {
        override fun getPreferredLanguage(context: Context?): String {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPreferences.getString(KEY_LOCALE, null) ?: "en"
        }

        override fun updateLocale(
            context: Context,
            locale: Locale,
        ): Context {
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            val configurationContext = context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return configurationContext
        }
    }
