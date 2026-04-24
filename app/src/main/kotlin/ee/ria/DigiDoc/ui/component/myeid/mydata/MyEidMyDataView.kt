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
            ).forEach { navigationItem ->
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
