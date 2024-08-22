@file:Suppress("PackageName", "unused")

package ee.ria.DigiDoc.common

object Constant {
    object SignatureRequest {
        const val SIGNATURE_PROFILE_TS = "time-stamp"
        private const val ESTONIAN_PHONE_CODE = "372"
        const val PLUS_PREFIXED_ESTONIAN_PHONE_CODE = "+$ESTONIAN_PHONE_CODE"
        const val FIRST_NUMBER_IN_ESTONIAN_MOBILE_NUMBER = "5"

        const val MAXIMUM_INITIALIZATION_COUNT: Int = 5
        const val MAX_DISPLAY_MESSAGE_BYTES = 40
        const val MAX_DISPLAY_MESSAGE_LENGTH: Int = 200
        const val DEFAULT_LANGUAGE = "ENG"
        val SUPPORTED_LANGUAGES: Set<String> =
            setOf(DEFAULT_LANGUAGE, "EST", "RUS", "LIT")

        const val DIGEST_TYPE = "SHA256"

        const val RELYING_PARTY_NAME = "RIA DigiDoc"
        const val RELYING_PARTY_UUID = "00000000-0000-0000-0000-000000000000"
        const val DISPLAY_TEXT_FORMAT = "GSM-7"
        const val ALTERNATIVE_DISPLAY_TEXT_FORMAT = "UCS-2"
    }

    object SmartIdConstants {
        const val SID_BROADCAST_ACTION: String = "ee.ria.DigiDoc.android.smartid.SID_BROADCAST_ACTION"
        const val SID_BROADCAST_TYPE_KEY: String =
            "ee.ria.DigiDoc.android.smartid.SID_BROADCAST_TYPE_KEY"

        const val SIGNING_ROLE_DATA: String = "ee.ria.DigiDoc.android.smartid.SIGNING_ROLE_DATA"

        const val CREATE_SIGNATURE_REQUEST: String =
            "ee.ria.DigiDoc.android.smartid.CREATE_SIGNATURE_REQUEST"
        const val CERTIFICATE_CERT_BUNDLE: String =
            "ee.ria.DigiDoc.android.smartid.CERTIFICATE_CERT_BUNDLE"
        const val CREATE_SIGNATURE_DEVICE: String = "ee.ria.DigiDoc.android.smartid.SID_DEVICE"
        const val CREATE_SIGNATURE_CHALLENGE: String = "ee.ria.DigiDoc.android.smartid.SID_CHALLENGE"
        const val CREATE_SIGNATURE_STATUS: String =
            "ee.ria.DigiDoc.android.smartid.CREATE_SIGNATURE_STATUS"
        const val SERVICE_FAULT: String = "ee.ria.DigiDoc.android.smartid.SERVICE_FAULT"
        const val PROXY_SETTING: String = "ee.ria.DigiDoc.smartid.PROXY_SETTING"
        const val MANUAL_PROXY_HOST: String = "ee.ria.DigiDoc.smartid.HOST"
        const val MANUAL_PROXY_PORT: String = "ee.ria.DigiDoc.smartid.PORT"
        const val MANUAL_PROXY_USERNAME: String = "ee.ria.DigiDoc.smartid.USERNAME"
        const val MANUAL_PROXY_PASSWORD: String = "ee.ria.DigiDoc.smartid.PASSWORD"

        const val NOTIFICATION_CHANNEL: String = "SMART_ID_CHANNEL"
        const val NOTIFICATION_PERMISSION_CODE: Int = 1

        const val NOTIFICATION_NAME = "Smart-ID"
        const val PEM_BEGIN_CERT = "-----BEGIN CERTIFICATE-----"
        const val PEM_END_CERT = "-----END CERTIFICATE-----"
    }

    object MobileIdConstants {
        const val MID_BROADCAST_ACTION: String = "ee.ria.DigiDoc.androidmobileid.MID_BROADCAST_ACTION"
        const val MID_BROADCAST_TYPE_KEY: String =
            "ee.ria.DigiDoc.androidmobileid.MID_BROADCAST_TYPE_KEY"

        const val SIGNING_ROLE_DATA: String = "ee.ria.DigiDoc.androidmobileid.SIGNING_ROLE_DATA"

