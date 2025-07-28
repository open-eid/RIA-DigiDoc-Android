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
    signersIssuerName: String,
    tsIssuerName: String,
    ocspIssuerName: String,
    tsSubjectName: String,
    ocspSubjectName: String,
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
                signerIssuerName = signersIssuerName,
                tsIssuerName = tsIssuerName,
                ocspIssuerName = ocspIssuerName,
                tsSubjectName = tsSubjectName,
                ocspSubjectName = ocspSubjectName,
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
