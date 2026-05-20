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

package ee.ria.DigiDoc.domain.service

import ee.ria.DigiDoc.common.certificate.CertificateService
import ee.ria.DigiDoc.common.model.ExtendedCertificate
import ee.ria.DigiDoc.domain.model.IdCardData
import ee.ria.DigiDoc.idcard.CertificateType
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.CodeVerificationException
import ee.ria.DigiDoc.idcard.Token
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderException
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdCardServiceImpl
    @Inject
    constructor(
        private val certificateService: CertificateService,
    ) : IdCardService {
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
                val pin2CodeChanged = token.pinChangedFlag(CodeType.PIN2)

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
                    pin2CodeChanged = pin2CodeChanged == 1,
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
    }
