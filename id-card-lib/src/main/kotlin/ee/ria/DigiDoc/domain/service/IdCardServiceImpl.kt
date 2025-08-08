@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.network.utils.SendDiagnostics
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdCardServiceImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val containerWrapper: ContainerWrapper,
        private val certificateService: CertificateService,
    ) : IdCardService {
        @Throws(Exception::class)
        override suspend fun signContainer(
            token: Token,
            container: SignedContainer,
            pin2: ByteArray,
            roleData: RoleData?,
        ): SignedContainer =
            withContext(IO) {
                sign(container, token, pin2, roleData)
            }

        @Throws(Exception::class)
        override suspend fun data(token: Token): IdCardData =
            withContext(Main) {
                val personalData = token.personalData()
                val authenticationCertificateData =
                    token.certificate(CertificateType.AUTHENTICATION)
                val signingCertificateData = token.certificate(CertificateType.SIGNING)
                val pin1RetryCounter = token.codeRetryCounter(CodeType.PIN1)
                val pin2RetryCounter = token.codeRetryCounter(CodeType.PIN2)
                val pukRetryCounter = token.codeRetryCounter(CodeType.PUK)

                val authCertificate = ExtendedCertificate.create(authenticationCertificateData, certificateService)
                val signCertificate = ExtendedCertificate.create(signingCertificateData, certificateService)

                IdCardData(
                    type = authCertificate.type,
                    personalData = personalData,
                    authCertificate = authCertificate,
                    signCertificate = signCertificate,
                    pin1RetryCount = pin1RetryCounter,
                    pin2RetryCount = pin2RetryCounter,
                    pukRetryCount = pukRetryCounter,
                )
            }

        @Throws(CodeVerificationException::class, SmartCardReaderException::class)
        override suspend fun editPin(
            token: Token,
            codeType: CodeType,
            currentPin: ByteArray,
            newPin: ByteArray,
        ): IdCardData {
            token.changeCode(codeType, currentPin, newPin)
            return data(token)
        }

        @Throws(CodeVerificationException::class, SmartCardReaderException::class)
        override suspend fun unblockAndEditPin(
            token: Token,
            codeType: CodeType,
            currentPuk: ByteArray,
            newPin: ByteArray,
        ): IdCardData {
            token.unblockAndChangeCode(currentPuk, codeType, newPin)
            return data(token)
        }

        @Throws(Exception::class)
        private suspend fun sign(
            signedContainer: SignedContainer,
            token: Token,
            pin2: ByteArray,
            roleData: RoleData?,
        ): SignedContainer {
            val idCardData = data(token)
            val signCertificateData = idCardData.signCertificate.data

            val dataToSign: ByteArray?

            val signer = ExternalSigner(signCertificateData)
            signer.setProfile(SIGNATURE_PROFILE_TS)
            signer.setUserAgent(UserAgentUtil.getUserAgent(context, SendDiagnostics.Devices))

            dataToSign =
                containerWrapper.prepareSignature(
                    signer,
                    signedContainer,
                    signCertificateData,
                    roleData,
                )

            val signatureData =
                token.calculateSignature(
                    pin2,
                    dataToSign,
                    idCardData.signCertificate.ellipticCurve,
                )

            containerWrapper.finalizeSignature(
                signer,
                signedContainer,
                signatureData,
            )
            return signedContainer
        }

        @Throws(Exception::class)
        override fun authenticate(
            token: Token,
            pin1: ByteArray,
            origin: String,
            challenge: String,
        ): Pair<ByteArray, ByteArray> {
            val authCert = token.certificate(CertificateType.AUTHENTICATION)

            val cert =
                java.security.cert.CertificateFactory
                    .getInstance("X.509")
                    .generateCertificate(authCert.inputStream())
            val publicKey = cert.publicKey

            val hashAlg =
                when (publicKey) {
                    is java.security.interfaces.RSAPublicKey ->
                        when (publicKey.modulus.bitLength()) {
                            2048 -> "SHA-256"
                            3072 -> "SHA-384"
                            4096 -> "SHA-512"
                            else -> throw IllegalArgumentException("Unsupported RSA key length")
                        }
                    is java.security.interfaces.ECPublicKey ->
                        when (publicKey.params.curve.field.fieldSize) {
                            256 -> "SHA-256"
                            384 -> "SHA-384"
                            512 -> "SHA-512"
                            else -> throw IllegalArgumentException("Unsupported EC key length")
                        }
                    else -> throw IllegalArgumentException("Unsupported key type")
                }

            val md = MessageDigest.getInstance(hashAlg)
            val originHash = md.digest(origin.toByteArray(Charsets.UTF_8))
            val challengeHash = md.digest(challenge.toByteArray(Charsets.UTF_8))
            val signedData = originHash + challengeHash
            val tbsHash = MessageDigest.getInstance("SHA-384").digest(signedData)
            val signature = token.authenticate(pin1, tbsHash)

            return authCert to signature
        }
    }
