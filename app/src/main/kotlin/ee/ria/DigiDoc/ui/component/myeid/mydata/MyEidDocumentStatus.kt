@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import android.content.Context
import ee.ria.DigiDoc.R

enum class MyEidDocumentStatus {
    VALID,
    EXPIRED,
    UNKNOWN,
}

fun MyEidDocumentStatus.getLocalized(context: Context): String =
    when (this) {
        MyEidDocumentStatus.VALID -> context.getString(R.string.myeid_status_valid)
        MyEidDocumentStatus.EXPIRED -> context.getString(R.string.myeid_status_expired)
        MyEidDocumentStatus.UNKNOWN -> context.getString(R.string.myeid_status_unknown)
    }
