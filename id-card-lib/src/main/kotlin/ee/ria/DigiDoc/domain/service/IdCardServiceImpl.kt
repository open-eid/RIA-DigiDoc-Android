@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.common.Constant.SignatureRequest.SIGNATURE_PROFILE_TS
import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.IdCardCertificate
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.libdigidoclib.domain.model.ContainerWrapper
import ee.ria.DigiDoc.libdigidoclib.domain.model.RoleData
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.libdigidocpp.StringVector
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdCardServiceImpl
    @Inject
    constructor(
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

                val authCertificate = IdCardCertificate.create(authenticationCertificateData, certificateService)
                val signCertificate = IdCardCertificate.create(signingCertificateData, certificateService)

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

            if (roleData != null) {
                dataToSign =
                    containerWrapper.prepareSignature(
                        signedContainer, signCertificateData,
                        RoleData(
                            StringVector(TextUtil.removeEmptyStrings(roleData.roles)),
                            roleData.city,
                            roleData.state,
                            roleData.zip,
                            roleData.country,
                        ),
                    )
            } else {
                dataToSign =
                    containerWrapper.prepareSignature(
                        signedContainer,
                        signCertificateData,
                        SIGNATURE_PROFILE_TS,
                    )?.dataToSign()
            }

            val signatureData =
                token.calculateSignature(
                    pin2,
                    dataToSign,
                    idCardData.signCertificate.ellipticCurve,
                )

            containerWrapper.finalizeSignature(signedContainer, signatureData)
            return signedContainer
        }
    }
