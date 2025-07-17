@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.crypto.recipient

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
import ee.ria.DigiDoc.cryptolib.Addressee
import ee.ria.DigiDoc.ui.component.signing.certificate.SignatureDataItem
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RecipientDetails(
    modifier: Modifier = Modifier,
    recipient: Addressee,
    recipientFormattedName: String,
    recipientIssuerName: String,
    recipientConcatKDFAlgorithmURI: String,
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
        RecipientDetailItem()
            .recipientDetailItems(
                recipient = recipient,
                recipientFormattedName = recipientFormattedName,
                recipientIssuerName = recipientIssuerName,
                recipientConcatKDFAlgorithmURI = recipientConcatKDFAlgorithmURI,
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
