package ee.ria.DigiDoc.utils

import Constant.Routes.CRYPTO_SCREEN
import Constant.Routes.EID_SCREEN
import Constant.Routes.HOME_SCREEN
import Constant.Routes.MENU_SCREEN
import Constant.Routes.SIGNATURE_SCREEN

sealed class Route(val route : String) {
    object Home : Route(HOME_SCREEN)
    object Menu : Route(MENU_SCREEN)
    object Signature : Route(SIGNATURE_SCREEN)
    object Crypto : Route(CRYPTO_SCREEN)
    object eID : Route(EID_SCREEN)
}