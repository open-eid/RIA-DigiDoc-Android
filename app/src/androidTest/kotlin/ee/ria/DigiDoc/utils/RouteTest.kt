@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.CERTIFICATE_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ROOT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_RIGHTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_INPUT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNER_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteTest {
    @Test
    fun testRoutes() {
        assertEquals(HOME_SCREEN, Route.Home.route)
        assertEquals(SIGNATURE_SCREEN, Route.Signature.route)
        assertEquals(CRYPTO_SCREEN, Route.Crypto.route)
        assertEquals(EID_SCREEN, Route.EID.route)
        assertEquals(FILE_CHOOSING_SCREEN, Route.FileChoosing.route)
        assertEquals(SIGNING_SCREEN, Route.Signing.route)
        assertEquals(SIGNER_DETAIL_SCREEN, Route.SignerDetail.route)
        assertEquals(CERTIFICATE_DETAIL_SCREEN, Route.CertificateDetail.route)
        assertEquals(SETTINGS_SCREEN, Route.Settings.route)
        assertEquals(SETTINGS_RIGHTS_SCREEN, Route.SettingsRights.route)
        assertEquals(SETTINGS_SIGNING_SCREEN, Route.SettingsSigning.route)
        assertEquals(ROOT_SCREEN, Route.RootScreen.route)
        assertEquals(SIGNATURE_INPUT_SCREEN, Route.SignatureInputScreen.route)
        assertEquals(SIGNATURE_METHOD_SCREEN, Route.SignatureMethodScreen.route)
    }
}
