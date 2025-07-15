@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Languages.ENGLISH_LANGUAGE
import ee.ria.DigiDoc.utils.Constant.Languages.ESTONIAN_LANGUAGE

sealed class Language(val locale: String) {
    data object Estonian : Language(ESTONIAN_LANGUAGE)

    data object English : Language(ENGLISH_LANGUAGE)
}
