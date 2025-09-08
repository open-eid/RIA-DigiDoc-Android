@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model

import java.io.File

sealed class ContainerFileOpeningResult {
    data class OpenNestedFile(
        val file: File,
        val needsSivaDialog: Boolean,
    ) : ContainerFileOpeningResult()

    data class OpenWithFile(
        val file: File,
    ) : ContainerFileOpeningResult()

    data class Error(
        val throwable: Throwable,
    ) : ContainerFileOpeningResult()
}
