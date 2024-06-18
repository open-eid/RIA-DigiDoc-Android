@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.ACCESSIBILITY_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CERTIFICATE_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DIAGNOSTICS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ID_CARD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INFO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MENU_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MOBILE_ID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.NFC_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_RIGHTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNER_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SMART_ID_SCREEN

sealed class Route(val route: String) {
    object Home : Route(HOME_SCREEN)

    object Menu : Route(MENU_SCREEN)

    object Signature : Route(SIGNATURE_SCREEN)

    object Crypto : Route(CRYPTO_SCREEN)

    object EID : Route(EID_SCREEN)

    object MobileId : Route(MOBILE_ID_SCREEN)

    object SmartId : Route(SMART_ID_SCREEN)

    object IdCard : Route(ID_CARD_SCREEN)

    object NFC : Route(NFC_SCREEN)

    object FileChoosing : Route(FILE_CHOOSING_SCREEN)

    object Signing : Route(SIGNING_SCREEN)

    object Accessibility : Route(ACCESSIBILITY_SCREEN)

    object Info : Route(INFO_SCREEN)

    object Diagnostics : Route(DIAGNOSTICS_SCREEN)

    object SignerDetail : Route(SIGNER_DETAIL_SCREEN)

    object CertificateDetail : Route(CERTIFICATE_DETAIL_SCREEN)

    object Settings : Route(SETTINGS_SCREEN)

    object SettingsRights : Route(SETTINGS_RIGHTS_SCREEN)

    object SettingsSigning : Route(SETTINGS_SIGNING_SCREEN)
}
