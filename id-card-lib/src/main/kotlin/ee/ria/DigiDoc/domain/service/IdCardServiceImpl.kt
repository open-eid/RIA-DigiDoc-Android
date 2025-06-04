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
import ee.ria.DigiDoc.network.utils.UserAgentUtil
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import ee.ria.libdigidocpp.ExternalSigner
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
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
        ): Boolean {
            token.changeCode(codeType, currentPin, newPin)
            return true
        }

        @Throws(CodeVerificationException::class, SmartCardReaderException::class)
        override suspend fun unblockAndEditPin(
            token: Token,
            codeType: CodeType,
            currentPuk: ByteArray,
            newPin: ByteArray,
        ): Boolean {
            token.unblockAndChangeCode(currentPuk, codeType, newPin)
            return true
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
            signer.setUserAgent(UserAgentUtil.getUserAgent(context, true, false))

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
    }
