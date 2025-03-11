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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.formatNumbers
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utilsLib.date.DateUtil
import ee.ria.DigiDoc.utilsLib.extensions.formatHexString
import ee.ria.DigiDoc.utilsLib.extensions.hexString
import ee.ria.DigiDoc.utilsLib.text.TextUtil
import ee.ria.DigiDoc.viewmodel.CertificateDetailViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import org.bouncycastle.asn1.x500.style.BCStyle
import kotlin.text.Charsets.UTF_8

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CertificateDetailsView(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedCertificateViewModel: SharedCertificateViewModel,
    certificateDetailViewModel: CertificateDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val certificate = sharedCertificateViewModel.certificate.value

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    BackHandler {
        handleBackButtonClick(navController, sharedCertificateViewModel)
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("certificateDetailsScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.certificate_details_title,
                onLeftButtonClick = {
                    handleBackButtonClick(navController, sharedCertificateViewModel)
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
    ) { paddingValues ->
        SettingsMenuBottomSheet(
            navController = navController,
            isBottomSheetVisible = isSettingsMenuBottomSheetVisible,
        )
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                        .testTag("certificateDetailContainer"),
            ) {
                val certificateHolder = certificateDetailViewModel.certificateToJcaX509(certificate)
                if (certificate != null && certificateHolder != null) {
                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .testTag("scrollView"),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        val publicKeyParameters =
                            certificateHolder.subjectPublicKeyInfo.algorithm.parameters
                        val sigAlgParams = certificate.sigAlgParams?.toString(UTF_8)?.trim()

                        CertificateDetailItem().certificateDetailItems(
                            subjectNameHeader = R.string.subject_name,
                            issuerNameHeader = R.string.issuer_name,
                            publicKeyInfoHeader = R.string.public_key,
                            extensionsHeader = R.string.extensions,
                            fingerprintsHeader = R.string.fingerprints,
                            subjectCountryOrRegion =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.C,
                                ),
                            subjectOrganization =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.O,
                                ),
                            subjectOrganizationalUnit =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.OU,
                                ),
                            subjectCommonName =
                                TextUtil.splitTextAndJoin(
                                    text =
                                        TextUtil.removeSlashes(
                                            certificateDetailViewModel.getRDNValue(
                                                certificateHolder.subject,
                                                BCStyle.CN,
                                            ),
                                        ),
                                    delimiter = ",",
                                    joinDelimiter = ", ",
                                ),
                            subjectSurname =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.SURNAME,
                                ),
                            subjectGivenName =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.GIVENNAME,
                                ),
                            subjectSerialNumber =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.subject,
                                    BCStyle.SERIALNUMBER,
                                ),
                            issuerCountryOrRegion =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.issuer,
                                    BCStyle.C,
                                ),
                            issuerOrganization =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.issuer,
                                    BCStyle.O,
                                ),
                            issuerCommonName =
                                TextUtil.splitTextAndJoin(
                                    text =
                                        TextUtil.removeSlashes(
                                            certificateDetailViewModel.getRDNValue(
                                                certificateHolder.issuer,
                                                BCStyle.CN,
                                            ),
                                        ),
                                    delimiter = ",",
                                    joinDelimiter = ", ",
                                ),
                            issuerEmailAddress =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.issuer,
                                    BCStyle.EmailAddress,
                                ),
                            issuerOtherName =
                                certificateDetailViewModel.getRDNValue(
                                    certificateHolder.issuer,
                                    BCStyle.ORGANIZATION_IDENTIFIER,
                                ),
                            issuerSerialNumber =
                                certificateDetailViewModel.addLeadingZeroToHex(
                                    certificate.serialNumber?.toString(16)?.formatHexString(),
                                ),
                            issuerVersion = certificate.version.toString(),
                            issuerSignatureAlgorithm = "${certificate.sigAlgName} (${certificate.sigAlgOID})",
                            issuerParameters =
                                if (certificateDetailViewModel.isValidParametersData(
                                        sigAlgParams ?: "",
                                    )
                                ) {
                                    sigAlgParams
                                } else {
                                    "None"
                                },
                            issuerNotValidBefore = DateUtil.dateToCertificateFormat(certificate.notBefore),
                            issuerNotValidAfter = DateUtil.dateToCertificateFormat(certificate.notAfter),
                            publicKeyAlgorithm = certificate.publicKey.algorithm,
                            publicKeyParameters =
                                if (publicKeyParameters.toString() != "NULL") {
                                    publicKeyParameters.toString()
                                } else {
                                    "None"
                                },
                            publicKeyKey = certificateDetailViewModel.getPublicKeyString(certificate.publicKey),
                            publicKeyKeyUsage = certificateDetailViewModel.getKeyUsages(certificate.keyUsage),
                            publicKeySignature = certificate.signature.hexString().uppercase(),
                            extensions =
                                certificateDetailViewModel.getExtensionsData(
                                    certificateHolder,
                                    certificate,
                                ),
                            fingerprintSha256 =
                                certificateDetailViewModel.getCertificateSHA256Fingerprint(
                                    certificate,
                                ),
                            fingerprintSha1 =
                                certificateDetailViewModel.getCertificateSHA1Fingerprint(
                                    certificate,
                                ),
                        ).forEach { certificateDetail ->
                            when (certificateDetail) {
                                is CertificateListItem.Certificate -> {
                                    if (!certificateDetail.detailValue.isNullOrEmpty()) {
                                        val detailKeyText =
                                            if (certificateDetail.detailKey != 0) {
                                                stringResource(id = certificateDetail.detailKey)
                                            } else {
                                                ""
                                            }
                                        SignatureDataItem(
                                            modifier = modifier,
                                            icon = 0,
                                            testTag = certificateDetail.testTag,
                                            detailKey = certificateDetail.detailKey,
                                            detailValue = certificateDetail.detailValue,
                                            contentDescription =
                                                "$detailKeyText, ${certificateDetail.detailValue}".lowercase(),
                                            formatForAccessibility = certificateDetail.formatForAccessibility,
                                        )
                                        HorizontalDivider()
                                    }
                                }

                                is CertificateListItem.TextItem -> {
                                    Row(
                                        modifier =
                                            modifier
                                                .padding(top = MPadding, bottom = SPadding)
                                                .fillMaxWidth()
                                                .semantics(mergeDescendants = true) {
                                                    this.contentDescription =
                                                        formatNumbers(certificateDetail.text).lowercase()
                                                    heading()
                                                }
                                                .focusable()
                                                .focusGroup()
                                                .testTag(certificateDetail.testTag),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier =
                                                modifier
                                                    .notAccessible(),
                                            text = certificateDetail.text,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        }
                        InvisibleElement(modifier = modifier)
                    }
                }
            }
        }
    }
}

private fun handleBackButtonClick(
    navController: NavController,
    sharedCertificateViewModel: SharedCertificateViewModel,
) {
    sharedCertificateViewModel.resetCertificate()
    navController.navigateUp()
}
