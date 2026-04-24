/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName")

package ee.ria.DigiDoc.utilsLib.validator

import ee.ria.DigiDoc.common.Constant.MAXIMUM_LATVIAN_PERSONAL_CODE_LENGTH
import ee.ria.DigiDoc.utilsLib.date.DateOfBirthUtil.parseDateOfBirth
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import org.apache.commons.lang3.StringUtils
import java.time.DateTimeException
import java.time.LocalDate

object PersonalCodeValidator {
    private val LOG_TAG = javaClass.simpleName

    fun isPersonalCodeValid(personalCode: String): Boolean =
        (
            isPersonalCodeLengthValid(personalCode) &&
                isBirthDateValid(personalCode) &&
                isChecksumValid(personalCode)
        ) ||
            (isPersonalCodeLengthValid(personalCode) && isMobileIdTestCode(personalCode)) ||
            isLatvianPersonalCodeValid(personalCode)

    private fun isLatvianPersonalCodeValid(personalCode: String): Boolean {
        val regex = "^\\d{6}-\\d{5}$"

        return personalCode.isNotEmpty() &&
            personalCode.length == MAXIMUM_LATVIAN_PERSONAL_CODE_LENGTH &&
            personalCode.matches(regex.toRegex())
    }

    private fun isPersonalCodeNumeric(personalCode: String): Boolean = StringUtils.isNumeric(personalCode)

    private fun isBirthDateValid(personalCode: String): Boolean {
        if (!isPersonalCodeNumeric(personalCode)) {
            return false
        }

        try {
            val dateOfBirth: LocalDate = parseDateOfBirth(personalCode)
            return dateOfBirth.isBefore(LocalDate.now())
        } catch (dte: DateTimeException) {
            errorLog(LOG_TAG, "Invalid personal code birth of date", dte)
            return false
        } catch (iae: IllegalArgumentException) {
            errorLog(LOG_TAG, "Invalid personal code", iae)
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

    private fun isPersonalCodeLengthValid(personalCode: String): Boolean = personalCode.length == 11

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
