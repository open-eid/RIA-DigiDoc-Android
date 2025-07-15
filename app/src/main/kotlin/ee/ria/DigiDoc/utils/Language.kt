@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Languages.ENGLISH_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.ESTONIAN_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.RUSSIAN_LANGUAGE

open class Language(val locale: String) {
    data object Estonian : Language(ESTONIAN_LANGUAGE)

    data object English : Language(ENGLISH_LANGUAGE)

    data object Russian : Language(RUSSIAN_LANGUAGE)
}
