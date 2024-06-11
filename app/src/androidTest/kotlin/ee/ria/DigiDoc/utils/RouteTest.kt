@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ID_CARD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MENU_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MOBILE_ID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.NFC_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_RIGHTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SMART_ID_SCREEN
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {
    @Test
    fun testRoutes() {
        assertEquals(HOME_SCREEN, Route.Home.route)
        assertEquals(MENU_SCREEN, Route.Menu.route)
        assertEquals(SIGNATURE_SCREEN, Route.Signature.route)
        assertEquals(CRYPTO_SCREEN, Route.Crypto.route)
        assertEquals(EID_SCREEN, Route.EID.route)
        assertEquals(MOBILE_ID_SCREEN, Route.MobileId.route)
        assertEquals(SMART_ID_SCREEN, Route.SmartId.route)
        assertEquals(ID_CARD_SCREEN, Route.IdCard.route)
        assertEquals(NFC_SCREEN, Route.NFC.route)
        assertEquals(FILE_CHOOSING_SCREEN, Route.FileChoosing.route)
        assertEquals(SIGNING_SCREEN, Route.Signing.route)
        assertEquals(SETTINGS_SCREEN, Route.Settings.route)
        assertEquals(SETTINGS_RIGHTS_SCREEN, Route.SettingsRights.route)
        assertEquals(SETTINGS_SIGNING_SCREEN, Route.SettingsSigning.route)
    }
}
