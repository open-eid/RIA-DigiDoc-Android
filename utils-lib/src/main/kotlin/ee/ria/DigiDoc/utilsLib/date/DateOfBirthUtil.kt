@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.date

import java.time.DateTimeException
import java.time.LocalDate

object DateOfBirthUtil {
    @Throws(DateTimeException::class)
    fun parseDateOfBirth(personalCode: String): LocalDate {
        val firstNumber = Character.getNumericValue(personalCode[0])
        val century =
            when (firstNumber) {
                1, 2 -> 1800
                3, 4 -> 1900
                5, 6 -> 2000
                7, 8 -> 2100
                else -> throw IllegalArgumentException("Invalid personal code")
            }
        val year = personalCode.substring(1, 3).toInt() + century
        val month = personalCode.substring(3, 5).toInt()
        val day = personalCode.substring(5, 7).toInt()

        return LocalDate.of(year, month, day)
    }
}
