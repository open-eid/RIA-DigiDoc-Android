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
