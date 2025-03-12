@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.ExpandableButton
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.signing.ColoredSignedStatusText
import ee.ria.DigiDoc.ui.component.signing.StyledNameText
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.border
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Red50
import ee.ria.DigiDoc.ui.theme.Red800
import ee.ria.DigiDoc.ui.theme.Yellow50
import ee.ria.DigiDoc.ui.theme.Yellow800
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getSignatureStatusText
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.SignerDetailViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignerDetailsView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedSignatureViewModel: SharedSignatureViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    signerDetailViewModel: SignerDetailViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

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

    val selectedSignedContainerTabIndex = rememberSaveable { mutableIntStateOf(0) }

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    if (signature != null) {
        Scaffold(
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("signatureDetailsScreen"),
            topBar = {
                TopBar(
                    modifier = modifier,
                    title = R.string.signature_details_title,
                    onLeftButtonClick = {
                        handleBackButtonClick(navController, sharedSignatureViewModel)
                    },
                    onRightSecondaryButtonClick = {
                        isSettingsMenuBottomSheetVisible.value = true
                    },
                )
            },
        ) { innerPadding ->
            SettingsMenuBottomSheet(
                navController = navController,
                isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
            )
            Surface(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.primary)
                        .focusGroup()
                        .semantics {
                            testTagsAsResourceId = true
                        },
            ) {
                Column(
                    modifier =
                        modifier
                            .padding(SPadding)
                            .testTag("signersCertificateContainer"),
                ) {
                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }
                                .focusGroup()
                                .testTag("signatureDetailsSignatureRow"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_signature),
                            contentDescription = null,
                            modifier =
                                modifier
                                    .size(iconSizeXXS)
                                    .focusable(false)
                                    .testTag("signatureUpdateListSignatureType"),
                        )
                        Spacer(modifier = modifier.width(SPadding))

                        val nameText = formatName(signature.name)
                        val statusText =
                            getSignatureStatusText(
                                LocalContext.current,
                                signature.validator.status,
                            )
                        Column(
                            modifier =
                                modifier
                                    .semantics(mergeDescendants = true) {
                                        testTagsAsResourceId = true
                                        this.contentDescription =
                                            "${formatNumbers(nameText)}, $statusText"
                                    }
                                    .weight(1f)
                                    .focusGroup(),
                        ) {
                            StyledNameText(
                                modifier =
                                    modifier
                                        .focusable(false)
                                        .testTag("signatureDetailsSignatureName"),
                                nameText,
                            )
                            ColoredSignedStatusText(
                                text = statusText,
                                status = signature.validator.status,
                                modifier =
                                    modifier
                                        .padding(vertical = border)
                                        .focusable(false),
                            )
                        }
                    }

                    Column(
                        modifier =
                            modifier
                                .padding(vertical = SPadding)
                                .testTag("signersCertificateErrorContainer"),
                    ) {
                        if (signatureStatus != ValidatorInterface.Status.Valid) {
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

                    TabView(
                        modifier = modifier.padding(top = MPadding),
                        selectedTabIndex = selectedSignedContainerTabIndex.intValue,
                        onTabSelected = { index ->
                            selectedSignedContainerTabIndex.intValue = index
                        },
                        listOf(
                            Pair(
                                stringResource(R.string.signature_details_role_and_address_title),
                                {
                                    RolesDetailsView(
                                        navController = navController,
                                        modifier = modifier,
                                        sharedSignatureViewModel = sharedSignatureViewModel,
                                    )
                                },
                            ),
                            Pair(
                                stringResource(R.string.signature_details_signer_details_title),
                                {
                                    SignerDetails(
                                        modifier = modifier,
                                        signature = signature,
                                        signersIssuerName = signersIssuerName,
                                        tsIssuerName = tsIssuerName,
                                        ocspIssuerName = ocspIssuerName,
                                        tsSubjectName = tsSubjectName,
                                        ocspSubjectName = ocspSubjectName,
                                        sharedContainerViewModel = sharedContainerViewModel,
                                        sharedCertificateViewModel = sharedCertificateViewModel,
                                        navController = navController,
                                    )
                                },
                            ),
                        ),
                    )
                }
                InvisibleElement(modifier = modifier)
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
