@file:Suppress("PackageName", "MaxLineLength")

package ee.ria.DigiDoc.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.common.collect.ImmutableMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant.ALLOWED_PHONE_NUMBER_COUNTRY_CODES
import ee.ria.DigiDoc.common.Constant.MAXIMUM_PERSONAL_CODE_LENGTH
import ee.ria.DigiDoc.common.Constant.MINIMUM_PHONE_NUMBER_LENGTH
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.domain.preferences.DataStore
import ee.ria.DigiDoc.domain.repository.FileOpeningRepository
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.mobileId.MobileSignService
import ee.ria.DigiDoc.mobileId.utils.MobileCreateSignatureRequestHelper
import ee.ria.DigiDoc.network.mid.dto.request.MobileCreateSignatureRequest
import ee.ria.DigiDoc.network.mid.dto.response.MobileCreateSignatureProcessStatus
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import ee.ria.DigiDoc.utilsLib.validator.PersonalCodeValidator.validatePersonalCode
import ee.ria.libdigidocpp.Conf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects
import javax.inject.Inject

@HiltViewModel
class MobileIdViewModel
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val dataStore: DataStore,
        private val contentResolver: ContentResolver,
        private val mobileSignService: MobileSignService,
        private val fileOpeningRepository: FileOpeningRepository,
    ) : ViewModel() {
        private val _signedContainer = MutableLiveData<SignedContainer?>(null)
        val signedContainer: LiveData<SignedContainer?> = _signedContainer
        private val _signature = MutableLiveData<String?>(null)
        val signature: LiveData<String?> = _signature
        private val _errorState = MutableLiveData<String?>(null)
        val errorState: LiveData<String?> = _errorState
        private val _challenge = MutableLiveData<String?>(null)
        val challenge: LiveData<String?> = _challenge
        private val _status = MutableLiveData<MobileCreateSignatureProcessStatus?>(null)
        val status: LiveData<MobileCreateSignatureProcessStatus?> = _status

        private val messages: ImmutableMap<MobileCreateSignatureProcessStatus, Int> =
            ImmutableMap.builder<MobileCreateSignatureProcessStatus, Int>()
                .put(
                    MobileCreateSignatureProcessStatus.OK,
                    R.string.signature_update_mobile_id_status_request_ok,
                )
                .put(
                    MobileCreateSignatureProcessStatus.TIMEOUT,
                    R.string.signature_update_mobile_id_status_expired_transaction,
                )
                .put(
                    MobileCreateSignatureProcessStatus.NOT_MID_CLIENT,
                    R.string.signature_update_mobile_id_status_expired_transaction,
                )
                .put(
                    MobileCreateSignatureProcessStatus.USER_CANCELLED,
                    R.string.signature_update_mobile_id_status_user_cancel,
                )
                .put(
                    MobileCreateSignatureProcessStatus.SIGNATURE_HASH_MISMATCH,
                    R.string.signature_update_mobile_id_status_signature_hash_mismatch,
                )
                .put(
                    MobileCreateSignatureProcessStatus.DELIVERY_ERROR,
                    R.string.signature_update_mobile_id_status_delivery_error,
                )
                .put(
                    MobileCreateSignatureProcessStatus.PHONE_ABSENT,
                    R.string.signature_update_mobile_id_status_phone_absent,
                )
                .put(
                    MobileCreateSignatureProcessStatus.SIM_ERROR,
                    R.string.signature_update_mobile_id_status_sim_error,
                )
                .put(
                    MobileCreateSignatureProcessStatus.TOO_MANY_REQUESTS,
                    R.string.signature_update_signature_error_message_too_many_requests,
                )
                .put(
                    MobileCreateSignatureProcessStatus.EXCEEDED_UNSUCCESSFUL_REQUESTS,
                    R.string.signature_update_signature_error_message_exceeded_unsuccessful_requests,
                )
                .put(
                    MobileCreateSignatureProcessStatus.INVALID_ACCESS_RIGHTS,
                    R.string.signature_update_mobile_id_error_message_access_rights,
                )
                .put(
                    MobileCreateSignatureProcessStatus.OCSP_INVALID_TIME_SLOT,
                    R.string.signature_update_signature_error_message_invalid_time_slot,
                )
                .put(
                    MobileCreateSignatureProcessStatus.CERTIFICATE_REVOKED,
                    R.string.signature_update_signature_error_message_certificate_revoked,
                )
                .put(
                    MobileCreateSignatureProcessStatus.GENERAL_ERROR,
                    R.string.signature_update_mobile_id_error_general_client,
                )
                .put(MobileCreateSignatureProcessStatus.NO_RESPONSE, R.string.no_internet_connection)
                .put(
                    MobileCreateSignatureProcessStatus.INVALID_COUNTRY_CODE,
                    R.string.signature_update_mobile_id_status_no_country_code,
                )
                .put(MobileCreateSignatureProcessStatus.INVALID_SSL_HANDSHAKE, R.string.invalid_ssl_handshake)
                .put(
                    MobileCreateSignatureProcessStatus.TECHNICAL_ERROR,
                    R.string.signature_update_mobile_id_error_technical_error,
                )
                .build()

        fun resetSignedContainer() {
            _signedContainer.postValue(null)
        }

        fun cancelMobileIdWorkRequest() {
            mobileSignService.setCancelled(true)
        }

        suspend fun performMobileIdWorkRequest(
            container: SignedContainer?,
            personalCode: String,
            phoneNumber: String,
            @Suppress("UNUSED_PARAMETER") configurationProvider: ConfigurationProvider?,
            roleData: RoleData?,
        ) {
            val uuid = dataStore.getSettingsUUID()
            val proxySetting: ProxySetting = dataStore.getProxySetting()
            val manualProxySettings: ManualProxy = dataStore.getManualProxySettings(context)

            val displayMessage: String =
                context
                    .getString(R.string.signature_update_mobile_id_display_message)
            val request: MobileCreateSignatureRequest =
                MobileCreateSignatureRequestHelper
                    .create(
                        container,
                        uuid,
                        // TODO: configurationProvider?.midRestUrl,
                        "https://eid-dd.ria.ee/mid",
                        // TODO: configurationProvider?.midSkRestUrl,
                        "https://mid.sk.ee/mid-api",
                        Locale.getDefault(),
                        personalCode,
                        phoneNumber,
                        displayMessage,
                    )
            val certBundle =
                ArrayList(
                    listOf(
                        "MIIE+DCCBFmgAwIBAgIQMLOwlXoR0oFbj52nmRsnezAKBggqhkjOPQQDBDBaMQswCQYDVQQG" +
                            "EwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0" +
                            "NzAxMzEVMBMGA1UEAwwMRUUtR292Q0EyMDE4MB4XDTE4MDkwNTA5MTEwM1oXDTMzMDkwNTA5" +
                            "MTEwM1owWjELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNvbHV0aW9ucyBBUzEXMBUG" +
                            "A1UEYQwOTlRSRUUtMTA3NDcwMTMxFTATBgNVBAMMDEVFLUdvdkNBMjAxODCBmzAQBgcqhkjO" +
                            "PQIBBgUrgQQAIwOBhgAEAMcb/dmAcVo/b2azEPS6CfW7fEA2KuHKC53D7ShVNvLz4QUjCdTX" +
                            "jds/4u99jUoYEQecluVVzMlgEJR1nkN2eOrLAZYxPjwG5HiI1iZEyW9QKVdeEgyvhzWWTNHG" +
                            "jV3HdZRv7L9o4533PtJAyqJq9OTs6mjsqwFXjH49bfZ6CGmzUJsHo4ICvDCCArgwEgYDVR0T" +
                            "AQH/BAgwBgEB/wIBATAOBgNVHQ8BAf8EBAMCAQYwNAYDVR0lAQH/BCowKAYIKwYBBQUHAwkG" +
                            "CCsGAQUFBwMCBggrBgEFBQcDBAYIKwYBBQUHAwEwHQYDVR0OBBYEFH4pVuc0knhOd+FvLjMq" +
                            "mHHB/TSfMB8GA1UdIwQYMBaAFH4pVuc0knhOd+FvLjMqmHHB/TSfMIICAAYDVR0gBIIB9zCC" +
                            "AfMwCAYGBACPegECMAkGBwQAi+xAAQIwMgYLKwYBBAGDkSEBAQEwIzAhBggrBgEFBQcCARYV" +
                            "aHR0cHM6Ly93d3cuc2suZWUvQ1BTMA0GCysGAQQBg5EhAQECMA0GCysGAQQBg5F/AQEBMA0G" +
                            "CysGAQQBg5EhAQEFMA0GCysGAQQBg5EhAQEGMA0GCysGAQQBg5EhAQEHMA0GCysGAQQBg5Eh" +
                            "AQEDMA0GCysGAQQBg5EhAQEEMA0GCysGAQQBg5EhAQEIMA0GCysGAQQBg5EhAQEJMA0GCysG" +
                            "AQQBg5EhAQEKMA0GCysGAQQBg5EhAQELMA0GCysGAQQBg5EhAQEMMA0GCysGAQQBg5EhAQEN" +
                            "MA0GCysGAQQBg5EhAQEOMA0GCysGAQQBg5EhAQEPMA0GCysGAQQBg5EhAQEQMA0GCysGAQQB" +
                            "g5EhAQERMA0GCysGAQQBg5EhAQESMA0GCysGAQQBg5EhAQETMA0GCysGAQQBg5EhAQEUMA0G" +
                            "CysGAQQBg5F/AQECMA0GCysGAQQBg5F/AQEDMA0GCysGAQQBg5F/AQEEMA0GCysGAQQBg5F/" +
                            "AQEFMA0GCysGAQQBg5F/AQEGMDEGCisGAQQBg5EhCgEwIzAhBggrBgEFBQcCARYVaHR0cHM6" +
                            "Ly93d3cuc2suZWUvQ1BTMBgGCCsGAQUFBwEDBAwwCjAIBgYEAI5GAQEwCgYIKoZIzj0EAwQD" +
                            "gYwAMIGIAkIBk698EqetY9Tt6HwO50CfzdIIjKmlfCI34xKdU7J+wz1tNVu2tHJwEhdsH0e9" +
                            "2i969sRDp1RNPlVh4XFJzI3oQFQCQgGVxmcuVnsy7NUscDZ0erwovmbFOsNxELCANxNSWx5x" +
                            "MqzEIhV846opxu10UFDIBBPzkbBenL4h+g/WU7lG78fIhA==",
                        "MIIFVzCCBLigAwIBAgIQdUf6rBR0S4tbo2bU/mZV7TAKBggqhkjOPQQDBDBaMQswCQYDVQQG" +
                            "EwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0" +
                            "NzAxMzEVMBMGA1UEAwwMRUUtR292Q0EyMDE4MB4XDTE4MDkyMDA5MjIyOFoXDTMzMDkwNTA5" +
                            "MTEwM1owWDELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNvbHV0aW9ucyBBUzEXMBUG" +
                            "A1UEYQwOTlRSRUUtMTA3NDcwMTMxEzARBgNVBAMMCkVTVEVJRDIwMTgwgZswEAYHKoZIzj0C" +
                            "AQYFK4EEACMDgYYABAHHOBlv7UrRPYP1yHhOb7RA/YBDbtgynSVMqYdxnFrKHUXh6tFkghvH" +
                            "uA1k2DSom1hE5kqhB5VspDembwWDJBOQWQGOI/0t3EtccLYjeM7F9xOPdzUbZaIbpNRHpQgV" +
                            "BpFX0xpLTgW27MpIMhU8DHBWFpeAaNX3eUpD4gC5cvhsK0RFEqOCAx0wggMZMB8GA1UdIwQY" +
                            "MBaAFH4pVuc0knhOd+FvLjMqmHHB/TSfMB0GA1UdDgQWBBTZrHDbX36+lPig5L5HotA0rZoq" +
                            "EjAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB/wIBADCCAc0GA1UdIASCAcQwggHA" +
                            "MAgGBgQAj3oBAjAJBgcEAIvsQAECMDIGCysGAQQBg5EhAQEBMCMwIQYIKwYBBQUHAgEWFWh0" +
                            "dHBzOi8vd3d3LnNrLmVlL0NQUzANBgsrBgEEAYORIQEBAjANBgsrBgEEAYORfwEBATANBgsr" +
                            "BgEEAYORIQEBBTANBgsrBgEEAYORIQEBBjANBgsrBgEEAYORIQEBBzANBgsrBgEEAYORIQEB" +
                            "AzANBgsrBgEEAYORIQEBBDANBgsrBgEEAYORIQEBCDANBgsrBgEEAYORIQEBCTANBgsrBgEE" +
                            "AYORIQEBCjANBgsrBgEEAYORIQEBCzANBgsrBgEEAYORIQEBDDANBgsrBgEEAYORIQEBDTAN" +
                            "BgsrBgEEAYORIQEBDjANBgsrBgEEAYORIQEBDzANBgsrBgEEAYORIQEBEDANBgsrBgEEAYOR" +
                            "IQEBETANBgsrBgEEAYORIQEBEjANBgsrBgEEAYORIQEBEzANBgsrBgEEAYORIQEBFDANBgsr" +
                            "BgEEAYORfwEBAjANBgsrBgEEAYORfwEBAzANBgsrBgEEAYORfwEBBDANBgsrBgEEAYORfwEB" +
                            "BTANBgsrBgEEAYORfwEBBjAqBgNVHSUBAf8EIDAeBggrBgEFBQcDCQYIKwYBBQUHAwIGCCsG" +
                            "AQUFBwMEMGoGCCsGAQUFBwEBBF4wXDApBggrBgEFBQcwAYYdaHR0cDovL2FpYS5zay5lZS9l" +
                            "ZS1nb3ZjYTIwMTgwLwYIKwYBBQUHMAKGI2h0dHA6Ly9jLnNrLmVlL0VFLUdvdkNBMjAxOC5k" +
                            "ZXIuY3J0MBgGCCsGAQUFBwEDBAwwCjAIBgYEAI5GAQEwMAYDVR0fBCkwJzAloCOgIYYfaHR0" +
                            "cDovL2Muc2suZWUvRUUtR292Q0EyMDE4LmNybDAKBggqhkjOPQQDBAOBjAAwgYgCQgDeuUY4" +
                            "HczUbFKS002HZ88gclgYdztHqglENyTMtXE6dMBRnCbgUmhBCAA0mJSHbyFJ8W9ikLiSyurm" +
                            "kJM0hDE9KgJCASOqA405Ia5nKjTJPNsHQlMi7KZsIcTHOoBccx+54N8ZX1MgBozJmT59rZY/" +
                            "2/OeE163BAwD0UdUQAnMPP6+W3Vd",
                        "MIIEAzCCAuugAwIBAgIQVID5oHPtPwBMyonY43HmSjANBgkqhkiG9w0BAQUFADB1MQswCQYD" +
                            "VQQGEwJFRTEiMCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1czEoMCYGA1UEAwwf" +
                            "RUUgQ2VydGlmaWNhdGlvbiBDZW50cmUgUm9vdCBDQTEYMBYGCSqGSIb3DQEJARYJcGtpQHNr" +
                            "LmVlMCIYDzIwMTAxMDMwMTAxMDMwWhgPMjAzMDEyMTcyMzU5NTlaMHUxCzAJBgNVBAYTAkVF" +
                            "MSIwIAYDVQQKDBlBUyBTZXJ0aWZpdHNlZXJpbWlza2Vza3VzMSgwJgYDVQQDDB9FRSBDZXJ0" +
                            "aWZpY2F0aW9uIENlbnRyZSBSb290IENBMRgwFgYJKoZIhvcNAQkBFglwa2lAc2suZWUwggEi" +
                            "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDIIMDs4MVLqwd4lfNE7vsLDP90jmG7sWLq" +
                            "I9iroWUyeuuOF0+W2Ap7kaJjbMeMTC55v6kF/GlclY1i+blw7cNRfdCT5mzrMEvhvH2/UpvO" +
                            "bntl8jixwKIy72KyaOBhU8E2lf/slLo2rpwcpzIP5Xy0xm90/XsY6KxX7QYgSzIwWFv9zajm" +
                            "ofxwvI6Sc9uXp3whrj3B9UiHbCe9nyV0gVWw93X2PaRka9ZP585ArQ/dMtO8ihJTmMmJ+xAd" +
                            "TX7Nfh9WDSFwhfYggx/2uh8Ej+p3iDXE/+pOoYtNP2MbRMNE1CV2yreN1x5KZmTNXMWcg+HC" +
                            "CIia7E6j8T4cLNlsHaFLAgMBAAGjgYowgYcwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8E" +
                            "BAMCAQYwHQYDVR0OBBYEFBLyWj7qVhy/zQas8fElyalL1BSZMEUGA1UdJQQ+MDwGCCsGAQUF" +
                            "BwMCBggrBgEFBQcDAQYIKwYBBQUHAwMGCCsGAQUFBwMEBggrBgEFBQcDCAYIKwYBBQUHAwkw" +
                            "DQYJKoZIhvcNAQEFBQADggEBAHv25MANqhlHt01Xo/6tu7Fq1Q+e2+RjxY6hUFaTlrg4wCQi" +
                            "ZrxTFGGVv9DHKpY5P30osxBAIWrEr7BSdxjhlthWXePdNl4dp1BUoMUq5KqMlIpPnTX/dqQG" +
                            "E5Gion0ARD9V04I8GtVbvFZMIi5GQ4okQC3zErg7cBqklrkar4dBGmoYDQZPxz5uuSlNDUmJ" +
                            "EYcyW+ZLBMjkXOZ0c5RdFpgTlf7727FE5TpwrDdr5rMzcijJs1eg9gIWiAYLtqZLICjU3j2L" +
                            "rTcFU3T+bsy8QxdxXvnFzBqpYe73dgzzcvRyrc9yAjYHR8/vGVCJYMzpJJUPwssd8m92kMfM" +
                            "dcGWxZ0=",
                        "MIIGcDCCBVigAwIBAgIQRUgJC4ec7yFWcqzT3mwbWzANBgkqhkiG9w0BAQwFADB1MQswCQYD" +
                            "VQQGEwJFRTEiMCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1czEoMCYGA1UEAwwf" +
                            "RUUgQ2VydGlmaWNhdGlvbiBDZW50cmUgUm9vdCBDQTEYMBYGCSqGSIb3DQEJARYJcGtpQHNr" +
                            "LmVlMCAXDTE1MTIxNzEyMzg0M1oYDzIwMzAxMjE3MjM1OTU5WjBjMQswCQYDVQQGEwJFRTEi" +
                            "MCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1czEXMBUGA1UEYQwOTlRSRUUtMTA3" +
                            "NDcwMTMxFzAVBgNVBAMMDkVTVEVJRC1TSyAyMDE1MIICIjANBgkqhkiG9w0BAQEFAAOCAg8A" +
                            "MIICCgKCAgEA0oH61NDxbdW9k8nLA1qGaL4B7vydod2Ewp/STBZB3wEtIJCLdkpEsS8pXfFi" +
                            "RqwDVsgGGbu+Q99trlb5LI7yi7rIkRov5NftBdSNPSU5rAhYPQhvZZQgOwRaHa5Ey+BaLJHm" +
                            "LqYQS9hQvQsCYyws+xVvNFUpK0pGD64iycqdMuBl/nWq3fLuZppwBh0VFltm4nhr/1S0R9TR" +
                            "JpqFUGbGr4OK/DwebQ5PjhdS40gCUNwmC7fPQ4vIH+x+TCk2aG+u3MoAz0IrpVWqiwzG/vxr" +
                            "euPPAkgXeFCeYf6fXLsGz4WivsZFbph2pMjELu6sltlBXfAG3fGv43t91VXicyzR/eT5dsB+" +
                            "zFsW1sHV+1ONPr+qzgDxCH2cmuqoZNfIIq+buob3eA8ee+XpJKJQr+1qGrmhggjvAhc7m6cU" +
                            "4x/QfxwRYhIVNhJf+sKVThkQhbJ9XxuKk3c18wymwL1mpDD0PIGJqlssMeiuJ4IzagFbgESG" +
                            "NDUd4icm0hQT8CmQeUm1GbWeBYseqPhMQX97QFBLXJLVy2SCyoAz7Bq1qA43++EcibN+yBc1" +
                            "nQs2Zoq8ck9MK0bCxDMeUkQUz6VeQGp69ImOQrsw46qTz0mtdQrMSbnkXCuLan5dPm284J9H" +
                            "maqiYi6j6KLcZ2NkUnDQFesBVlMEm+fHa2iR6lnAFYZ06UECAwEAAaOCAgowggIGMB8GA1Ud" +
                            "IwQYMBaAFBLyWj7qVhy/zQas8fElyalL1BSZMB0GA1UdDgQWBBSzq4i8mdVipIUqCM20HXI7" +
                            "g3JHUTAOBgNVHQ8BAf8EBAMCAQYwdwYDVR0gBHAwbjAIBgYEAI96AQIwCQYHBACL7EABAjAw" +
                            "BgkrBgEEAc4fAQEwIzAhBggrBgEFBQcCARYVaHR0cHM6Ly93d3cuc2suZWUvQ1BTMAsGCSsG" +
                            "AQQBzh8BAjALBgkrBgEEAc4fAQMwCwYJKwYBBAHOHwEEMBIGA1UdEwEB/wQIMAYBAf8CAQAw" +
                            "QQYDVR0eBDowOKE2MASCAiIiMAqHCAAAAAAAAAAAMCKHIAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                            "AAAAAAAAAAAAAAAAMCcGA1UdJQQgMB4GCCsGAQUFBwMJBggrBgEFBQcDAgYIKwYBBQUHAwQw" +
                            "fAYIKwYBBQUHAQEEcDBuMCAGCCsGAQUFBzABhhRodHRwOi8vb2NzcC5zay5lZS9DQTBKBggr" +
                            "BgEFBQcwAoY+aHR0cDovL3d3dy5zay5lZS9jZXJ0cy9FRV9DZXJ0aWZpY2F0aW9uX0NlbnRy" +
                            "ZV9Sb290X0NBLmRlci5jcnQwPQYDVR0fBDYwNDAyoDCgLoYsaHR0cDovL3d3dy5zay5lZS9y" +
                            "ZXBvc2l0b3J5L2NybHMvZWVjY3JjYS5jcmwwDQYJKoZIhvcNAQEMBQADggEBAHRWDGI3P00r" +
                            "2sOnlvLHKk9eE7X93eT+4e5TeaQsOpE5zQRUTtshxN8Bnx2ToQ9rgi18q+MwXm2f0mrGakYY" +
                            "G0bix7ZgDQvCMD/kuRYmwLGdfsTXwh8KuL6uSHF+U/ZTss6qG7mxCHG9YvebkN5Yj/rYRvZ9" +
                            "/uJ9rieByxw4wo7b19p22PXkAkXP5y3+qK/Oet98lqwI97kJhiS2zxFYRk+dXbazmoVHnozY" +
                            "KmsZaSUvoYNNH19tpS7BLdsgi9KpbvQLb5ywIMq9ut3+b2Xvzq8yzmHMFtLIJ6Afu1jJpqD8" +
                            "2BUAFcvi5vhnP8M7b974R18WCOpgNQvXDI+2/8ZINeU=",
                        "MIIGgTCCBWmgAwIBAgIQXlM7EyVgNCtYSVcwizB43DANBgkqhkiG9w0BAQwFADB1MQswCQYD" +
                            "VQQGEwJFRTEiMCAGA1UECgwZQVMgU2VydGlmaXRzZWVyaW1pc2tlc2t1czEoMCYGA1UEAwwf" +
                            "RUUgQ2VydGlmaWNhdGlvbiBDZW50cmUgUm9vdCBDQTEYMBYGCSqGSIb3DQEJARYJcGtpQHNr" +
                            "LmVlMCAXDTE2MTIwODEyNTA1NloYDzIwMzAxMjE3MjM1OTU5WjCBhjELMAkGA1UEBhMCRUUx" +
                            "IjAgBgNVBAoMGUFTIFNlcnRpZml0c2VlcmltaXNrZXNrdXMxITAfBgNVBAsMGFNlcnRpZml0" +
                            "c2VlcmltaXN0ZWVudXNlZDEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxFzAVBgNVBAMMDktM" +
                            "QVNTMy1TSyAyMDE2MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAlkOLeKQPKK1U" +
                            "8VK7z2Dzt2SX2KblGqrBmOXfzlImzXHxGVopSeji2/4MdR5Ok6NJqXxanbyufXXRTeuE5nQ8" +
                            "Olzr5+9U21DPmVUADFNWnDLy6NWyqE3CvrYp7tVOHbfTb9Mf3ECvQNt8YM0HGwdSfc8kGXuX" +
                            "8d4oixxeG4AD+wrj1+LJ0ioaQFlS6Tbcwq3xEO0WVv1hMrJOoMmPpaqrvRLcoikpmjnPm/Gt" +
                            "fx64FcyXiMmNxFDnROVMgr1OQKbxAdlX3Iu32fcXjXesCTcACLlNRMi5Sb1wowjGEpqL2H53" +
                            "+JDIrdE7hM0uUqX4aaT5etaUh0o2hxOBHg3m6WRAZmBPqO1BqIBN6PRMWYgab7BBtJMUKXE+" +
                            "FUaNy9Lb8jraX85t3IwN/hbbMx3wUAqZvoQVIaJu2tsP8eTGJUd6jES9q9rH788LNf2w9o16" +
                            "blr1cM0AkzfbPf1ktClERcQd+iEhAPluSjKwMHIehRQGwGGuo7db4QXKhXDXPGK5YRw6Q56m" +
                            "p+BpSqJJqpdlQCieEXbHm9sHsoP5yaQygZI8nJpd0nlpdcTq91aEjrWuuksQTNDG9++8NSAq" +
                            "l2G/BVCesWx/zR0KtcWecMPUVfe7qEEFurWsewpLgZFsk5RLtNGwyTEgHHBfJqAJC8l2VMfb" +
                            "bsEW+tcjdMqb6BHgT6hNCx8CAwEAAaOCAfcwggHzMBIGA1UdEwEB/wQIMAYBAf8CAQAwDgYD" +
                            "VR0PAQH/BAQDAgHGMIHTBgNVHSAEgcswgcgwgYQGCSsGAQQBzh8HAzB3MCEGCCsGAQUFBwIB" +
                            "FhVodHRwczovL3d3dy5zay5lZS9jcHMwUgYIKwYBBQUHAgIwRh5EAEEAcwB1AHQAdQBzAGUA" +
                            "IABzAGUAcgB0AGkAZgBpAGsAYQBhAHQALgAgAEMAbwByAHAAbwByAGEAdABlACAASQBEAC4w" +
                            "CAYGZ4EMAQICMAsGCSsGAQQBzh8HAjAIBgYEAI96AQEwCQYHBACL7EABATAIBgYEAI96AQcw" +
                            "CQYHBACL7EABAzAdBgNVHQ4EFgQUrl5Y9fLy2cGO2e9OB9t1ylDihwAwHwYDVR0jBBgwFoAU" +
                            "EvJaPupWHL/NBqzx8SXJqUvUFJkweAYIKwYBBQUHAQEEbDBqMCAGCCsGAQUFBzABhhRodHRw" +
                            "Oi8vb2NzcC5zay5lZS9DQTBGBggrBgEFBQcwAoY6aHR0cDovL3NrLmVlL2NlcnRzL0VFX0Nl" +
                            "cnRpZmljYXRpb25fQ2VudHJlX1Jvb3RfQ0EuZGVyLmNydDA9BgNVHR8ENjA0MDKgMKAuhixo" +
                            "dHRwOi8vd3d3LnNrLmVlL3JlcG9zaXRvcnkvY3Jscy9lZWNjcmNhLmNybDANBgkqhkiG9w0B" +
                            "AQwFAAOCAQEAah2vGqi+Pe5+CPtarh0vCQWOm233nl5Y9qL+JqG5PccowQ41kzf4qknmP6BH" +
                            "fisYGQsRc75K07A+/BdlFrLMbP3fFsuTi7+HAmAjXYEq35G49GAQg52+HvZiBe+RtbR8yOOa" +
                            "r5fAKnzS1yNy9M1z7g7yMcEouk3TUebe2aanMvzabc7qgV3HGDfZkzhL9PlcjmFl0LQEflef" +
                            "/6sdMhy6C0HiditdLSUZYfSySJpb6lvJBGdN4Vrbo2fNtL3qIc+vX1Jvh/qLFIFmFXuC6lIj" +
                            "FJFtpIbCIQMtHoMXdI1A5JzzkmrTLPTSYDAQXXn9RPnzsRz2GnlYRV4xGayDGbUyow==",
                        "MIIFuzCCBKOgAwIBAgIIB7tYMxdqiRQwDQYJKoZIhvcNAQELBQAweTEtMCsGA1UEAwwkRGV2" +
                            "ZWxvcGVyIElEIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MSYwJAYDVQQLDB1BcHBsZSBDZXJ0" +
                            "aWZpY2F0aW9uIEF1dGhvcml0eTETMBEGA1UECgwKQXBwbGUgSW5jLjELMAkGA1UEBhMCVVMw" +
                            "HhcNMjIwODAzMDc0MjEzWhcNMjcwMjAxMjIxMjE1WjCBqTEaMBgGCgmSJomT8ixkAQEMCkVU" +
                            "ODQ3UUpWOUYxRjBEBgNVBAMMPURldmVsb3BlciBJRCBJbnN0YWxsZXI6IFJpaWdpIEluZm9z" +
                            "w7xzdGVlbWkgQW1ldCAoRVQ4NDdRSlY5RikxEzARBgNVBAsMCkVUODQ3UUpWOUYxITAfBgNV" +
                            "BAoMGFJpaWdpIEluZm9zw7xzdGVlbWkgQW1ldDELMAkGA1UEBhMCVVMwggEiMA0GCSqGSIb3" +
                            "DQEBAQUAA4IBDwAwggEKAoIBAQDb/Y5hBrdR/IgotXMxrSILtD5P3n6PlMsVPH0xlnIU8x9q" +
                            "RT7TPZf50FnN55WYXz12vSST1Z7hOGC6Lii4Rf9hxa96bKIeuoI+L+86fh6rV2TqsXRB2U7o" +
                            "tcvdZR4OW39wvLmDGkPaDzuQGoKCsaMIjQ5lXBc7CfNP32sKqtBH8MedmJoXeolYhN65STTI" +
                            "AY2B71XSWrwHH2I3pHFN7Kr2ggGZA6MLCzm/yH2xryhqbqOjlxXvIjD7gmho7kCWmvZzEF19" +
                            "2N3n2RbxJnPRCvoK6WB9HsFs+/6zuZTV3Io0pJA3n5Ax5iFdrCw/L22HvSkB3sOMZRa4JYBy" +
                            "kQ9BqWF3AgMBAAGjggIUMIICEDAMBgNVHRMBAf8EAjAAMB8GA1UdIwQYMBaAFFcX7aLP3HyY" +
                            "oRDg/L6HLSzy4xdUMEAGCCsGAQUFBwEBBDQwMjAwBggrBgEFBQcwAYYkaHR0cDovL29jc3Au" +
                            "YXBwbGUuY29tL29jc3AwMy1kZXZpZDA3MIIBHQYDVR0gBIIBFDCCARAwggEMBgkqhkiG92Nk" +
                            "BQEwgf4wgcMGCCsGAQUFBwICMIG2DIGzUmVsaWFuY2Ugb24gdGhpcyBjZXJ0aWZpY2F0ZSBi" +
                            "eSBhbnkgcGFydHkgYXNzdW1lcyBhY2NlcHRhbmNlIG9mIHRoZSB0aGVuIGFwcGxpY2FibGUg" +
                            "c3RhbmRhcmQgdGVybXMgYW5kIGNvbmRpdGlvbnMgb2YgdXNlLCBjZXJ0aWZpY2F0ZSBwb2xp" +
                            "Y3kgYW5kIGNlcnRpZmljYXRpb24gcHJhY3RpY2Ugc3RhdGVtZW50cy4wNgYIKwYBBQUHAgEW" +
                            "Kmh0dHA6Ly93d3cuYXBwbGUuY29tL2NlcnRpZmljYXRlYXV0aG9yaXR5LzAXBgNVHSUBAf8E" +
                            "DTALBgkqhkiG92NkBA0wHQYDVR0OBBYEFFAAcixtXY1mEP5ackgjI9X+9PraMA4GA1UdDwEB" +
                            "/wQEAwIHgDAfBgoqhkiG92NkBgEhBBEMDzIwMTIwODI3MDAwMDAwWjATBgoqhkiG92NkBgEO" +
                            "AQH/BAIFADANBgkqhkiG9w0BAQsFAAOCAQEAgZNNYbV7Hw7L3qqTGWP76QQyjIw7XfzIIVlK" +
                            "HcThOw6gihW2bgz0/uC62b2IBBxklF6+M/u7dgGN2cp29mn5A/EoWLNnCigpWBkpH+fh8j3F" +
                            "qkEVOZCHftz3BiuxGSuSd5jgULlj7XkFfymZlbelzLnKtVF2ypRBivAXalLQdf/f504uHg5I" +
                            "GR3YGkI6JO8ah6lyN4AQe4QATx9eHLH2KafX0Yb1+SaNFoGvNa50+r4mu+IHRUYukc60KB0z" +
                            "wnOXVSKDVabHIXY8VowqrSZQNdXZKwyz686eqWe3IW+82RSBeULtlIlhm1j3AwBx0VtOsOYC" +
                            "B6GbPlN1eCWGOT0fsg==",
                        "MIIHEzCCBPugAwIBAgIQBunUtxaO1T63sGEL5Si7PTANBgkqhkiG9w0BAQsFADBpMQswCQYD" +
                            "VQQGEwJVUzEXMBUGA1UEChMORGlnaUNlcnQsIEluYy4xQTA/BgNVBAMTOERpZ2lDZXJ0IFRy" +
                            "dXN0ZWQgRzQgQ29kZSBTaWduaW5nIFJTQTQwOTYgU0hBMzg0IDIwMjEgQ0ExMB4XDTIyMDgy" +
                            "MzAwMDAwMFoXDTIzMDkwOTIzNTk1OVowgYAxCzAJBgNVBAYTAkVFMRAwDgYDVQQHEwdUYWxs" +
                            "aW5uMSEwHwYDVQQKDBhSaWlnaSBJbmZvc8O8c3RlZW1pIEFtZXQxITAfBgNVBAMMGFJpaWdp" +
                            "IEluZm9zw7xzdGVlbWkgQW1ldDEZMBcGCSqGSIb3DQEJARYKZWlkQHJpYS5lZTCCAaIwDQYJ" +
                            "KoZIhvcNAQEBBQADggGPADCCAYoCggGBAOUsFdsFl3wL64UC7wkG8ckTKMmcbMX2oMa+UEPL" +
                            "kmpeXk3b3O48F2ca8SKftWUzagJsdCwpqUNfxbo/0GG7KHr7j+8CbN7V49+0zNeyq0SnjoLC" +
                            "rPRZEH4ZIp1a6CP9iOxzprFIjlzKCv6oT1ZQIZ03cBaIK3QP/yRlI8f46VxNMj1BwBp5xXf8" +
                            "x6bvHlApKeLDJkaICRqYwAeGIYy1LWqdJ6C1GBxG5NepKmxTurGzwUGHCJgzeHvAjkjZW3bA" +
                            "7Eck1rNk/sFaniWev5dJXnOvDKLB+7J70nDwmbSqnKhRcbxFTY8qCoHVTZ9V6VxCS1vU+1kK" +
                            "lLTXqor52Mw2b3/dC2cqAimYV2GEmXrimDobuNjz3sCSHY+S9mAaQW87l+xxSlI0F1caSRPS" +
                            "XDd7O5RBglpSINa3eRL5SKtmJi9h6HzobtGgZhVPU+g+G6JSYJgzc8LxUFKbyW1Q83QWH1jE" +
                            "txObFfnv2lkDZGU34m6kXEGGvBFymRXGhAhatPgaUwIDAQABo4ICHTCCAhkwHwYDVR0jBBgw" +
                            "FoAUaDfg67Y7+F8Rhvv+YXsIiGX0TkIwHQYDVR0OBBYEFD78QFfeLiGpQev8nmzXe4b9kKYq" +
                            "MBUGA1UdEQQOMAyBCmVpZEByaWEuZWUwDgYDVR0PAQH/BAQDAgeAMBMGA1UdJQQMMAoGCCsG" +
                            "AQUFBwMDMIG1BgNVHR8Ega0wgaowU6BRoE+GTWh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9E" +
                            "aWdpQ2VydFRydXN0ZWRHNENvZGVTaWduaW5nUlNBNDA5NlNIQTM4NDIwMjFDQTEuY3JsMFOg" +
                            "UaBPhk1odHRwOi8vY3JsNC5kaWdpY2VydC5jb20vRGlnaUNlcnRUcnVzdGVkRzRDb2RlU2ln" +
                            "bmluZ1JTQTQwOTZTSEEzODQyMDIxQ0ExLmNybDA+BgNVHSAENzA1MDMGBmeBDAEEATApMCcG" +
                            "CCsGAQUFBwIBFhtodHRwOi8vd3d3LmRpZ2ljZXJ0LmNvbS9DUFMwgZQGCCsGAQUFBwEBBIGH" +
                            "MIGEMCQGCCsGAQUFBzABhhhodHRwOi8vb2NzcC5kaWdpY2VydC5jb20wXAYIKwYBBQUHMAKG" +
                            "UGh0dHA6Ly9jYWNlcnRzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydFRydXN0ZWRHNENvZGVTaWdu" +
                            "aW5nUlNBNDA5NlNIQTM4NDIwMjFDQTEuY3J0MAwGA1UdEwEB/wQCMAAwDQYJKoZIhvcNAQEL" +
                            "BQADggIBAL0meLrtfwUy5dRjTG8f98xTmB/IiCa9D3xudYgk9QRx1dl8oG0SJNlTqIowWG9S" +
                            "i1ieg5BitfXKlYu4BYoosmCGZgEckkO0D9IvCyTUBxX3yuUXW5jU+uwThqCq4Ssak3MzzyGX" +
                            "Is+UcoJOyH17j4J4yXyfvK5eOsXNlmb9jotWMVVsYCqVGBeA9M38Li17KgUIWmpb09J6OTOS" +
                            "yQJysSti3OIQeB3yxeVXkHxXfZGwNKxwl5mLI+KLRStOMK2oMHkP9cicKdV0SXeFdcvOdn53" +
                            "GgXHS9Qdon1Qe6Uljh0B9LtdHn2vyjTgMrVrHDG4DmiEg7GZOFmvR0jFBGPNsz4krkL+g+EZ" +
                            "lPA1PHLO3Vnec48RD6JQJs4cg61hBWnMHmf/gkp8QYuSV2u2VLgfVCQHF6EoyvCK+QU+rwKO" +
                            "TCt34Km1sR34rk5tipIbhPy2/DpeoGoXJFSkUNxUNk3DJ7sZz8rar7VHlDVqGEVYQ1666vzb" +
                            "AdXm+GzDdf1dLPy+Y3puBzJuycLwZryABpjNc+FotMUPcvpLFIJjVdZCsi0fudmi5hMaBlcI" +
                            "p72e2It5stZHbDdQNpZ3kEtf+Si750N1MaZbecGCmRSVjmrgzMmXzLymjqTe1wp+t9z8WNyD" +
                            "1pUtUrMI4Z00jsVyxnM5skTmEL5qFxd1tLiJt9BpkbNe",
                        "MIIGGzCCBQOgAwIBAgIQDmRuJmtGcd4j6HiqQzw0hzANBgkqhkiG9w0BAQsFADBZMQswCQYD" +
                            "VQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypEaWdpQ2VydCBHbG9i" +
                            "YWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjMwODMxMDAwMDAwWhcNMjQwOTMw" +
                            "MjM1OTU5WjBXMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEhMB8GA1UECgwYUmlp" +
                            "Z2kgSW5mb3PDvHN0ZWVtaSBBbWV0MRMwEQYDVQQDDAoqLmVlc3RpLmVlMHYwEAYHKoZIzj0C" +
                            "AQYFK4EEACIDYgAEIbJjZD5Mfjpd2P6FDuNclnN0hp/1ANWr05wK6/Nl/BIR/rr702rV2Y17" +
                            "uoBukHA4TvChN3P8YMHloK+TcXmjy+CQpRQtYUvm+meobN0NWSdKGASqtX9C4E6RYQKcs2mX" +
                            "o4IDjTCCA4kwHwYDVR0jBBgwFoAUdIWAwGbH3zfez70pN6oDHb7tzRcwHQYDVR0OBBYEFB/b" +
                            "eFjCUl4v17Qy2g1AgqvJwOaHMB8GA1UdEQQYMBaCCiouZWVzdGkuZWWCCGVlc3RpLmVlMA4G" +
                            "A1UdDwEB/wQEAwIHgDAdBgNVHSUEFjAUBggrBgEFBQcDAQYIKwYBBQUHAwIwgZ8GA1UdHwSB" +
                            "lzCBlDBIoEagRIZCaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xvYmFsRzJU" +
                            "TFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3JsMEigRqBEhkJodHRwOi8vY3JsNC5kaWdpY2VydC5j" +
                            "b20vRGlnaUNlcnRHbG9iYWxHMlRMU1JTQVNIQTI1NjIwMjBDQTEtMS5jcmwwPgYDVR0gBDcw" +
                            "NTAzBgZngQwBAgIwKTAnBggrBgEFBQcCARYbaHR0cDovL3d3dy5kaWdpY2VydC5jb20vQ1BT" +
                            "MIGHBggrBgEFBQcBAQR7MHkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNv" +
                            "bTBRBggrBgEFBQcwAoZFaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xv" +
                            "YmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3J0MAkGA1UdEwQCMAAwggF+BgorBgEEAdZ5" +
                            "AgQCBIIBbgSCAWoBaAB2AO7N0GTV2xrOxVy3nbTNE6Iyh0Z8vOzew1FIWUZxH7WbAAABikpp" +
                            "0YgAAAQDAEcwRQIhAOuRDRbH2F/4xj+4psS1uN7agonxJpSX7l1m9CpJX/gkAiBFDEGuoEij" +
                            "UPdQ3M5ibV6YsXW4648t7mkR0W56XiNZYAB2AEiw42vapkc0D+VqAvqdMOscUgHLVt0sgdm7" +
                            "v6s52IRzAAABikppz/EAAAQDAEcwRQIhALEE3j07957wr2WLsozkjmXPepYu5p/iTZx65kYt" +
                            "O47aAiAKS1VoZ0mMssYUcwmYs5FB79zNnVW5rXD4heRSFvpT9AB2ANq2v2s/tbYin5vCu1xr" +
                            "6HCRcWy7UYSFNL2kPTBI1/urAAABikpp0BkAAAQDAEcwRQIgOuq96euO9Aade5R6HfpNGEci" +
                            "ZUfbgW+oMmstOl3YqAUCIQDsafdu8nlmkNrN7h8uuqVXBqyv9J/u0WU80dAxPCGBiTANBgkq" +
                            "hkiG9w0BAQsFAAOCAQEAaCYTTF6Sps1YXdD6kKiYkslaxzql6D/F9Imog4pJXRZH7ye5kHuG" +
                            "OPFfnUQEqOziOspZCusX2Bz4DK4I/oc4cQnMxQHIDdF4H/GS/2aBbU/R4Ustgxkd4PCdxOn6" +
                            "lVux8aFDCRKrNBrUF1/970StNuh8tatyYvDEenwC0F3l2hRBQ3FYZMYkR9H8FM314a/sGST6" +
                            "lQiKJq2hrziMWilOwKxc88MBz9H9CYrEsCMI65iHvWA8njofxSYdM5NHhxTxhHKn6qZxHSji" +
                            "QvF9edUYTQ4wwTczmHuqYY2qxYh6WUzRyaKSeng9fe8ZVZdjOwmCa9ZdgjQYMZbDezMt+oRp" +
                            "2Q==",
                        "MIIGqzCCBZOgAwIBAgIQDbwS9oTZfnfyOWnIC0FFzzANBgkqhkiG9w0BAQsFADBPMQswCQYD" +
                            "VQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMSkwJwYDVQQDEyBEaWdpQ2VydCBUTFMg" +
                            "UlNBIFNIQTI1NiAyMDIwIENBMTAeFw0yMzA5MTQwMDAwMDBaFw0yNDEwMTQyMzU5NTlaMFox" +
                            "CzAJBgNVBAYTAkVFMRAwDgYDVQQHEwdUYWxsaW5uMRswGQYDVQQKExJTSyBJRCBTb2x1dGlv" +
                            "bnMgQVMxHDAaBgNVBAMTE3JwLWFwaS5zbWFydC1pZC5jb20wggEiMA0GCSqGSIb3DQEBAQUA" +
                            "A4IBDwAwggEKAoIBAQCNIT6IOzShQ1kEck1bKx7BSpBGOXFrBQ1oPTkWsCgE4N+CtmU3dr+2" +
                            "C7CVltvA0wiTsyI2Obj60WZzX6Z/tytjW/KLLxWCWka8WZ9SoXz3tlP/zTubGgMd+5FBTzv3" +
                            "chmXLyz+7Ywslrkl4Rv9A0/NHEj2xiKfyiZS16bC+nRlzhTefw8+LXMj2SWOhu8ZsBuqwRxw" +
                            "5//UcFZla3WdEsHfT7/nPQ8WmFG87yttmNUlKhV7d52kMoTqWUQnqhjoj6GG3BB/o3C1KuUe" +
                            "XaK+kWUgf5rkT1l6gAzMwsSI82BGixEuvqi7fQPQ2UOhAg5YDJ1t5LE+B05iY5syg+bmZWKX" +
                            "AgMBAAGjggN2MIIDcjAfBgNVHSMEGDAWgBS3a6LqqKqEjHnqtNoPmLLFlXa59DAdBgNVHQ4E" +
                            "FgQUQZTeBKY0ZUEgmj7qWgp+8H1oZUswHgYDVR0RBBcwFYITcnAtYXBpLnNtYXJ0LWlkLmNv" +
                            "bTA+BgNVHSAENzA1MDMGBmeBDAECAjApMCcGCCsGAQUFBwIBFhtodHRwOi8vd3d3LmRpZ2lj" +
                            "ZXJ0LmNvbS9DUFMwDgYDVR0PAQH/BAQDAgWgMB0GA1UdJQQWMBQGCCsGAQUFBwMCBggrBgEF" +
                            "BQcDATCBjwYDVR0fBIGHMIGEMECgPqA8hjpodHRwOi8vY3JsMy5kaWdpY2VydC5jb20vRGln" +
                            "aUNlcnRUTFNSU0FTSEEyNTYyMDIwQ0ExLTQuY3JsMECgPqA8hjpodHRwOi8vY3JsNC5kaWdp" +
                            "Y2VydC5jb20vRGlnaUNlcnRUTFNSU0FTSEEyNTYyMDIwQ0ExLTQuY3JsMH8GCCsGAQUFBwEB" +
                            "BHMwcTAkBggrBgEFBQcwAYYYaHR0cDovL29jc3AuZGlnaWNlcnQuY29tMEkGCCsGAQUFBzAC" +
                            "hj1odHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20vRGlnaUNlcnRUTFNSU0FTSEEyNTYyMDIw" +
                            "Q0ExLTEuY3J0MAwGA1UdEwEB/wQCMAAwggF+BgorBgEEAdZ5AgQCBIIBbgSCAWoBaAB1AO7N" +
                            "0GTV2xrOxVy3nbTNE6Iyh0Z8vOzew1FIWUZxH7WbAAABipL1L6QAAAQDAEYwRAIgRC3qF7YK" +
                            "hEnBiH9BrP67E2oTHBSdAhcX4MBRcdlcaAQCIGhfOOpvn5rjvXbTbr5zl4HhhVB+kcoRK7Qm" +
                            "aCSkQ9imAHYASLDja9qmRzQP5WoC+p0w6xxSActW3SyB2bu/qznYhHMAAAGKkvUvmQAABAMA" +
                            "RzBFAiAxOGUaTnZkyT1mWjAK3rMMlV3MLlIDNNvNaeiBojR86QIhALvjaTOJIQ31uGJJCGPv" +
                            "EzSdOCYusXVmAZFOJm3FqlHmAHcA2ra/az+1tiKfm8K7XGvocJFxbLtRhIU0vaQ9MEjX+6sA" +
                            "AAGKkvUv3gAABAMASDBGAiEA2AWQZe/yjoXPHBsYOewyJcqIGyRYaYBGG8BNRE3yQ2gCIQCn" +
                            "NHPkdiXExsUcAAb0hE2OFycnNAi0wqJNMq/gkeP9cTANBgkqhkiG9w0BAQsFAAOCAQEAA4U1" +
                            "OmrWavB4hIhTcoubR8JACiuQW3F9sxTYVRpNq8LrJVjV2uV05YuNnAvusDVttpHfIadBKXQi" +
                            "JlpmxVMRBvuCsQ3BmokeHlzie5RBGzVMARCuQ6/CP/1M4n2s8Q8CtdHXkbL4pgGRFrMvUrir" +
                            "yXvEK9rUMO325p6fA3AJt5fUU8XxChkS937Q5hEso21OLsTBE0EZWmoKpJz2k3t5kL2PkEy0" +
                            "bysL/PtRAc7695bbuvfc79iA27xlKT/l2n4Dl8SZXIVHkUAaCawf8b33wlDQHbFOQa72BYjV" +
                            "fx0bO6+frSFyUWKpLOJhmEJ5vMlCkmGuJghUZW6MBSRy1sccgA==",
                        "MIIGxzCCBa+gAwIBAgIQCGc4gK2IFG5sQ7qUAnmodTANBgkqhkiG9w0BAQsFADBZMQswCQYD" +
                            "VQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypEaWdpQ2VydCBHbG9i" +
                            "YWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjMxMDE3MDAwMDAwWhcNMjQxMTE2" +
                            "MjM1OTU5WjBVMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEhMB8GA1UECgwYUmlp" +
                            "Z2kgSW5mb3PDvHN0ZWVtaSBBbWV0MREwDwYDVQQDDAgqLnJpYS5lZTB2MBAGByqGSM49AgEG" +
                            "BSuBBAAiA2IABOYBKbUstAoHqrc8+wvdz8BCwTLq7e+NVI7hVHE/Lhe96LXq/+EIojuRSv/s" +
                            "J3VR3SA9iaUM/YDyTkGEIslAuoE9gAdMQAUKLHlMn239CLmJLWFrmjXuB5EWXCBPP7MVlaOC" +
                            "BDswggQ3MB8GA1UdIwQYMBaAFHSFgMBmx9833s+9KTeqAx2+7c0XMB0GA1UdDgQWBBRgQWKG" +
                            "jIZPe1FBACPtZ/ktZmmpTDCByAYDVR0RBIHAMIG9gggqLnJpYS5lZYILcG9zdC5yaWEuZWWC" +
                            "C2VkZ2UucmlhLmVlggpzaXAucmlhLmVlggttZWV0LnJpYS5lZYINZGlhbGluLnJpYS5lZYIT" +
                            "bHluY2Rpc2NvdmVyLnJpYS5lZYILbGRhcC5yaWEuZWWCDGRjLTAxLnJpYS5lZYIMZGMtMDIu" +
                            "cmlhLmVlggtzbXRwLnJpYS5lZYINZXhjLTAxLnJpYS5lZYINZXhjLTAyLnJpYS5lZYIGcmlh" +
                            "LmVlMD4GA1UdIAQ3MDUwMwYGZ4EMAQICMCkwJwYIKwYBBQUHAgEWG2h0dHA6Ly93d3cuZGln" +
                            "aWNlcnQuY29tL0NQUzAOBgNVHQ8BAf8EBAMCA4gwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsG" +
                            "AQUFBwMCMIGfBgNVHR8EgZcwgZQwSKBGoESGQmh0dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9E" +
                            "aWdpQ2VydEdsb2JhbEcyVExTUlNBU0hBMjU2MjAyMENBMS0xLmNybDBIoEagRIZCaHR0cDov" +
                            "L2NybDQuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0Ex" +
                            "LTEuY3JsMIGHBggrBgEFBQcBAQR7MHkwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2lj" +
                            "ZXJ0LmNvbTBRBggrBgEFBQcwAoZFaHR0cDovL2NhY2VydHMuZGlnaWNlcnQuY29tL0RpZ2lD" +
                            "ZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3J0MAwGA1UdEwEB/wQCMAAwggF/" +
                            "BgorBgEEAdZ5AgQCBIIBbwSCAWsBaQB2AO7N0GTV2xrOxVy3nbTNE6Iyh0Z8vOzew1FIWUZx" +
                            "H7WbAAABiz1XNJgAAAQDAEcwRQIhAOWX0YWs3FsrXaHiiaRkBrSdLPPvQ55BJFrluOGpQ3oK" +
                            "AiAyL9tbc7kqS3XYDPOgKn9R54x+bsbcxiYj6UgETkg+0AB3AEiw42vapkc0D+VqAvqdMOsc" +
                            "UgHLVt0sgdm7v6s52IRzAAABiz1XNA0AAAQDAEgwRgIhAJQftRI0svKD6jmxAGBnjNyXRfWn" +
                            "VZkFeew2FsUiALP9AiEAlRpmd5cSKMtLUlnKGLmgq8nTOo5Es6PLpO/vDvs/+wwAdgDatr9r" +
                            "P7W2Ip+bwrtca+hwkXFsu1GEhTS9pD0wSNf7qwAAAYs9VzPlAAAEAwBHMEUCIQCY3ojnU0Wo" +
                            "YC1qjUMJ6i2WEkgkRTX3+AMGqNJ+CAnWJgIgNUlqlzaqLCIMJmB1DhVDFTB5W3lJEEwIWxj3" +
                            "RO55VGYwDQYJKoZIhvcNAQELBQADggEBAAig8nd2zQTm9XPTxM4S0CYgY66R5JgJIwCdt5c6" +
                            "uZ3yd3MXE3+dDovwzGbSLziKLvzohPATe2XT2fpYFU9AZDdJQ+3XNAmoWMWRftv/eezv1CW0" +
                            "23V3vsaUdutkBgg7iCBvYEPw0nXxP8DpNY/keXnC40ZhaAXgoVcdH5oK6UkjI30V5qDwMslL" +
                            "UjQxhNu0LMbB2R3tBZKcYUu4L1W1Kn/QyXbmMNoBusLDM0bOT8AXa+3iLyV/rCsIR+f/OWDB" +
                            "TfXwDC3FNHkf7eMQAvoHSs1Wp6rA5QlbjnelVKF5Q0a6p9Ciy/qFfQN5lQdjh1jhesxBvsgb" +
                            "J/Kj0lH3pr+sv3A=",
                        "MIIG2zCCBcOgAwIBAgIQDlQIAFM0WCbkUSFQ7zdq0DANBgkqhkiG9w0BAQsFADBZMQswCQYD" +
                            "VQQGEwJVUzEVMBMGA1UEChMMRGlnaUNlcnQgSW5jMTMwMQYDVQQDEypEaWdpQ2VydCBHbG9i" +
                            "YWwgRzIgVExTIFJTQSBTSEEyNTYgMjAyMCBDQTEwHhcNMjQwMTEyMDAwMDAwWhcNMjUwMjEx" +
                            "MjM1OTU5WjBQMQswCQYDVQQGEwJFRTEQMA4GA1UEBxMHVGFsbGlubjEbMBkGA1UEChMSU0sg" +
                            "SUQgU29sdXRpb25zIEFTMRIwEAYDVQQDEwltaWQuc2suZWUwggEiMA0GCSqGSIb3DQEBAQUA" +
                            "A4IBDwAwggEKAoIBAQCZiyx8QGoJP+h4t1tT2gw2kTeQHpsmYEURrYeBsBont1HhSnck+bwr" +
                            "8DEfOcVOE9fgFFl9Ds7EYr4uwsMlC/DDdE+zTOGK/y2cVmfMdCzIAkz+lmcHVV5K7cxGd54i" +
                            "0821UnpTSKZdAOce8JWgynvMHTmUkTn/XFk1UUeC3HhcsWdPXFbrAp9d3EYMOHJdGFqJorFx" +
                            "BcF2lw2jaMhGl0uIFtw1A2Vww3udpSLvT3OPZobBqv96atydKnmtGC0ka4dhoo3Qb7VbEbdP" +
                            "i/WJ6P7pODU4VplZjgvlZEP31wS1odZZNBp4PVmWUakfrQGFLDncwbeMxaiYqP6czf9Edznh" +
                            "AgMBAAGjggOmMIIDojAfBgNVHSMEGDAWgBR0hYDAZsffN97PvSk3qgMdvu3NFzAdBgNVHQ4E" +
                            "FgQU6MHulpVddTFkeStlIxwkIu4wYLswOAYDVR0RBDEwL4IJbWlkLnNrLmVlggxzdGF0dXMu" +
                            "c2suZWWCFGRpZ2lkb2NzZXJ2aWNlLnNrLmVlMD4GA1UdIAQ3MDUwMwYGZ4EMAQICMCkwJwYI" +
                            "KwYBBQUHAgEWG2h0dHA6Ly93d3cuZGlnaWNlcnQuY29tL0NQUzAOBgNVHQ8BAf8EBAMCBaAw" +
                            "HQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGfBgNVHR8EgZcwgZQwSKBGoESGQmh0" +
                            "dHA6Ly9jcmwzLmRpZ2ljZXJ0LmNvbS9EaWdpQ2VydEdsb2JhbEcyVExTUlNBU0hBMjU2MjAy" +
                            "MENBMS0xLmNybDBIoEagRIZCaHR0cDovL2NybDQuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xv" +
                            "YmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0ExLTEuY3JsMIGHBggrBgEFBQcBAQR7MHkwJAYIKwYB" +
                            "BQUHMAGGGGh0dHA6Ly9vY3NwLmRpZ2ljZXJ0LmNvbTBRBggrBgEFBQcwAoZFaHR0cDovL2Nh" +
                            "Y2VydHMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0R2xvYmFsRzJUTFNSU0FTSEEyNTYyMDIwQ0Ex" +
                            "LTEuY3J0MAwGA1UdEwEB/wQCMAAwggF7BgorBgEEAdZ5AgQCBIIBawSCAWcBZQB1AM8RVu7V" +
                            "Lnyv84db2Wkum+kacWdKsBfsrAHSW3fOzDsIAAABjP06eP4AAAQDAEYwRAIgczn6HKzeC155" +
                            "F/zcXDq+yBkWYOLySkvcuIUkOX6kmv0CIEMkTopBBFD1h4EDgBRFvpI9eqee/8ajf6vIFmIj" +
                            "UCv/AHQAfVkeEuF4KnscYWd8Xv340IdcFKBOlZ65Ay/ZDowuebgAAAGM/Tp4uwAABAMARTBD" +
                            "Ah9aQ3ogcFz/ImJO11KejbwxOaW4TNemJHR6LWc3F5W0AiA6vI6kEz5bxgNrZbosXcAvS4EC" +
                            "qfAm2RbCPv0JsnaJJAB2AObSMWNAd4zBEEEG13G5zsHSQPaWhIb7uocyHf0eN45QAAABjP06" +
                            "eOYAAAQDAEcwRQIgfIxL7QiOZKobDodUBRbB/jrE4kWbfKwW69VsXiZSP+cCIQCaq2PgQiCn" +
                            "j9Zmd1icbz4SxLOAKO33LeA56Z6UJaIBzjANBgkqhkiG9w0BAQsFAAOCAQEAJtZjRGHuBCxf" +
                            "cIlUO88gTdSR5srJn+MnP8VJdv74no/CQ2anGXqNXpk8+E4j7tE5Hv4CDB9m7s09hAii6qz0" +
                            "gtAR0CzxlVQ0brOMYX99qYN+7eLo75o4ssK5kPajIDQNFKqinGrGS2E+os+IlNnaoXzwbJLJ" +
                            "C/tu3qYuWzkcSFZaMOwL7zxLlGlJfszW6pAfKPhZQucQRkmV/6N8SazBmfGFlHkgZB5BDeaX" +
                            "Ukchd8B6VJYC+hn+NReo26/tGCy9/RBeyffMnWkgYKcmTpQafQzVO0WpKAl/U4PdEELGgHMM" +
                            "UhOdarv+JIECOcFoaSSn+sd6dkOMpFxwvPaZmu5XUA==",
                        "MIIFuTCCA6GgAwIBAgIQdGXMmxhPDu1hWuq15s9LKTANBgkqhkiG9w0BAQwFADBmMQswCQYD" +
                            "VQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0x" +
                            "MDc0NzAxMzEhMB8GA1UEAwwYU0sgSUQgU29sdXRpb25zIFJPT1QgRzFSMB4XDTIxMTAwNDEx" +
                            "NTExN1oXDTQxMTAwNDExNTExN1owZjELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNv" +
                            "bHV0aW9ucyBBUzEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxITAfBgNVBAMMGFNLIElEIFNv" +
                            "bHV0aW9ucyBST09UIEcxUjCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAL7mnRyY" +
                            "j4tyd8hvdY3RKvbRCDa70qUX+bi6GV081Y40QlpNvbZiB/Q31bMxOuHMZ9qmMktJyAS4d3LX" +
                            "aOsvrJT3kVg9Wk9+SJ071pMT4jKdelFR0e4pIM28DwLsfQ9n32kCyOTbSLz0cZ8RnZUW3Yk8" +
                            "qX2ORnFtn/oscJW4EWCBOh3hYlIK8MEyDqNr5MByrZ9Ew5LebDYxeOqa1KereTXVrF8RmWYp" +
                            "uXGuuMen5ujZtBjaF2LlSsb/chX4PtapgUZKLHVfPjVlpBlx1IAKcQCWorB/vqQeeBqGzLim" +
                            "lG7FiSA1IodYMQCncXqcY/Z/nbQTSNkRJd6xHVzCVJv6EqlW6lVnak6nJ5uCyITlVj3/oDxO" +
                            "DiNe3Sc14/vPu4YvjX1U9NsNuC4VZiScarTSbgphi/TWmFkX3Cvyb1abBNJSbN7R7R4An49W" +
                            "l28uN87h7mu7HydcCF0Urqm1kjLryK7Y0ApGD5v6U3kRpIXvaqCr/+TVmBCiDvvwaMvxlZ+P" +
                            "pUOJMscYtWm8ijouup5AeFqnQWFzjZjF7ssL7u7Gf28xuWUj8yOJOWpKsMlkgTYz4GuroYsR" +
                            "cbjWOxTbjk9amRiHfMoQO3EjMBCq3fZUcRfomgc9xhu03Rj/P+sCkFI1kZDQaC5HH6KtnvO5" +
                            "F4INqgQlOIPuaaYGlaCCqeGoWG/ZAgMBAAGjYzBhMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0P" +
                            "AQH/BAQDAgEGMB0GA1UdDgQWBBSVDbdkGMKmm2Z22Pz8mlokvCjWzTAfBgNVHSMEGDAWgBSV" +
                            "DbdkGMKmm2Z22Pz8mlokvCjWzTANBgkqhkiG9w0BAQwFAAOCAgEAO8FcJcrsEGZXQasT4PtT" +
                            "N1DhvBtABv9iNR8rEeS9k0bTj4Y5FZ2kGeeYHscW3S9fxM3718nkbCTdGCJX2UM2aw+WZ9pN" +
                            "LjwNUuumxXE8ydnX0TLX7UozULBH7oIoYUJ5a6OFT/ZfJ4WfET7WXapIYzyl2rQ3VJnjoHLq" +
                            "5u6PqhnU8kJ41tivjsttmlbVJ1LKKMrbv2JoOv39E4NukarlK/vJ0CDWKGIhziusS/Uj1dxn" +
                            "Q4tYzakMlni3suNcgAl7atMShIUP1VjDDfcIs0V8RUfDVXEuyFsCU2FYxTeTwc7NA8V4zqtN" +
                            "KVHgkUoJkDaJM3EMyscyUf0EibBxMUt/uDLGmcHG/ap3fyNxG8ewtB44ky1DJ6LR6Boq2ieI" +
                            "khM5TdHrYldZ39i66Kd7ssFXcUgDzrcQI/wVlCKnjnbhVnsNyB/W6FzdIqYzTzwMC4BYhsiJ" +
                            "Q9AuEyPDJuetp8Rzdg59zdgiC1zqTMkIWxe5Kdnu/9QzvB12lXyTAS1xGKEWt7XM3MzhPZjY" +
                            "aB0EHrXdpd79k0YP9gxYIILISa20rWl95IbiYSVR6LvgiZOJV39SyPKHbRDECL2uz9s0ZgZk" +
                            "u0B1yrwUwsL4YW8dNRw7InVzH/EoMANVkhuNQDiF9/zGQX3/5fcX5tJZaD3vy9mYJAJ5d/vh" +
                            "cGOqHbI3s2W27+c=",
                        "MIICtDCCAhagAwIBAgIQSmaL1ubiC3FhWulCIhJ3+zAKBggqhkjOPQQDBDBmMQswCQYDVQQG" +
                            "EwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0" +
                            "NzAxMzEhMB8GA1UEAwwYU0sgSUQgU29sdXRpb25zIFJPT1QgRzFFMB4XDTIxMTAwNDExNDUw" +
                            "NloXDTQxMTAwNDExNDUwNlowZjELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNvbHV0" +
                            "aW9ucyBBUzEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxITAfBgNVBAMMGFNLIElEIFNvbHV0" +
                            "aW9ucyBST09UIEcxRTCBmzAQBgcqhkjOPQIBBgUrgQQAIwOBhgAEAN+zN0QNRH0pL6ozIEqF" +
                            "SBiqASoVlcgoi2+epQ6fU/gJ5ZbySnNtjcfxqqi3sW+XHK3IXN2kwMImzWx/P7S4ZhRfAJNJ" +
                            "i6qPmyyDnXIE17jUuqVBcPZwpap3cqQND3FA3/wgs3r/dnBHyQD4M2ihf3j4IDy/ANl6IiI2" +
                            "D4Z50ik4QUfYo2MwYTAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4E" +
                            "FgQUhnRPOus48rCn7u25hZudgwlFMWswHwYDVR0jBBgwFoAUhnRPOus48rCn7u25hZudgwlF" +
                            "MWswCgYIKoZIzj0EAwQDgYsAMIGHAkF+pYLDdJjsHc7hvW0dlIPk6riZKr3PDe6+3t3Paq+U" +
                            "db4OGw3yzJoxCKQbnLULtlN6weO6wupBn91X+wX6QbCy/gJCAK/JtP1UsWLkmxCannuKGOIH" +
                            "8CgcKIXdW/z7/TQLfrygdJMgZ5TjzLMHXoX3gKSTNpCuZEeamEYBzd8HKLDnKYWV",
                        "MIIDiDCCAuqgAwIBAgIQH5sBIc4ZTj1hWvEFaRQX9DAKBggqhkjOPQQDAzBmMQswCQYDVQQG" +
                            "EwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0" +
                            "NzAxMzEhMB8GA1UEAwwYU0sgSUQgU29sdXRpb25zIFJPT1QgRzFFMB4XDTIxMTAwNDEyMTgx" +
                            "MloXDTM2MTAwNDEyMTgxMlowZzELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNvbHV0" +
                            "aW9ucyBBUzEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxIjAgBgNVBAMMGVNLIElEIFNvbHV0" +
                            "aW9ucyBPUkcgMjAyMUUwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAT5UvJTY26GyX5dkIOsjGOv" +
                            "y4W1MFXjWgXW3q9aqT1yuEOUyuPnHtm/fTUiIXY5PCVviBhdqubUdP/U9bxOs6M6oTptGU9S" +
                            "JgQ7JY9tK4HbpJPk+J3Iz1JHMqq3JCktyUWjggFaMIIBVjAfBgNVHSMEGDAWgBSGdE866zjy" +
                            "sKfu7bmFm52DCUUxazAdBgNVHQ4EFgQU/Inn/EN4/+wsw4Soo4DjI0ga1CgwDgYDVR0PAQH/" +
                            "BAQDAgEGMBIGA1UdEwEB/wQIMAYBAf8CAQAwbQYIKwYBBQUHAQEEYTBfMCAGCCsGAQUFBzAB" +
                            "hhRodHRwOi8vb2NzcC5zay5lZS9DQTA7BggrBgEFBQcwAoYvaHR0cDovL2Muc2suZWUvU0tf" +
                            "SURfU29sdXRpb25zX1JPT1RfRzFFLmRlci5jcnQwLwYDVR0fBCgwJjAkoCKgIIYeaHR0cDov" +
                            "L2Muc2suZWUvU0tfUk9PVF9HMUUuY3JsMFAGA1UdIARJMEcwRQYEVR0gADA9MDsGCCsGAQUF" +
                            "BwIBFi9odHRwczovL3d3dy5za2lkc29sdXRpb25zLmV1L2VuL3JlcG9zaXRvcnkvQ1BTLzAK" +
                            "BggqhkjOPQQDAwOBiwAwgYcCQXQIw72bQj2lOzbDp3xy5TlFqBPQBIN0YbWV+TSenzGfqT6a" +
                            "QjHTZ9migRQeEXw97kXLSD3wN/g9LQ9Ok1dZvDQ3AkIAt+bOVtVzJr+43e8IFl4Cot5wPywc" +
                            "mI042EtuAT1SJ7lOkIUoaQZ6ZPj3pnI+5wX+z6s753MznY97AdwpGvEADkE=",
                        "MIIGszCCBJugAwIBAgIQTrdBH/kVRQFhWvLs8/TEtjANBgkqhkiG9w0BAQwFADBmMQswCQYD" +
                            "VQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0x" +
                            "MDc0NzAxMzEhMB8GA1UEAwwYU0sgSUQgU29sdXRpb25zIFJPT1QgRzFSMB4XDTIxMTAwNDEy" +
                            "MjYyMFoXDTM2MTAwNDEyMjYyMFowZzELMAkGA1UEBhMCRUUxGzAZBgNVBAoMElNLIElEIFNv" +
                            "bHV0aW9ucyBBUzEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxIjAgBgNVBAMMGVNLIElEIFNv" +
                            "bHV0aW9ucyBPUkcgMjAyMVIwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCv5c8Z" +
                            "z+xsh2xEQ3R4grFpZjzzJuRkHXs0xhVHNhJG8WZh6NldtLBHJPVT1PD8au9ism51NJMirstd" +
                            "buYVBuoGXIBf+1/lg0M4KaLEOb1wgz/BXhBgRDMXjGdqlv24MnPx5Il0qTb2G7pBauksvTIO" +
                            "KWncM1CvcBA46zWjXHRGNv87BJcACaaU9/DA4hZZ1/my0Z+d+Fdh2YNCqfXWEJ8Sz3UVO16Q" +
                            "Mas2g8mL5vsp/0Viy4HAX5Gw+XwUmU+HKqdtHf0iW0zjTdUafOPdrbt0f1ivIi9FtZ9CpBXx" +
                            "UPdgC798VnK3jsaUwGwlKhXf7coT+6ulGSvERGCeWQ9yLfoswAieqDZ7Zp7iDG6CTMvMfn+d" +
                            "TdHqmkIWG2JYcFW9QVoCyQKTjvZfxtO1xAcVavvgXa+vaXX8EsyQqnYF7Zkp5PzB5/x9jmX7" +
                            "0HBHEPc1CLarxYpMLLPc3FqtMiX2wVqv5HtfKuE8Q6xqmcQWMbjl51xHBrMOD51ahOs3W9bq" +
                            "6NT/BsHvzZ+fSFcjRjh4Jq7OFN/ymA0hD4+rbrixgpkJ3w1iA2mLyPT5BfwL2fEGMxtWxFj7" +
                            "y2ETzXN/mW34+CqyJcQHRYeXs9/bvg1iXiRyTCZA0Mkw8Z8E56PyXbNnhH6AOMg7S1YQ7yXF" +
                            "XzESEJaJ7mBZl5jPg4t/FThzPHnwwwIDAQABo4IBWjCCAVYwHwYDVR0jBBgwFoAUlQ23ZBjC" +
                            "pptmdtj8/JpaJLwo1s0wHQYDVR0OBBYEFOLGphjgoLJJ8oWgse1E8b2HyKY2MA4GA1UdDwEB" +
                            "/wQEAwIBBjASBgNVHRMBAf8ECDAGAQH/AgEAMG0GCCsGAQUFBwEBBGEwXzAgBggrBgEFBQcw" +
                            "AYYUaHR0cDovL29jc3Auc2suZWUvQ0EwOwYIKwYBBQUHMAKGL2h0dHA6Ly9jLnNrLmVlL1NL" +
                            "X0lEX1NvbHV0aW9uc19ST09UX0cxUi5kZXIuY3J0MC8GA1UdHwQoMCYwJKAioCCGHmh0dHA6" +
                            "Ly9jLnNrLmVlL1NLX1JPT1RfRzFSLmNybDBQBgNVHSAESTBHMEUGBFUdIAAwPTA7BggrBgEF" +
                            "BQcCARYvaHR0cHM6Ly93d3cuc2tpZHNvbHV0aW9ucy5ldS9lbi9yZXBvc2l0b3J5L0NQUy8w" +
                            "DQYJKoZIhvcNAQEMBQADggIBAC/df7QO74rBXo+wQnChGnbtpWZSdISgqMKtBhO3hAvy7s27" +
                            "odXThZi9fLPEpqw9CI5NSeGz6e5HGc258o4kYzhymMLF7Hyne2S5OVvfJ25XMtXOR23MGC4Y" +
                            "n1ogZXNPlaZULvty/1UzSLbjdGDnkfdHWTEPJPskXHS1CM70ZM2wGtfAjPRW90wThSZAh6CS" +
                            "/KNlJTTOjYMDYknQPTESiUV33YiB7xL5wV/9ymoM03IvZ0ew0ktY1YNsfr4eb3FfcFmCiXMR" +
                            "mPYbdncxBvyzLXxHoZtZJq2y3SHpPb1fqWv+LQC6+c3YeEYYyzNVwCgpdOpvr3QU6imr2H8e" +
                            "IcKCe5unD+gJFZC4+ITcF56RLQ/uzddwez+QYjWyIg9ZEwfvZZOqoWzvhxNlgr2yUs8cPV6r" +
                            "K3I+STVvS4BgwsGQlVrbddwllNHDVHbO3h6zSkjqUhYcAHWCHzptbjtl5GOVs/55m2/knlrx" +
                            "l8MuDXPl5h2PosaVqO7dI+2Sl/rpDTwyPlfnwdV8vKS6m81yc7H2pn29EspMBTE6/dRoaq2q" +
                            "0B17lt9ojuhkEus8QCbuFWs0avUnmDALqZq2qBg878WFSEIxS87EJ6X/zFm9kf/u+4aCN9hu" +
                            "l2YzqQ/saFpWsvg083GHz1gNiikXYeUwygZmqqOj8tbX+thcE+XU/HSLWlHR",
                        "MIIFrzCCA5egAwIBAgIQBBYz+OVlgi4DZk1l1P8SCjANBgkqhkiG9w0BAQsFADBpMQswCQYD" +
                            "VQQGEwJVUzEXMBUGA1UEChMORGlnaUNlcnQsIEluYy4xQTA/BgNVBAMTOERpZ2lDZXJ0IFRy" +
                            "dXN0ZWQgRzQgQ29kZSBTaWduaW5nIFJTQTQwOTYgU0hBMzg0IDIwMjEgQ0ExMB4XDTI0MDEw" +
                            "OTAwMDAwMFoXDTI1MDEwODIzNTk1OVowZTELMAkGA1UEBhMCRUUxEDAOBgNVBAcTB1RhbGxp" +
                            "bm4xITAfBgNVBAoMGFJpaWdpIEluZm9zw7xzdGVlbWkgQW1ldDEhMB8GA1UEAwwYUmlpZ2kg" +
                            "SW5mb3PDvHN0ZWVtaSBBbWV0MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE4BDkjB13oUKtykcF" +
                            "ratt7OpuCpFnpAOVMWsS+L27gSgoqbESCWoml5fyzNMaE6qZ0/M5vPeBO3iOmOzJHJz5iK5t" +
                            "ckE0rXV9RXZSjywiTD5nJuVBCvEgR1YDPHVMZ7dto4ICAzCCAf8wHwYDVR0jBBgwFoAUaDfg" +
                            "67Y7+F8Rhvv+YXsIiGX0TkIwHQYDVR0OBBYEFG4NeiVHkn4KqyS7iMmzzpUNPw14MD4GA1Ud" +
                            "IAQ3MDUwMwYGZ4EMAQQBMCkwJwYIKwYBBQUHAgEWG2h0dHA6Ly93d3cuZGlnaWNlcnQuY29t" +
                            "L0NQUzAOBgNVHQ8BAf8EBAMCB4AwEwYDVR0lBAwwCgYIKwYBBQUHAwMwgbUGA1UdHwSBrTCB" +
                            "qjBToFGgT4ZNaHR0cDovL2NybDMuZGlnaWNlcnQuY29tL0RpZ2lDZXJ0VHJ1c3RlZEc0Q29k" +
                            "ZVNpZ25pbmdSU0E0MDk2U0hBMzg0MjAyMUNBMS5jcmwwU6BRoE+GTWh0dHA6Ly9jcmw0LmRp" +
                            "Z2ljZXJ0LmNvbS9EaWdpQ2VydFRydXN0ZWRHNENvZGVTaWduaW5nUlNBNDA5NlNIQTM4NDIw" +
                            "MjFDQTEuY3JsMIGUBggrBgEFBQcBAQSBhzCBhDAkBggrBgEFBQcwAYYYaHR0cDovL29jc3Au" +
                            "ZGlnaWNlcnQuY29tMFwGCCsGAQUFBzAChlBodHRwOi8vY2FjZXJ0cy5kaWdpY2VydC5jb20v" +
                            "RGlnaUNlcnRUcnVzdGVkRzRDb2RlU2lnbmluZ1JTQTQwOTZTSEEzODQyMDIxQ0ExLmNydDAJ" +
                            "BgNVHRMEAjAAMA0GCSqGSIb3DQEBCwUAA4ICAQBwzQxT8p2vhjSdbod6SCq3gQ8Nyyt9MWJL" +
                            "kO0ExY6fk0LxShuyyxllIlr+JJr7GpRyZpxq6hO5/v5f9w2LXauyZvnvU6cbeO8PgQVMNN+n" +
                            "ebx8WhHjtGLLAGRRPr7c7lEt/euBGk4oevV5BfxDyobkXqS61BL8lXWK4FWqPr8KiR7n6g/U" +
                            "mgrqJ+p0ZwMGLfsXnJvFLaCQqQISwAg3D7QyejdSvU6yNcAiVyUMlYQEA7qq2YrI1r1eCyHD" +
                            "gUoUSaamIpgwED/ujrtlbyuUricy2HDhlWxBBgUOfGwxwCyDkjTHQqcGfFk+m7QCWge5ao3U" +
                            "xm0gStgO0v6BraQLYxxYYz6b0Z/Uf0IQkgNyNByVDgv2HJmcfV0/zfNBz4XUJKOnCaemxa14" +
                            "TROZzo6ScSxdUtsaQ3DVNbdwp90VCrzvZbdm+tiYpXcK/Jz6u8eIz4ECuue5BNT7iIDqAQpn" +
                            "fhXn0WXwvO/BvJbyNxsSt5ahkZe9IxY4M84xIGEgVL9AY4E7i9AuXr0D2rH3VcK2qcDev0jF" +
                            "zB7ExHTtdaZNl8Gj6P1WrmR18fEWhOmLQiyhxESHEFnc8NJfcB7D9W6y+/ZiJD5RqXYOtW0p" +
                            "77368oBOY7hPTNy1geWW4IOtvlnSFnH5uqY4RLlvNr1p0EwowRjybHyKVdOML+kviEB2V3Ni" +
                            "Gw==",
                    ),
                )
            withContext(Dispatchers.Main) {
                mobileSignService.errorState.observeForever {
                    if (it != null) {
                        _errorState.postValue(it)
                    }
                }
                mobileSignService.challenge.observeForever { challenge ->
                    if (challenge != null) {
                        _challenge.postValue(challenge)
                    }
                }
                mobileSignService.status.observeForever {
                    if (it != null) {
                        _status.postValue(it)
                        _errorState.postValue(
                            context.getString(
                                messages[it]
                                    ?: R.string.signature_update_mobile_id_error_general_client,
                            ),
                        )
                    }
                }
                mobileSignService.response.observeForever {
                    when (it?.status) {
                        MobileCreateSignatureProcessStatus.USER_CANCELLED -> {
                            _status.postValue(it.status)
                            _errorState.postValue(
                                context.getString(
                                    messages[it.status]
                                        ?: R.string.signature_update_mobile_id_error_general_client,
                                ),
                            )
                        }

                        MobileCreateSignatureProcessStatus.OK -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                val uri: Uri? =
                                    container?.getContainerFile()?.let { file ->
                                        FileProvider.getUriForFile(
                                            context,
                                            context.getString(R.string.file_provider_authority),
                                            file,
                                        )
                                    }
                                if (uri != null) {
                                    val signedContainer =
                                        fileOpeningRepository.openOrCreateContainer(
                                            context,
                                            contentResolver,
                                            listOf(uri),
                                        )
                                    _signedContainer.postValue(signedContainer)
                                }
                            }

                            _signature.postValue(it.signature)
                            _status.postValue(it.status)
                        }

                        else -> {
                            if (it != null) {
                                _status.postValue(it.status)
                                _errorState.postValue(
                                    context.getString(
                                        messages[it.status]
                                            ?: R.string.signature_update_mobile_id_error_general_client,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            mobileSignService.processMobileIdRequest(
                request,
                roleData,
                proxySetting,
                manualProxySettings,
                // TODO: configurationProvider?.certBundle,
                certBundle,
                Objects.requireNonNull(Conf.instance()).PKCS12Cert(),
                Objects.requireNonNull(Conf.instance()).PKCS12Pass(),
            )
        }

        fun positiveButtonEnabled(
            phoneNumber: String?,
            personalCode: String?,
        ): Boolean {
            if (phoneNumber != null && personalCode != null) {
                validatePersonalCode(personalCode)

                return isCountryCodeCorrect(phoneNumber.toString()) &&
                    isPhoneNumberCorrect(phoneNumber.toString()) &&
                    isPersonalCodeCorrect(personalCode.toString())
            }
            return false
        }

        private fun isCountryCodeCorrect(phoneNumber: String): Boolean {
            for (allowedCountryCode in ALLOWED_PHONE_NUMBER_COUNTRY_CODES) {
                if (phoneNumber.startsWith(allowedCountryCode)) {
                    return true
                }
            }
            return false
        }

        private fun isPhoneNumberCorrect(phoneNumber: String): Boolean {
            return phoneNumber.length >= MINIMUM_PHONE_NUMBER_LENGTH
        }

        private fun isPersonalCodeCorrect(personalCode: String): Boolean {
            return personalCode.length == MAXIMUM_PERSONAL_CODE_LENGTH
        }
    }
