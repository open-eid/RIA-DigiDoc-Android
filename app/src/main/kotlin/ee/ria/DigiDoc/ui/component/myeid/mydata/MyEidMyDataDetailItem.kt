@file:Suppress("PackageName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ee.ria.DigiDoc.R

data class MyEidMyDataDetailItem(
    val showTagBadge: Boolean = false,
    @StringRes val label: Int = 0,
    val value: String? = null,
    val contentDescription: String = "",
    val testTag: String = "",
) {
    @Composable
    fun myEidMyDataDetailItems(
        firstname: String?,
        lastname: String?,
        citizenship: String?,
        documentNumber: String?,
        validTo: String?,
    ): List<MyEidMyDataDetailItem> =
        listOf(
            MyEidMyDataDetailItem(
                label = R.string.myeid_firstname,
                value = firstname,
                contentDescription =
                    if (value != null) {
                        "${stringResource(id = R.string.myeid_firstname)} $value"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataFirstname",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_lastname,
                value = lastname,
                contentDescription =
                    if (value != null) {
                        "${stringResource(id = R.string.myeid_lastname)}, $value"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataLastname",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_citizenship,
                value = citizenship,
                contentDescription =
                    if (value != null) {
                        "${stringResource(id = R.string.myeid_citizenship)}, $value"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataCitizenship",
            ),
            MyEidMyDataDetailItem(
                label = R.string.myeid_document_number,
                value = documentNumber,
                contentDescription =
                    if (value != null) {
                        "${stringResource(id = R.string.myeid_document_number)}, $value"
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
                    if (value != null) {
                        "${stringResource(id = R.string.myeid_valid_to)}, $value"
                    } else {
                        ""
                    },
                testTag = "myEidMyDataValidTo",
            ),
        )
}
