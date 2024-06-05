@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale

object DateUtil {
    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    @Throws(ParseException::class)
    fun signedDateTimeString(signedDateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date: Date? = inputFormat.parse(signedDateString)
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        return date?.let { outputFormat.format(it) } ?: ""
    }

    @Throws(DateTimeParseException::class)
    fun getConfigurationDate(dateString: String): Date {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssX")
        val zonedDateTime = ZonedDateTime.parse(dateString, formatter)
        val instant = zonedDateTime.toInstant()
        return Date.from(instant)
    }

    @Throws(ParseException::class)
    fun stringToDate(dateString: String): Date? {
        return dateFormat.parse(dateString)
    }
}
