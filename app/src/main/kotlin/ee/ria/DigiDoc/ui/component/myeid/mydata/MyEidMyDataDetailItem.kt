@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers

data class MyEidMyDataDetailItem(
    val showTagBadge: Boolean = false,
    @param:StringRes val label: Int = 0,
    val value: String? = null,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun myEidMyDataDetailItems(
        firstname: String?,
        lastname: String?,
        citizenship: String?,
        personalCode: String?,
        dateOfBirth: String?,
        documentNumber: String?,
        validTo: String?,
    ): List<MyEidMyDataDetailItem> =
        listOf(
            MyEidMyDataDetailItem(
                label = R.string.myeid_givennames,
                value = firstname,
                contentDescription =
                    if (firstname != null) {
                        "${stringResource(id = R.string.myeid_givennames)} ${firstname.lowercase()}"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataFirstname",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_surname,
                value = lastname,
                contentDescription =
                    if (lastname != null) {
                        "${stringResource(id = R.string.myeid_surname)}, ${lastname.lowercase()}"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataLastname",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_citizenship,
                value = citizenship,
                contentDescription =
                    if (citizenship != null) {
                        "${stringResource(id = R.string.myeid_citizenship)}, ${citizenship.lowercase()}"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataCitizenship",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_personal_code,
                value = personalCode,
                contentDescription =
                    if (personalCode != null) {
                        "${stringResource(id = R.string.myeid_personal_code)}, ${formatNumbers(personalCode)}"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataPersonalCode",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_date_of_birth,
                value = dateOfBirth,
                contentDescription =
                    if (dateOfBirth != null) {
                        "${stringResource(id = R.string.myeid_date_of_birth)}, $dateOfBirth"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataDateOfBirth",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_document_number,
                value = documentNumber,
                contentDescription =
                    if (documentNumber != null) {
                        "${stringResource(id = R.string.myeid_document_number)}, ${formatNumbers(documentNumber)}"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataDocumentNumber",
            ),
            MyEidMyDataDetailItem(
                showTagBadge = true,
                label = R.string.myeid_valid_to,
                value = validTo,
                contentDescription =
                    if (validTo != null) {
                        "${stringResource(id = R.string.myeid_valid_to)}, $validTo"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataValidTo",
            ),
        )
}
