@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.repository

import android.content.Context
import com.google.common.collect.ImmutableList
import com.unboundid.asn1.ASN1OctetString
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.ResultCode
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl
import com.unboundid.util.LDAPTestUtils
import com.unboundid.util.ssl.SSLUtil
import com.unboundid.util.ssl.TLSCipherSuiteSelector
import ee.ria.DigiDoc.common.Constant.BASE_DN
import ee.ria.DigiDoc.common.Constant.CERT_BINARY_ATTR
import ee.ria.DigiDoc.common.Constant.LDAP_PORT
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.exception.NoInternetConnectionException
import ee.ria.DigiDoc.common.model.EIDType
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.cryptolib.exception.CryptoException
import ee.ria.DigiDoc.cryptolib.ldap.EstEidLdapFilter
import ee.ria.DigiDoc.cryptolib.ldap.LdapFilter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLSocketFactory

@Singleton
class RecipientRepositoryImpl
    @Inject
    constructor(
        private val configurationRepository: ConfigurationRepository,
        private val certificateService: CertificateService,
    ) : RecipientRepository {
        @Throws(CryptoException::class, NoInternetConnectionException::class)
        override suspend fun find(
            context: Context,
            query: String,
        ): List<Addressee> {
            var certs: List<Addressee>
            withContext(IO) {
                certs =
                    try {
                        findPersonCertificate(context, query)
                    } catch (e: NoInternetConnectionException) {
                        throw e
                    } catch (_: CryptoException) {
                        findCorporationCertificate(context, query)
                    }
                certs = if (certs.isEmpty()) findCorporationCertificate(context, query) else certs
            }

            return certs
        }

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        private fun findPersonCertificate(
            context: Context,
            query: String,
        ): List<Addressee> {
            val configurationProvider = configurationRepository.getConfiguration()
            val ldapPersonUrl = configurationProvider?.ldapPersonUrl?.split("://")[1]
            return search(context, ldapPersonUrl, EstEidLdapFilter(query))
        }

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        private fun findCorporationCertificate(
            context: Context,
            query: String,
        ): List<Addressee> {
            val configurationProvider = configurationRepository.getConfiguration()
            val ldapCorpUrl = configurationProvider?.ldapCorpUrl?.split("://")[1]
            return search(context, ldapCorpUrl, LdapFilter(query))
        }

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        private fun search(
            context: Context,
            url: String?,
            ldapFilter: LdapFilter,
        ): List<Addressee> {
            try {
                LDAPConnection(getDefaultKeystoreSslSocketFactory()).use { connection ->
                    connection.connect(url, LDAP_PORT)
                    return executeSearch(connection, ldapFilter)
                }
            } catch (e: Exception) {
                if (e is LDAPException && e.resultCode.equals(ResultCode.CONNECT_ERROR)) {
                    throw NoInternetConnectionException(context)
                }
                throw CryptoException("Finding recipients failed", e)
            }
        }

        @Throws(LDAPException::class, IOException::class)
        private fun executeSearch(
            connection: LDAPConnection,
            ldapFilter: LdapFilter,
        ): List<Addressee> {
            val maximumNumberOfResults = 50
            val searchRequest =
                SearchRequest(
                    BASE_DN,
                    SearchScope.SUB,
                    ldapFilter.filterString(),
                    CERT_BINARY_ATTR,
                )
            var extraResponseCookie: ASN1OctetString? = null
            val builder: ImmutableList.Builder<Addressee> = ImmutableList.builder<Addressee>()
            while (true) {
                searchRequest.setControls(
                    SimplePagedResultsControl(
                        maximumNumberOfResults,
                        extraResponseCookie,
                    ),
                )
                val searchResult = connection.search(searchRequest)
                for (entry in searchResult.getSearchEntries()) {
                    for (attribute in entry.attributes) {
                        for (value in attribute.rawValues) {
                            val certificate = ExtendedCertificate.create(value.value, certificateService)
                            if (isSuitableKeyAndNotMobileId(certificate)) {
                                builder.add(Addressee(certificate.data))
                            }
                        }
                    }
                }

                LDAPTestUtils.assertHasControl(
                    searchResult,
                    SimplePagedResultsControl.PAGED_RESULTS_OID,
                )
                val releaseControl = SimplePagedResultsControl.get(searchResult)
                if (releaseControl != null && releaseControl.moreResultsToReturn() &&
                    searchResult.entryCount < maximumNumberOfResults
                ) {
                    extraResponseCookie = releaseControl.cookie
                } else {
                    break
                }
            }

            return builder.build()
        }

        @Throws(GeneralSecurityException::class)
        private fun getDefaultKeystoreSslSocketFactory(): SSLSocketFactory {
            TLSCipherSuiteSelector.setAllowSHA1(true)
            TLSCipherSuiteSelector.setAllowRSAKeyExchange(true)
            return SSLUtil().createSSLSocketFactory()
        }

        private fun isSuitableKeyAndNotMobileId(certificate: ExtendedCertificate): Boolean {
            return (hasKeyEnciphermentUsage(certificate) || hasKeyAgreementUsage(certificate)) &&
                !isServerAuthKeyPurpose(
                    certificate,
                ) &&
                (!isESealType(certificate) || !isTlsClientAuthKeyPurpose(certificate)) &&
                !isMobileIdType(
                    certificate,
                )
        }

        private fun isTlsClientAuthKeyPurpose(certificate: ExtendedCertificate): Boolean {
            return certificate.extendedKeyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_clientAuth)
        }

        private fun hasKeyAgreementUsage(certificate: ExtendedCertificate): Boolean {
            return certificate.keyUsage.hasUsages(KeyUsage.keyAgreement)
        }

        private fun hasKeyEnciphermentUsage(certificate: ExtendedCertificate): Boolean {
            return certificate.keyUsage.hasUsages(KeyUsage.keyEncipherment)
        }

        private fun isServerAuthKeyPurpose(certificate: ExtendedCertificate): Boolean {
            return certificate.extendedKeyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_serverAuth)
        }

        private fun isMobileIdType(certificate: ExtendedCertificate): Boolean {
            return certificate.type == EIDType.MOBILE_ID
        }

        private fun isESealType(certificate: ExtendedCertificate): Boolean {
            return certificate.type == EIDType.E_SEAL
        }
    }
