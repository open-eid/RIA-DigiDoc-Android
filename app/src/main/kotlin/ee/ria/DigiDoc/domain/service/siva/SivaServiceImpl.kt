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
import kotlinx.coroutines.Dispatchers.Main
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
                signedContainer.getSignatures(Main).first().profile == "TimeStampToken"

        override suspend fun getTimestampedContainer(
            context: Context,
            parentContainer: SignedContainer,
        ): SignedContainer {
            try {
                val nestedContainer = parentContainer.getNestedTimestampedContainer()
                return SignedContainer(
                    context,
                    nestedContainer?.rawContainer(),
                    parentContainer.getContainerFile(),
                    parentContainer.isExistingContainer(),
                    parentContainer.getSignatures(Main),
                )
            } catch (ex: Exception) {
                errorLog(logTag, "Unable to open timestamped container", ex)
            }

            return parentContainer
        }
    }
