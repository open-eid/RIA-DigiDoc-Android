@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

import ee.ria.DigiDoc.utils.Constant.Routes.ACCESSIBILITY_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CERTIFICATE_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CONTAINER_NOTIFICATIONS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.CRYPTO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DECRYPT_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DECRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.DIAGNOSTICS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.EID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPTION_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPT_RECIPIENT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ENCRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.FILE_CHOOSING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.HOME_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INFO_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.INIT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_IDENTIFICATION_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_IDENTIFICATION_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_PIN_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.MYEID_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.PROXY_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECENT_DOCUMENTS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.RECIPIENT_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.ROOT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_LANGUAGE_CHOOSER_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SETTINGS_THEME_CHOOSER_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_INPUT_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_METHOD_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNATURE_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNER_DETAIL_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.SIGNING_SERVICES_SCREEN
import ee.ria.DigiDoc.utils.Constant.Routes.VALIDATION_SERVICES_SCREEN

open class Route(val route: String) {
    data object Init : Route(INIT_SCREEN)

    data object Home : Route(HOME_SCREEN)

    data object Signature : Route(SIGNATURE_SCREEN)

    data object Crypto : Route(CRYPTO_SCREEN)

    data object EID : Route(EID_SCREEN)

    data object FileChoosing : Route(FILE_CHOOSING_SCREEN)

    data object CryptoFileChoosing : Route(CRYPTO_FILE_CHOOSING_SCREEN)

    data object Signing : Route(SIGNING_SCREEN)

    data object Encrypt : Route(ENCRYPT_SCREEN)

    data object DecryptScreen : Route(DECRYPT_SCREEN)

    data object DecryptMethodScreen : Route(DECRYPT_METHOD_SCREEN)

    data object Accessibility : Route(ACCESSIBILITY_SCREEN)

    data object Info : Route(INFO_SCREEN)

    data object Diagnostics : Route(DIAGNOSTICS_SCREEN)

    data object SignerDetail : Route(SIGNER_DETAIL_SCREEN)

    data object CertificateDetail : Route(CERTIFICATE_DETAIL_SCREEN)

    data object RecipientDetail : Route(RECIPIENT_DETAIL_SCREEN)

    data object RecentDocuments : Route(RECENT_DOCUMENTS_SCREEN)

    data object RecentDocumentsFromEncrypt : Route(RECENT_DOCUMENTS_FROM_ENCRYPT_SCREEN)

    data object Settings : Route(SETTINGS_SCREEN)

    data object SettingsLanguageChooser : Route(SETTINGS_LANGUAGE_CHOOSER_SCREEN)

    data object SettingsThemeChooser : Route(SETTINGS_THEME_CHOOSER_SCREEN)

    data object RootScreen : Route(ROOT_SCREEN)

    data object SignatureInputScreen : Route(SIGNATURE_INPUT_SCREEN)

    data object SignatureMethodScreen : Route(SIGNATURE_METHOD_SCREEN)

    data object EncryptRecipientScreen : Route(ENCRYPT_RECIPIENT_SCREEN)

    data object SigningServicesScreen : Route(SIGNING_SERVICES_SCREEN)

    data object ValidationServicesScreen : Route(VALIDATION_SERVICES_SCREEN)

    data object EncryptionServicesScreen : Route(ENCRYPTION_SERVICES_SCREEN)

    data object ProxyServicesScreen : Route(PROXY_SERVICES_SCREEN)

    data object ContainerNotificationsScreen : Route(CONTAINER_NOTIFICATIONS_SCREEN)

    data object MyEidIdentificationScreen : Route(MYEID_IDENTIFICATION_SCREEN)

    data object MyEidIdentificationMethodScreen : Route(MYEID_IDENTIFICATION_METHOD_SCREEN)

    data object MyEidScreen : Route(MYEID_SCREEN)

    data object MyEidPinScreen : Route(MYEID_PIN_SCREEN)
}
