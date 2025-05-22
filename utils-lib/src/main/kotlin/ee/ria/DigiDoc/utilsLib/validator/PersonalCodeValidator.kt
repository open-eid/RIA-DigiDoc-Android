@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.validator

import ee.ria.DigiDoc.utilsLib.date.DateOfBirthUtil.parseDateOfBirth
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.apache.commons.lang3.StringUtils
import java.time.DateTimeException
import java.time.LocalDate

object PersonalCodeValidator {
    private val LOG_TAG = javaClass.simpleName

    fun isPersonalCodeValid(personalCode: String): Boolean {
        return (
            isPersonalCodeLengthValid(personalCode) && isBirthDateValid(personalCode) &&
                isChecksumValid(personalCode)
        ) ||
            (isPersonalCodeLengthValid(personalCode) && isMobileIdTestCode(personalCode))
    }

    private fun isPersonalCodeNumeric(personalCode: String): Boolean {
        return StringUtils.isNumeric(personalCode)
    }

    private fun isBirthDateValid(personalCode: String): Boolean {
        if (!isPersonalCodeNumeric(personalCode)) {
            return false
        }

        try {
            val dateOfBirth: LocalDate = parseDateOfBirth(personalCode)
            return dateOfBirth.isBefore(LocalDate.now())
        } catch (e: DateTimeException) {
            errorLog(LOG_TAG, "Invalid personal code birth of date", e)
            return false
        }
    }

    private fun isChecksumValid(personalCode: String): Boolean {
        var sum1 = 0
        var sum2 = 0

        var i = 0
        var pos1 = 1
        var pos2 = 3

        while (i < 10) {
            var personalCodeNumber = 0
            try {
                personalCodeNumber = personalCode.substring(i, i + 1).toInt()
            } catch (nfe: NumberFormatException) {
                errorLog(LOG_TAG, "Unable to parse personal code number", nfe)
            }
            sum1 += personalCodeNumber * pos1
            sum2 += personalCodeNumber * pos2
            pos1 = if (pos1 == 9) 1 else pos1 + 1
            pos2 = if (pos2 == 9) 1 else pos2 + 1

            i += 1
        }

        var result = sum1 % 11
        if (result >= 10) {
            result = sum2 % 11

            if (result >= 10) {
                result = 0
            }
        }

        val lastNumber = Character.getNumericValue(personalCode[personalCode.length - 1])

        return lastNumber == result
    }

    private fun isPersonalCodeLengthValid(personalCode: String): Boolean {
        return personalCode.length == 11
    }

    private fun isMobileIdTestCode(personalCode: String): Boolean {
        val testNumbers =
            listOf(
                "14212128020",
                "14212128021",
                "14212128022",
                "14212128023",
                "14212128024",
                "14212128025",
                "14212128026",
                "14212128027",
                "38002240211",
                "14212128029",
            )

        return testNumbers.contains(personalCode)
    }
}
