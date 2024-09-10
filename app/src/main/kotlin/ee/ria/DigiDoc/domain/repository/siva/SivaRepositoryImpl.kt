@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository.siva

import android.content.Context
import ee.ria.DigiDoc.domain.service.siva.SivaService
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SivaRepositoryImpl
    @Inject
    constructor(
        private val sivaService: SivaService,
    ) : SivaRepository {
        override fun isSivaConfirmationNeeded(
            context: Context,
            files: List<File>,
        ): Boolean = sivaService.isSivaConfirmationNeeded(context, files)

        override suspend fun isTimestampedContainer(
            signedContainer: SignedContainer,
            isSivaConfirmed: Boolean,
        ): Boolean = sivaService.isTimestampedContainer(signedContainer, isSivaConfirmed)

        override suspend fun getTimestampedContainer(
            context: Context,
            parentContainer: SignedContainer,
        ): SignedContainer = sivaService.getTimestampedContainer(context, parentContainer)
    }
