@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.ExpandableButton
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Red50
import ee.ria.DigiDoc.ui.theme.Red800
import ee.ria.DigiDoc.ui.theme.Yellow50
import ee.ria.DigiDoc.ui.theme.Yellow800
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.SignerDetailViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignerDetailsView(
    navController: NavController,
    modifier: Modifier = Modifier,
    sharedSignatureViewModel: SharedSignatureViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    signerDetailViewModel: SignerDetailViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val signature = sharedSignatureViewModel.signature.value
    val signatureStatus = signature?.validator?.status
    val diagnosticsInfo = signature?.validator?.diagnostics ?: ""
    val isSignatureWithWarning =
        signatureStatus == ValidatorInterface.Status.Warning ||
            signatureStatus == ValidatorInterface.Status.NonQSCD
    val warningText =
        when (signatureStatus) {
            ValidatorInterface.Status.Warning -> {
                if (diagnosticsInfo.contains("Signature digest weak")) {
                    R.string.signature_error_details_reason_weak
                } else {
                    R.string.signature_error_details_reason_warning
                }
            }
            ValidatorInterface.Status.NonQSCD -> R.string.signature_error_details_reason_nonqscd
            ValidatorInterface.Status.Unknown -> R.string.signature_error_details_reason_unknown
            else -> R.string.signature_error_details_invalid_reason
        }.let { stringResource(id = it) }
    val tagBackgroundColor = if (isSignatureWithWarning) Yellow50 else Red50
    val tagContentColor = if (isSignatureWithWarning) Yellow800 else Red800
    val signersIssuerName =
        signerDetailViewModel.getIssuerCommonName(
            signature?.signingCertificateDer?.x509Certificate(),
        )
    val tsIssuerName =
        signerDetailViewModel.getIssuerCommonName(
            signature?.timeStampCertificateDer?.x509Certificate(),
        )
    val ocspIssuerName =
        signerDetailViewModel.getIssuerCommonName(
            signature?.ocspCertificateDer?.x509Certificate(),
        )

    val tsSubjectName =
        signerDetailViewModel.getSubjectCommonName(
            signature?.timeStampCertificateDer?.x509Certificate(),
        )
    val ocspSubjectName =
        signerDetailViewModel.getSubjectCommonName(
            signature?.ocspCertificateDer?.x509Certificate(),
        )

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    Scaffold { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    },
        ) {
            Column {
                TopBar(
                    modifier =
                        modifier
                            .testTag("appBar"),
                    title = R.string.signature_details_title,
                    onBackButtonClick = {
                        handleBackButtonClick(navController, sharedSignatureViewModel)
                    },
                )
                if (signature != null) {
                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .testTag("scrollView"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (signatureStatus != ValidatorInterface.Status.Valid) {
                            Column(
                                modifier =
                                    modifier
                                        .padding(screenViewLargePadding)
                                        .testTag("signersCertificateErrorContainer"),
                            ) {
                                TagBadge(
                                    modifier =
                                        modifier
                                            .testTag("signersCertificateErrorTitle"),
                                    text = stringResource(id = R.string.signature_error_details_title),
                                    contentColor = tagContentColor,
                                    backgroundColor = tagBackgroundColor,
                                )

                                DynamicText(
                                    modifier =
                                        modifier
                                            .padding(
                                                horizontal = itemSpacingPadding,
                                                vertical = screenViewLargePadding,
                                            )
                                            .testTag("signersCertificateErrorDetails"),
                                    text = warningText,
                                )

                                ExpandableButton(
                                    modifier = modifier,
                                    title = R.string.signature_error_details_button,
                                    detailText = signature.validator.diagnostics,
                                    contentDescription =
                                        stringResource(
                                            id = R.string.signature_error_details_button_accessibility,
                                        ),
                                )
                            }
                        }
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
                                    CertificateDataItem(
                                        modifier = modifier,
                                        detailKey = navigationItem.label,
                                        detailValue = navigationItem.value,
                                        certificate = navigationItem.certificate,
                                        contentDescription = navigationItem.contentDescription,
                                        formatForAccessibility = navigationItem.formatForAccessibility,
                                        onCertificateButtonClick = {
                                            navigationItem.certificate?.let {
                                                sharedCertificateViewModel.setCertificate(
                                                    it,
                                                )
                                                navController.navigate(
                                                    Route.CertificateDetail.route,
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

private fun handleBackButtonClick(
    navController: NavController,
    sharedSignatureViewModel: SharedSignatureViewModel,
) {
    sharedSignatureViewModel.resetSignature()
    navController.navigateUp()
}
