/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.cryptolib.repository

import android.content.Context
import com.google.common.collect.ImmutableList
import com.unboundid.asn1.ASN1OctetString
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldap.sdk.LDAPConnectionOptions
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.NameResolver
import com.unboundid.ldap.sdk.ResultCode
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchScope
import com.unboundid.ldap.sdk.controls.SimplePagedResultsControl
import com.unboundid.util.LDAPTestUtils
import com.unboundid.util.ssl.HostNameSSLSocketVerifier
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
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxyAuthenticationException
import ee.ria.DigiDoc.network.proxy.ProxyTunnelSocketFactory
import ee.ria.DigiDoc.network.utils.NetworkUtil
import ee.ria.DigiDoc.network.utils.ProxyUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.bouncycastle.asn1.x509.KeyPurposeId
import org.bouncycastle.asn1.x509.KeyUsage
import java.io.IOException
import java.net.InetAddress
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.SocketFactory
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

@Singleton
class RecipientRepositoryImpl
    @Inject
    constructor(
        private val configurationRepository: ConfigurationRepository,
        private val certificateService: CertificateService,
    ) : RecipientRepository {
        private val logTag = "RecipientRepositoryImpl"
        private val proxyConnectTimeoutMillis = NetworkUtil.DEFAULT_TIMEOUT * 1000

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
                val addressees = ArrayList<Addressee>()
                var count = 0
                for (url in ldapPersonUrls.orEmpty()) {
                    val ldapUrl = url.split("://")[1]
                    val ldapUrlComponents = ldapUrl.split("/")
                    val ldapPersonUrl = ldapUrlComponents[0]
                    val dn = if (ldapUrlComponents.size > 1) ldapUrlComponents[1] else null

                    try {
                        val (addresseesSearch, countSearch) = search(context, ldapPersonUrl, dn, LdapFilter(query))
                        addressees.addAll(addresseesSearch)
                        count += countSearch
                    } catch (e: NoInternetConnectionException) {
                        errorLog(logTag, "Unable to connect to LDAP url: $ldapPersonUrl", e)
                        throw e
                    } catch (ce: CryptoException) {
                        errorLog(logTag, "Unable to get certificates from LDAP url: $ldapPersonUrl", ce)
                        throw CryptoException("Unable to get certificates from LDAP url: $ldapPersonUrl", ce)
                    }
                }
                return Pair(addressees, count)
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
                val tunnelProxy = tunnelProxy(context, url)
                LDAPConnection(
                    socketFactory(tunnelProxy, url),
                    connectionOptions(tunnelProxy != null),
                ).use { connection ->
                    connection.connect(url, LDAP_PORT)
                    return executeSearch(connection, ldapFilter, dn)
                }
            } catch (e: Exception) {
                proxyAuthenticationFailure(e)?.let { throw it }
                if (e is LDAPException && e.resultCode.equals(ResultCode.CONNECT_ERROR)) {
                    throw NoInternetConnectionException(context)
                }
                throw CryptoException("Finding recipients failed", e)
            }
        }

        private fun connectionOptions(isTunnelled: Boolean): LDAPConnectionOptions =
            LDAPConnectionOptions().apply {
                sslSocketVerifier = HostNameSSLSocketVerifier(true)
                if (isTunnelled) {
                    nameResolver = TunnelledNameResolver
                    connectTimeoutMillis = proxyConnectTimeoutMillis * 3
                }
            }

        private fun tunnelProxy(
            context: Context,
            url: String?,
        ): ManualProxy? {
            if (url == null) {
                return null
            }
            return ProxyUtil
                .getProxyValues(
                    ProxyUtil.getProxySetting(context),
                    ProxyUtil.getManualProxySettings(context),
                )?.takeIf { it.host.isNotEmpty() }
        }

        @Throws(GeneralSecurityException::class)
        private fun socketFactory(
            tunnelProxy: ManualProxy?,
            url: String?,
        ): SocketFactory {
            val sslSocketFactory = getDefaultKeystoreSslSocketFactory()
            if (url == null) {
                return sslSocketFactory
            }
            return ProxyTunnelSocketFactory(
                tunnelProxy,
                url,
                LDAP_PORT,
                sslSocketFactory,
                proxyConnectTimeoutMillis,
            )
        }

        private object TunnelledNameResolver : NameResolver() {
            override fun getByName(host: String?): InetAddress = InetAddress.getLoopbackAddress()

            override fun getAllByName(host: String?): Array<InetAddress> = arrayOf(InetAddress.getLoopbackAddress())

            override fun toString(buffer: StringBuilder) {
                buffer.append("TunnelledNameResolver()")
            }
        }

        private fun proxyAuthenticationFailure(throwable: Throwable): ProxyAuthenticationException? =
            generateSequence(throwable) { it.cause }
                .filterIsInstance<ProxyAuthenticationException>()
                .firstOrNull()

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
            val ldapCerts = configurationRepository.getConfiguration()?.ldapCerts ?: listOf()
            if (ldapCerts.isEmpty()) {
                return SSLUtil().createSSLSocketFactory()
            }
            return SSLUtil(ldapTrustManagers(ldapCerts)).createSSLSocketFactory()
        }

        @Throws(GeneralSecurityException::class, IOException::class)
        private fun ldapTrustManagers(ldapCerts: List<String>): Array<TrustManager> {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            ldapCerts.forEachIndexed { index, ldapCert ->
                val certificate =
                    certificateFactory.generateCertificate(
                        Base64.getMimeDecoder().decode(ldapCert).inputStream(),
                    )
                keyStore.setCertificateEntry("ldap-$index", certificate)
            }
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            return trustManagerFactory.trustManagers
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
