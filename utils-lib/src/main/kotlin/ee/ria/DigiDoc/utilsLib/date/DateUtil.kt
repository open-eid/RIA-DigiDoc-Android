@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.errorLog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtil {
    private val logTag = "DateUtil"

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

    fun getFormattedDateTime(
        dateTimeString: String,
        isUTC: Boolean,
    ): String {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z", Locale.getDefault())

        return try {
            if (isUTC) {
                inputDateFormat.timeZone = TimeZone.getTimeZone("UTC")
                outputDateFormat.timeZone = TimeZone.getTimeZone("UTC")
                return inputDateFormat
                    .parse(
                        dateTimeString,
                    )?.let {
                        outputDateFormat.format(
                            it,
                        )
                    } ?: ""
            }
            inputDateFormat.timeZone = TimeZone.getTimeZone("UTC")
            return inputDateFormat.parse(dateTimeString)?.let { outputDateFormat.format(it) } ?: ""
        } catch (e: Exception) {
            errorLog(logTag, "Unable to get formatted date time", e)
            ""
        }
    }

    fun dateToCertificateFormat(date: Date): String {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss Z", Locale.getDefault())
        return dateFormat.format(date)
    }
}
