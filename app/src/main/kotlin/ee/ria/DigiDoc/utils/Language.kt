@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Languages.ENGLISH_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.ESTONIAN_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.RUSSIAN_LANGUAGE

sealed class Language(val locale: String) {
    object Estonian : Language(ESTONIAN_LANGUAGE)

    object English : Language(ENGLISH_LANGUAGE)

    object Russian : Language(RUSSIAN_LANGUAGE)
}
