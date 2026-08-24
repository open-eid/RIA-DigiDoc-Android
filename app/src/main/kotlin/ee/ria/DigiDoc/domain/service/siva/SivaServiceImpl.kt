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

package ee.ria.DigiDoc.domain.service.siva

import android.content.Context
import ee.ria.DigiDoc.common.Constant.ASICS_MIMETYPE
import ee.ria.DigiDoc.common.Constant.PDF_MIMETYPE
import ee.ria.DigiDoc.common.Constant.SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import ee.ria.DigiDoc.utilsLib.extensions.isCades
import ee.ria.DigiDoc.utilsLib.extensions.isSignedPDF
import ee.ria.DigiDoc.utilsLib.extensions.isXades
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.utilsLib.mimetype.MimeTypeResolver
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SivaServiceImpl
    @Inject
    constructor(
        private val mimeTypeResolver: MimeTypeResolver,
    ) : SivaService {
        private val logTag = "SivaService"

        override fun isSivaConfirmationNeeded(
            context: Context,
            files: List<File>,
        ): Boolean {
            if (files.size != 1) {
                return false
            }

            val file = files.first()
            val mimetype = mimeTypeResolver.mimeType(file)

            return SEND_SIVA_CONTAINER_NOTIFICATION_MIMETYPES.contains(mimetype) &&
                !file.isXades(context) ||
                (PDF_MIMETYPE == mimetype && file.isSignedPDF(context)) ||
                file.isCades(context)
        }

        override suspend fun isTimestampedContainer(signedContainer: SignedContainer): Boolean =
            signedContainer.getDataFiles().size == 1 &&
                signedContainer.containerMimetype().equals(ASICS_MIMETYPE) &&
                signedContainer.getSignatures().firstOrNull()?.profile == "TimeStampToken"

        override suspend fun getTimestampedContainer(
            context: Context,
            parentContainer: SignedContainer,
            isSivaConfirmed: Boolean,
        ): SignedContainer {
            try {
                val nestedContainer = parentContainer.getNestedTimestampedContainer(isSivaConfirmed)
                return SignedContainer(
                    context,
                    nestedContainer?.rawContainer(),
                    parentContainer.getContainerFile(),
                    parentContainer.isExistingContainer(),
                    parentContainer.getSignatures(),
                )
            } catch (ex: Exception) {
                errorLog(logTag, "Unable to open timestamped container", ex)
            }

            return parentContainer
        }
    }