        const val CREATE_SIGNATURE_REQUEST: String =
            "ee.ria.DigiDoc.androidmobileid.CREATE_SIGNATURE_REQUEST"
        const val ACCESS_TOKEN_PASS: String = "ee.ria.DigiDoc.androidmobileid.ACCESS_TOKEN_PASS"
        const val ACCESS_TOKEN_PATH: String = "ee.ria.DigiDoc.androidmobileid.ACCESS_TOKEN_PATH"
        const val CERTIFICATE_CERT_BUNDLE: String =
            "ee.ria.DigiDoc.androidmobileid.CERTIFICATE_CERT_BUNDLE"
        const val CREATE_SIGNATURE_CHALLENGE: String = "ee.ria.DigiDoc.androidmobileid.MID_CHALLENGE"
        const val CREATE_SIGNATURE_STATUS: String =
            "ee.ria.DigiDoc.androidmobileid.CREATE_SIGNATURE_STATUS"
        const val SERVICE_FAULT: String = "ee.ria.DigiDoc.androidmobileid.SERVICE_FAULT"
        const val CONFIG_URL: String = "ee.ria.DigiDoc.androidmobileid.CONFIG_URL"
        const val PROXY_SETTING: String = "ee.ria.DigiDoc.androidmobileid.PROXY_SETTING"
        const val MANUAL_PROXY_HOST: String = "ee.ria.DigiDoc.androidmobileid.HOST"
        const val MANUAL_PROXY_PORT: String = "ee.ria.DigiDoc.androidmobileid.PORT"
        const val MANUAL_PROXY_USERNAME: String = "ee.ria.DigiDoc.androidmobileid.USERNAME"
        const val MANUAL_PROXY_PASSWORD: String = "ee.ria.DigiDoc.androidmobileid.PASSWORD"

        const val CERT_PEM_HEADER = "-----BEGIN CERTIFICATE-----"
        const val CERT_PEM_FOOTER = "-----END CERTIFICATE-----"
    }

    const val TSL_SEQUENCE_NUMBER_ELEMENT: String = "TSLSequenceNumber"
    const val KEY_LOCALE = "locale"
    private const val RESTRICTED_FILENAME_CHARACTERS_AS_STRING = "@%:^?[]\\'\"”’{}#&`\\\\~«»/´"
    private const val RTL_CHARACTERS_AS_STRING = "" + '\u200E' + '\u200F' + '\u202E' + '\u202A' + '\u202B'
    const val RESTRICTED_FILENAME_CHARACTERS_AND_RTL_CHARACTERS_AS_STRING =
        RESTRICTED_FILENAME_CHARACTERS_AS_STRING + RTL_CHARACTERS_AS_STRING
    const val DEFAULT_FILENAME = "newFile"
    const val ALLOWED_URL_CHARACTERS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_,.:/%;+=@?&!()"

    const val DIR_TSA_CERT = "tsa_cert"
    const val DIR_SIVA_CERT = "siva_cert"

    const val DATA_FILE_DIR = "%s-data-files"
    const val DIR_SIGNATURE_CONTAINERS = "signed_containers"
    const val DIR_EXTERNALLY_OPENED_FILES = "external_files"
    const val DEFAULT_CONTAINER_EXTENSION = "asice"
    const val CONTAINER_MIME_TYPE = "application/octet-stream"
    const val DEFAULT_MIME_TYPE = "text/plain"
    private val ASICS_CONTAINER_EXTENSIONS: Set<String> = setOf("asics", "scs")

    val CONTAINER_EXTENSIONS: Set<String> =
        setOf("asice", "sce", "adoc", "bdoc", "ddoc", "edoc")
            .plus(ASICS_CONTAINER_EXTENSIONS)

    val NON_LEGACY_CONTAINER_EXTENSIONS: Set<String> =
        setOf("asice", "sce", "bdoc")

    const val PDF_EXTENSION = "pdf"

    val NO_REMOVE_SIGNATURE_BUTTON_FILE_EXTENSIONS: Set<String> =
        setOf("adoc", "ddoc")
            .plus(ASICS_CONTAINER_EXTENSIONS)
            .plus(PDF_EXTENSION)

    val UNSIGNABLE_CONTAINER_EXTENSIONS: Set<String> =
        setOf("adoc", "ddoc").plus(ASICS_CONTAINER_EXTENSIONS)

    const val MAXIMUM_PERSONAL_CODE_LENGTH: Int = 11

    // Country code (3 numbers) + phone number (7 or more numbers)
    const val MINIMUM_PHONE_NUMBER_LENGTH = 10
    val ALLOWED_PHONE_NUMBER_COUNTRY_CODES = listOf("370", "372")
}
