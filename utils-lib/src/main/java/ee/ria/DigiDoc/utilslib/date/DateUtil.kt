@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilslib.date

import java.text.SimpleDateFormat
import java.util.Locale

object DateUtil {
    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
}
