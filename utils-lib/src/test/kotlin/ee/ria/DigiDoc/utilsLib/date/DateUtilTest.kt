@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import ee.ria.DigiDoc.utilsLib.date.DateUtil.getFormattedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DateUtilTest {
    @Test
    fun dateUtil_format_successFromEpoch() {
        val dateFormat = DateUtil.dateFormat.format(Date.from(Instant.EPOCH))
        assertEquals("01-01-1970 00:00:00", dateFormat)
    }

    @Test
    fun dateUtil_format_successFromDateString() {
        val inputDateString = "1970-01-01T03:00:00Z"
        val expectedOutputDateString = "01.01.1970 03:00:00"

        val outputDateString =
            try {
                DateUtil.signedDateTimeString(inputDateString)
            } catch (e: ParseException) {
                e.printStackTrace()
                fail("Signed date must succeed with valid date string")
            }

        assertEquals(expectedOutputDateString, outputDateString)
    }

    @Test
    fun dateUtil_format_returnEmptyStringWithInvalidDateFormat() {
        val inputDateString = "invalid_date_format"

        val outputDateString =
            try {
                DateUtil.signedDateTimeString(inputDateString)
            } catch (e: ParseException) {
                e.printStackTrace()
                ""
            }

        assertEquals("", outputDateString)
    }

    @Test
    fun dateUtil_format_returnEmptyStringWithEmptyDateString() {
        val outputDateString =
            try {
                DateUtil.signedDateTimeString("")
            } catch (e: ParseException) {
                e.printStackTrace()
                ""
            }

        assertEquals("", outputDateString)
    }

    @Test
    @Throws(ParseException::class)
    fun dateUtil_signedDateTimeString_returnEmptyStringWhenDateNull() {
        val inputDateFormatMock = mock(SimpleDateFormat::class.java)
        val outputDateFormatMock = mock(SimpleDateFormat::class.java)
        val signedDateString = "1970-01-01T00:00:00Z"
        val nullDate: Date? = null

        `when`(inputDateFormatMock.parse(signedDateString)).thenReturn(nullDate)

        val result: String = DateUtil.signedDateTimeString(signedDateString, inputDateFormatMock, outputDateFormatMock)

        assertEquals("", result)
        verify(inputDateFormatMock, times(1)).parse(signedDateString)
        verify(outputDateFormatMock, never()).format(any(Date::class.java))
    }

    @Test
    fun dateUtil_stringToDate_success() {
        val dateString = "01-01-1970 03:00:00"
        val expectedDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(dateString)
        val actualDate = DateUtil.stringToDate(dateString)
        assertEquals(expectedDate, actualDate)
    }

    @Test(expected = ParseException::class)
    fun dateUtil_stringToDate_throwParseException() {
        val dateString = "invalid date string"
        DateUtil.stringToDate(dateString)
    }

    @Test(expected = ParseException::class)
    fun dateUtil_stringToDate_throwParseExceptionWithEmptyDate() {
        val dateString = ""
        DateUtil.stringToDate(dateString)
    }

    @Test
    fun dateUtil_getConfigurationDate_success() {
        val dateString = "19700101000000Z"
        val expectedDateTime = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        val expectedDate = Date.from(expectedDateTime.toInstant())

        val result: Date = DateUtil.getConfigurationDate(dateString)

        assertEquals(expectedDate, result)
    }

    @Test
    fun dateUtil_getFormattedDateTime_successWithCurrentDateObject() {
        val result = DateUtil.dateToCertificateFormat(Date())
        assert(result.isNotEmpty())
    }

    @Test
    fun dateUtil_getFormattedDateTime_successWithUTC() {
        val inputDate = "1970-01-01T00:00:00ZZ"
        val expectedOutput = "01.01.1970 00:00:00 +0000"
        val result = getFormattedDateTime(inputDate, true)
        assertEquals(expectedOutput, result)
    }

    @Test
    fun dateUtil_getFormattedDateTime_successWithNotUTC() {
        val inputDate = "1970-01-01T00:00:00Z"
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss Z", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val expectedOutput =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                .apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(inputDate)
                ?.let {
                    sdf.format(
                        it,
                    )
                }
        val result = getFormattedDateTime(inputDate, false)
        assertEquals(expectedOutput, result)
    }

    @Test(expected = DateTimeParseException::class)
    fun dateUtil_getConfigurationDate_throwExceptionWithInvalidDateString() {
        val invalidDateString = "19700101"
        DateUtil.getConfigurationDate(invalidDateString)
    }

    @Test
    fun dateUtil_getFormattedDateTime_returnEmptyStringWhenDateParseNull() {
        val inputDateFormatMock = mock(SimpleDateFormat::class.java)
        `when`(inputDateFormatMock.parse(anyString())).thenReturn(null)

        val result: String = getFormattedDateTime("1970-01-01T00:00:00Z", true, inputDateFormatMock)

        assertEquals("", result)

        verify(inputDateFormatMock, times(1)).parse(anyString())
    }

    @Test
    fun dateUtil_getFormattedDateTime_returnEmptyStringWhenDateNotUTCAndParseUnsuccessful() {
        val inputDateFormatMock = mock(SimpleDateFormat::class.java)
        `when`(inputDateFormatMock.parse(anyString())).thenReturn(null)

        val result = getFormattedDateTime("1970-01-01T00:00:00Z", false, inputDateFormatMock)

        assertEquals("", result)

        verify(inputDateFormatMock, times(1)).parse(anyString())
    }

    @Test
    fun dateUtil_dateToCertificateFormat_success() {
        val gmtTimeZone = TimeZone.getTimeZone("GMT")
        TimeZone.setDefault(gmtTimeZone)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("1970-01-01")
        val expectedOutput = "Thursday, 1 January 1970 00:00:00 +0000"
        val result = date?.let { DateUtil.dateToCertificateFormat(it, Locale.US) }
        assertEquals(expectedOutput, result)
    }

    @Test
    fun dateUtil_getFormattedDateTime_returnEmptyStringWithInvalidDate() {
        val inputDate = "invalid-date"
        val result = getFormattedDateTime(inputDate, true)
        assertEquals("", result)
    }
}
