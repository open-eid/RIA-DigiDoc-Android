@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

class DateUtilTest {
    @Test
    fun dateUtil_format_successFromEpoch() {
        val dateFormat = DateUtil.dateFormat.format(Date.from(Instant.EPOCH))
        assertEquals("01-01-1970 03:00:00", dateFormat)
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
}
