@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils

object Constant {
    // ee.ria.DigiDoc.utils.Route
    object Routes {
        const val HOME_SCREEN = "home_route"
        const val MENU_SCREEN = "menu_route"
        const val SIGNATURE_SCREEN = "signature_route"
        const val CRYPTO_SCREEN = "crypto_route"
        const val EID_SCREEN = "eid_route"
        const val MOBILE_ID_SCREEN = "mobile_id_route"
        const val SMART_ID_SCREEN = "smart_id_route"
        const val ID_CARD_SCREEN = "id_card_route"
        const val NFC_SCREEN = "nfc_route"
        const val SIGNING_SCREEN = "signing_route"
        const val SETTINGS_SCREEN = "settings_route"
        const val SETTINGS_RIGHTS_SCREEN = "settings_rights_route"
        const val SETTINGS_SIGNING_SCREEN = "settings_signing_route"
    }

    object Defaults {
        const val DEFAULT_UUID_VALUE = "00000000-0000-0000-0000-000000000000"
        const val DEFAULT_TSA_URL_VALUE = "https://eid-dd.ria.ee/ts"
    }
}
