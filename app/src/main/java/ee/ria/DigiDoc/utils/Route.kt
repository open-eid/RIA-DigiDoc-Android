@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MENU_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN

sealed class Route(val route: String) {
    object Home : Route(HOME_SCREEN)

    object Menu : Route(MENU_SCREEN)

    object Signature : Route(SIGNATURE_SCREEN)

    object Crypto : Route(CRYPTO_SCREEN)

    object EID : Route(EID_SCREEN)
}
