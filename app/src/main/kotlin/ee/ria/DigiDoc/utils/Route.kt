@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.ACCESSIBILITY_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CERTIFICATE_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DIAGNOSTICS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INFO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INIT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MENU_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ROOT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_RIGHTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_INPUT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNER_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN

sealed class Route(val route: String) {
    data object Init : Route(INIT_SCREEN)

    data object Home : Route(HOME_SCREEN)

    data object Menu : Route(MENU_SCREEN)

    data object Signature : Route(SIGNATURE_SCREEN)

    data object Crypto : Route(CRYPTO_SCREEN)

    data object EID : Route(EID_SCREEN)

    data object FileChoosing : Route(FILE_CHOOSING_SCREEN)

    data object Signing : Route(SIGNING_SCREEN)

    data object Accessibility : Route(ACCESSIBILITY_SCREEN)

    data object Info : Route(INFO_SCREEN)

    data object Diagnostics : Route(DIAGNOSTICS_SCREEN)

    data object SignerDetail : Route(SIGNER_DETAIL_SCREEN)

    data object CertificateDetail : Route(CERTIFICATE_DETAIL_SCREEN)

    data object RecentDocuments : Route(RECENT_DOCUMENTS_SCREEN)

    data object Settings : Route(SETTINGS_SCREEN)

    data object SettingsRights : Route(SETTINGS_RIGHTS_SCREEN)

    data object SettingsSigning : Route(SETTINGS_SIGNING_SCREEN)

    data object RootScreen : Route(ROOT_SCREEN)

    data object SignatureInputScreen : Route(SIGNATURE_INPUT_SCREEN)

    data object SignatureMethodScreen : Route(SIGNATURE_METHOD_SCREEN)
}
