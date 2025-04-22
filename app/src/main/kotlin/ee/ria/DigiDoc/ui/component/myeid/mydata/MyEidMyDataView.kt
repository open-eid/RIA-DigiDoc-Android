@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding

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
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("myEidMyDataView"),
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
                        status = MyEidDocumentStatus.VALID,
                    )
                    HorizontalDivider()
                }
            }
    }
}
