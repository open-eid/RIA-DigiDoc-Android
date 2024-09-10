@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.repository.siva

import android.content.Context
import ee.ria.DigiDoc.libdigidoclib.SignedContainer
import java.io.File

interface SivaRepository {
    fun isSivaConfirmationNeeded(
        context: Context,
        files: List<File>,
    ): Boolean

    suspend fun isTimestampedContainer(
        signedContainer: SignedContainer,
        isSivaConfirmed: Boolean,
    ): Boolean

    suspend fun getTimestampedContainer(
        context: Context,
        parentContainer: SignedContainer,
    ): SignedContainer
}
