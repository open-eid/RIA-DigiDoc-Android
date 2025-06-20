@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class FormattedDateTime(val date: String, val time: String)

object DateUtil {
    private val logTag = javaClass.simpleName

    val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateTimeFormatWithDots: SimpleDateFormat
        get() = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    val dateTimeFormat: SimpleDateFormat
        get() = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    @Throws(ParseException::class)
    fun formattedDateTime(
        signedDateString: String,
        inputFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
        outputDateFormat: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
        outputTimeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault()),
    ): FormattedDateTime {
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date: Date? = inputFormat.parse(signedDateString)

        outputDateFormat.timeZone = TimeZone.getDefault()
        outputTimeFormat.timeZone = TimeZone.getDefault()

        return if (date != null) {
            FormattedDateTime(
                date = outputDateFormat.format(date),
                time = outputTimeFormat.format(date),
            )
        } else {
            FormattedDateTime("", "")
        }
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
        return dateTimeFormat.parse(dateString)
    }

    fun getFormattedDateTime(
        dateTimeString: String,
        isUTC: Boolean,
        inputDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
        outputDateFormat: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z", Locale.getDefault()),
    ): String {
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
            ""
        }
    }

    fun dateToCertificateFormat(
        date: Date,
        locale: Locale = Locale.getDefault(),
    ): String {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy HH:mm:ss Z", locale)
        return dateFormat.format(date)
    }

    fun isBefore(
        dateString: String,
        format: String = "dd.MM.yyyy",
    ): Boolean? {
        return try {
            val formatter = DateTimeFormatter.ofPattern(format)
            val expiryDate = LocalDate.parse(dateString, formatter)
            val today = LocalDate.now()
            expiryDate.isBefore(today)
        } catch (dtpe: DateTimeParseException) {
            errorLog(logTag, "Unable to parse date $dateString", dtpe)
            null
        }
    }
}
