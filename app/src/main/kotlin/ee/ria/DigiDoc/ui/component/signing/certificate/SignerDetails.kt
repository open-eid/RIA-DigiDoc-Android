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

package ee.ria.DigiDoc.ui.component.signing.certificate

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
import androidx.navigation.NavController
import ee.ria.DigiDoc.libdigidoclib.domain.model.SignatureInterface
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignerDetails(
    modifier: Modifier = Modifier,
    signature: SignatureInterface,
    isTimestamp: Boolean,
    signersIssuerName: String,
    tsIssuerName: String,
    ocspIssuerName: String,
    tsSubjectName: String,
    ocspSubjectName: String,
    archiveTimestampCertInfo: List<ArchiveTimestampCertInfo>,
    sharedContainerViewModel: SharedContainerViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    navController: NavController,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = XSPadding)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("signerDetailsView"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SignerDetailItem()
            .signersDetailItems(
                signature = signature,
                isTimestamp = isTimestamp,
                signerIssuerName = signersIssuerName,
                tsIssuerName = tsIssuerName,
                ocspIssuerName = ocspIssuerName,
                tsSubjectName = tsSubjectName,
                ocspSubjectName = ocspSubjectName,
                archiveTimestampCertInfo = archiveTimestampCertInfo,
                sharedContainerViewModel = sharedContainerViewModel,
            ).forEach { navigationItem ->
                if (!navigationItem.value.isNullOrEmpty()) {
                    SignatureDataItem(
                        modifier = modifier,
                        icon = navigationItem.icon,
                        isLink = navigationItem.isLink,
                        testTag = navigationItem.testTag,
                        detailKey = navigationItem.label,
                        detailValue = navigationItem.value,
                        certificate = navigationItem.certificate,
                        contentDescription = navigationItem.contentDescription,
                        formatForAccessibility = navigationItem.formatForAccessibility,
                        onCertificateButtonClick = {
                            navigationItem.certificate?.let {
                                sharedCertificateViewModel.setCertificate(it)
                                navController.navigate(Route.CertificateDetail.route)
                            }
                        },
                    )
                    HorizontalDivider()
                }
            }
    }
}
