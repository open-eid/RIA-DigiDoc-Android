@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtil {
    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    fun signedDateTimeString(signedDateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date: Date? = inputFormat.parse(signedDateString)
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        return date?.let { outputFormat.format(it) } ?: ""
    }
}
