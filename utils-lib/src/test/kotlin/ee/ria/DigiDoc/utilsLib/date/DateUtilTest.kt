@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.Date

class DateUtilTest {
    @Test
    fun dateFormatFromEpoch() {
        val dateFormat = DateUtil.dateFormat.format(Date.from(Instant.EPOCH))
        assertEquals("01-01-1970 03:00:00", dateFormat)
    }
}
