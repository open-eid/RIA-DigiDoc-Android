@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.utilsLib.date.DateUtil.isBefore

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidMyDataView(
    modifier: Modifier = Modifier,
    firstname: String,
    lastname: String,
    citizenship: String,
    personalCode: String,
    dateOfBirth: String,
    documentNumber: String,
    validTo: String,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = XSPadding)
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MyEidMyDataDetailItem()
            .myEidMyDataDetailItems(
                firstname = firstname,
                lastname = lastname,
                citizenship = citizenship,
                personalCode = personalCode,
                dateOfBirth = dateOfBirth,
                documentNumber = documentNumber,
                validTo = validTo,
            )
            .forEach { navigationItem ->
                if (!navigationItem.value.isNullOrEmpty()) {
                    MyEidMyDataItem(
                        modifier = modifier,
                        testTag = navigationItem.testTag,
                        detailKey = navigationItem.label,
                        detailValue = navigationItem.value,
                        contentDescription = navigationItem.contentDescription,
                        showTagBadge = navigationItem.showTagBadge,
                        status =
                            when (isBefore(validTo)) {
                                true -> MyEidDocumentStatus.EXPIRED
                                false -> MyEidDocumentStatus.VALID
                                null -> MyEidDocumentStatus.UNKNOWN
                            },
                    )
                    HorizontalDivider()
                }
            }
    }
}
