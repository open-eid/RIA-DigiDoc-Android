@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing.certificate

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import ee.ria.DigiDoc.common.Constant.DDOC_MIMETYPE
import ee.ria.DigiDoc.libdigidoclib.domain.model.ValidatorInterface
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.ExpandableButton
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.ColoredSignedStatusText
import ee.ria.DigiDoc.ui.component.signing.StyledNameText
import ee.ria.DigiDoc.ui.theme.Dimensions.SBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil
import ee.ria.DigiDoc.utils.libdigidoc.SignatureStatusUtil.getSignatureStatusText
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.container.NameUtil.formatName
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.CertificateDetailViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSignatureViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignerDetailsView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSignatureViewModel: SharedSignatureViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
    certificateDetailViewModel: CertificateDetailViewModel = hiltViewModel(),
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val signature = sharedSignatureViewModel.signature.value
    var signatureStatus = signature?.validator?.status
    val diagnosticsInfo = signature?.validator?.diagnostics ?: ""

    val timestamps = sharedContainerViewModel.signedContainer.value?.getTimestamps()
    val firstTimestamp = timestamps?.firstOrNull()
    val isDdoc = sharedContainerViewModel.signedContainer.value?.containerMimetype() == DDOC_MIMETYPE
    val isValid = firstTimestamp?.let { SignatureStatusUtil.isDdocSignatureValid(it) } == true
    val isDdocValid = !timestamps.isNullOrEmpty() && isDdoc && isValid

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

    val signersIssuerName =
        certificateDetailViewModel.getIssuerCommonName(
            signature?.signingCertificateDer?.x509Certificate(),
        )
    val tsIssuerName =
        certificateDetailViewModel.getIssuerCommonName(
            signature?.timeStampCertificateDer?.x509Certificate(),
        )
    val ocspIssuerName =
        certificateDetailViewModel.getIssuerCommonName(
            signature?.ocspCertificateDer?.x509Certificate(),
        )

    val tsSubjectName =
        certificateDetailViewModel.getSubjectCommonName(
            signature?.timeStampCertificateDer?.x509Certificate(),
        )
    val ocspSubjectName =
        certificateDetailViewModel.getSubjectCommonName(
            signature?.ocspCertificateDer?.x509Certificate(),
        )

    val selectedSignedContainerTabIndex = rememberSaveable { mutableIntStateOf(0) }

    BackHandler {
        handleBackButtonClick(navController, sharedSignatureViewModel)
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    if (signature != null) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    modifier = modifier.padding(vertical = SPadding),
                    hostState = snackBarHostState,
                )
            },
            modifier =
                modifier
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("signatureDetailsScreen"),
            topBar = {
                TopBar(
                    modifier = modifier,
                    sharedMenuViewModel = sharedMenuViewModel,
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
                            .verticalScroll(rememberScrollState())
                            .padding(SPadding)
                            .semantics {
                                testTagsAsResourceId = true
                            }.testTag("signersCertificateContainer"),
                ) {
                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .focusable(true)
                                .semantics(mergeDescendants = true) {
                                    testTagsAsResourceId = true
                                }.focusGroup()
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
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }.testTag("signatureUpdateListSignatureType")
                                    .notAccessible(),
                        )
                        Spacer(modifier = modifier.width(SPadding))

                        val nameText = formatName(signature.name)
                        val statusText =
                            getSignatureStatusText(
                                LocalContext.current,
                                if (isDdocValid) {
                                    ValidatorInterface.Status.Valid
                                } else {
                                    signature.validator.status
                                },
                            )
                        Column(
                            modifier =
                                modifier
                                    .semantics(mergeDescendants = true) {
                                        testTagsAsResourceId = true
                                        this.contentDescription =
                                            "${formatNumbers(nameText)}, $statusText"
                                    }.weight(1f)
                                    .focusGroup(),
                        ) {
                            StyledNameText(
                                modifier =
                                    modifier
                                        .focusable(false)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }.testTag("signatureDetailsSignatureName")
                                        .notAccessible(),
                                nameText,
                            )
                            ColoredSignedStatusText(
                                text = statusText,
                                status =
                                    if (isDdocValid) {
                                        val validStatus = ValidatorInterface.Status.Valid
                                        signatureStatus = validStatus
                                        validStatus
                                    } else {
                                        signature.validator.status
                                    },
                                modifier =
                                    modifier
                                        .padding(vertical = SBorder)
                                        .focusable(false)
                                        .notAccessible(),
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
                                            horizontal = XSPadding,
                                            vertical = SPadding,
                                        ).testTag("signersCertificateErrorDetails"),
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
                        modifier = modifier,
                        testTag = "signatureDetailsTabView",
                        selectedTabIndex = selectedSignedContainerTabIndex.intValue,
                        onTabSelected = { index ->
                            selectedSignedContainerTabIndex.intValue = index
                        },
                        listOf(
                            Pair(
                                stringResource(R.string.signature_details_role_and_address_title),
                            ) {
                                RolesDetailsView(
                                    navController = navController,
                                    modifier = modifier,
                                    sharedSignatureViewModel = sharedSignatureViewModel,
                                )
                            },
                            Pair(
                                stringResource(R.string.signature_details_signer_details_title),
                            ) {
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
                    )
                    InvisibleElement(modifier = modifier)
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
