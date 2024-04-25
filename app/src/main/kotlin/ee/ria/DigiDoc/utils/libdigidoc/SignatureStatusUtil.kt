@file:Suppress("PackageName")

package ee.ria.DigiDoc.utils.libdigidoc

import android.content.Context
import ee.ria.DigiDoc.libdigidoclib.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface

object SignatureStatusUtil {
    fun getSignatureStatusText(
        context: Context,
        status: ValidatorInterface.Status,
    ): String {
        return when (status) {
            ValidatorInterface.Status.Valid -> context.getString(R.string.signing_container_signature_status_valid)
            ValidatorInterface.Status.Warning ->
                "${context.getString(R.string.signing_container_signature_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_warning)
            ValidatorInterface.Status.NonQSCD ->
                "${context.getString(R.string.signing_container_signature_status_valid)} " +
                    context.getString(R.string.signing_container_signature_status_non_qscd)
            ValidatorInterface.Status.Invalid -> context.getString(R.string.signing_container_signature_status_invalid)
            else -> context.getString(R.string.signing_container_signature_status_unknown)
        }
    }
}
