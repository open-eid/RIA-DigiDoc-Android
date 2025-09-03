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
import ee.ria.DigiDoc.cryptolib.ldap.LdapFilter
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
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
        private val logTag = "RecipientRepositoryImpl"

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        override suspend fun find(
            context: Context,
            query: String,
        ): Pair<List<Addressee>, Int> {
            var certs: Pair<List<Addressee>, Int> = Pair(listOf(), 0)
            withContext(IO) {
                val escapedQuery =
                    query
                        .replace("\\", "\\5c")
                        .replace("(", "\\28")
                        .replace(")", "\\29")
                        .replace("*", "\\2a")

                certs =
                    try {
                        findCertificates(context, escapedQuery)
                    } catch (e: NoInternetConnectionException) {
                        throw e
                    } catch (ce: CryptoException) {
                        errorLog(logTag, "Unable to get certificates from LDAP", ce)
                        Pair(listOf(), 0)
                    }
            }

            return certs
        }

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        private fun findCertificates(
            context: Context,
            query: String,
        ): Pair<List<Addressee>, Int> {
            val configurationProvider = configurationRepository.getConfiguration()

            val ldapFilter = LdapFilter(query)
            if (ldapFilter.isPersonalCode(query)) {
                val ldapPersonUrls = configurationProvider?.ldapPersonUrls
                for (url in ldapPersonUrls.orEmpty()) {
                    val ldapUrl = url.split("://")[1]
                    val ldapUrlComponents = ldapUrl.split("/")
                    val ldapPersonUrl = ldapUrlComponents[0]
                    val dn = if (ldapUrlComponents.size > 1) ldapUrlComponents[1] else null

                    try {
                        val (addressees, count) = search(context, ldapPersonUrl, dn, LdapFilter(query))
                        if (addressees.isNotEmpty()) {
                            return Pair(addressees, count)
                        }
                    } catch (e: NoInternetConnectionException) {
                        errorLog(logTag, "Unable to connect to LDAP url: $ldapPersonUrl", e)
                        throw e
                    } catch (ce: CryptoException) {
                        errorLog(logTag, "Unable to get certificates from LDAP url: $ldapPersonUrl", ce)
                        throw CryptoException("Unable to get certificates from LDAP url: $ldapPersonUrl", ce)
                    }
                }
                return Pair(listOf(), 0)
            } else {
                val ldapCorpUrl = configurationProvider?.ldapCorpUrl?.split("://")[1]
                return search(context, ldapCorpUrl, null, LdapFilter(query))
            }
        }

        @Throws(CryptoException::class, NoInternetConnectionException::class)
        private fun search(
            context: Context,
            url: String?,
            dn: String?,
            ldapFilter: LdapFilter,
        ): Pair<List<Addressee>, Int> {
            try {
                LDAPConnection(getDefaultKeystoreSslSocketFactory()).use { connection ->
                    connection.connect(url, LDAP_PORT)
                    return executeSearch(connection, ldapFilter, dn)
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
            dn: String?,
        ): Pair<List<Addressee>, Int> {
            val maximumNumberOfResults = 50
            val fullDN = BASE_DN + dn ?.let { ",$it" }.orEmpty()
            val searchRequest =
                SearchRequest(
                    fullDN,
                    SearchScope.SUB,
                    ldapFilter.filterString(),
                    CERT_BINARY_ATTR,
                )
            var extraResponseCookie: ASN1OctetString? = null
            val builder: ImmutableList.Builder<Addressee> = ImmutableList.builder<Addressee>()
            var resultCount = 0
            while (true) {
                searchRequest.setControls(
                    SimplePagedResultsControl(
                        maximumNumberOfResults,
                        extraResponseCookie,
                    ),
                )
                val searchResult = connection.search(searchRequest)
                val searchEntries = searchResult.getSearchEntries()
                resultCount += searchEntries.size
                for (entry in searchEntries) {
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
                if (releaseControl != null &&
                    releaseControl.moreResultsToReturn() &&
                    searchResult.entryCount < maximumNumberOfResults
                ) {
                    extraResponseCookie = releaseControl.cookie
                } else {
                    break
                }
            }

            return Pair(builder.build(), resultCount)
        }

        @Throws(GeneralSecurityException::class)
        private fun getDefaultKeystoreSslSocketFactory(): SSLSocketFactory {
            TLSCipherSuiteSelector.setAllowSHA1(true)
            TLSCipherSuiteSelector.setAllowRSAKeyExchange(true)
            return SSLUtil().createSSLSocketFactory()
        }

        private fun isSuitableKeyAndNotMobileId(certificate: ExtendedCertificate): Boolean =
            (hasKeyEnciphermentUsage(certificate) || hasKeyAgreementUsage(certificate)) &&
                !isServerAuthKeyPurpose(certificate) &&
                (!isESealType(certificate) || !isTlsClientAuthKeyPurpose(certificate)) &&
                !isMobileIdType(certificate)

        private fun isTlsClientAuthKeyPurpose(certificate: ExtendedCertificate): Boolean =
            certificate.extendedKeyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_clientAuth)

        private fun hasKeyAgreementUsage(certificate: ExtendedCertificate): Boolean =
            certificate.keyUsage.hasUsages(KeyUsage.keyAgreement)

        private fun hasKeyEnciphermentUsage(certificate: ExtendedCertificate): Boolean =
            certificate.keyUsage.hasUsages(KeyUsage.keyEncipherment)

        private fun isServerAuthKeyPurpose(certificate: ExtendedCertificate): Boolean =
            certificate.extendedKeyUsage.hasKeyPurposeId(KeyPurposeId.id_kp_serverAuth)

        private fun isMobileIdType(certificate: ExtendedCertificate): Boolean = certificate.type == EIDType.MOBILE_ID

        private fun isESealType(certificate: ExtendedCertificate): Boolean = certificate.type == EIDType.E_SEAL
    }
