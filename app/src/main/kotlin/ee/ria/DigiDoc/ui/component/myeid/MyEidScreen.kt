@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.pin.PinChangeVariant
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.DateOfBirthUtil
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.myeid.mydata.MyEidMyDataView
import ee.ria.DigiDoc.ui.component.myeid.pinandcertificate.MyEidPinAndCertificateView
import ee.ria.DigiDoc.ui.component.shared.TabView
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.shared.dialog.PinGuideDialog
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utilsLib.date.DateUtil.formattedDateTime
import ee.ria.DigiDoc.utilsLib.extensions.x509Certificate
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMyEidViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedMyEidViewModel: SharedMyEidViewModel,
) {
    val listState = rememberLazyListState()

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val idCardStatus by sharedMyEidViewModel.idCardStatus.asFlow().collectAsState(SmartCardReaderStatus.IDLE)

    val idCardData by sharedMyEidViewModel.idCardData.asFlow().collectAsState(null)

    val selectedMyEidTabIndex = rememberSaveable { mutableIntStateOf(0) }

    val showChangePin1Dialog = rememberSaveable { mutableStateOf(false) }
    val showChangePin2Dialog = rememberSaveable { mutableStateOf(false) }
    val showPukDialog = rememberSaveable { mutableStateOf(false) }
    val showForgotPin1Dialog = rememberSaveable { mutableStateOf(false) }
    val showForgotPin2Dialog = rememberSaveable { mutableStateOf(false) }

    val changePukText =
        stringResource(
            R.string.myeid_change_pin,
            CodeType.PUK,
        )

    val changePukSubtitleText = stringResource(R.string.myeid_puk_info)

    val buttonName = stringResource(id = R.string.button_name)

    val pin1Guidelines =
        """
        • ${stringResource(R.string.myeid_pin1_guideline_1)}
        • ${stringResource(R.string.myeid_pin1_guideline_2)}
        • ${stringResource(R.string.myeid_pin1_guideline_3)}
        """.trimIndent()

    val pin2Guidelines =
        """
        • ${stringResource(R.string.myeid_pin2_guideline_1)}
        • ${stringResource(R.string.myeid_pin2_guideline_2)}
        • ${stringResource(R.string.myeid_pin2_guideline_3)}
        """.trimIndent()

    val pukGuidelines =
        """
        • ${stringResource(R.string.myeid_puk_guideline_1)}
        • ${stringResource(R.string.myeid_puk_guideline_2)}
        """.trimIndent()

    val handlePinDialogResult: (Boolean, PinChangeVariant) -> Unit = { isConfirmed, pinVariant ->
        showChangePin1Dialog.value = false
        showChangePin2Dialog.value = false
        showPukDialog.value = false
        showForgotPin1Dialog.value = false
        showForgotPin2Dialog.value = false

        sharedMyEidViewModel.setScreenContent(pinVariant)

        if (isConfirmed) {
            navController.navigate(
                Route.MyEidPinScreen.route,
            )
        }
    }

    BackHandler {
        sharedMyEidViewModel.resetValues()
        sharedMyEidViewModel.resetIdCardData()
        navController.navigateUp()
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    LaunchedEffect(idCardStatus) {
        idCardStatus?.let { status ->
            if (idCardData?.personalData != null) {
                when (status) {
                    SmartCardReaderStatus.CARD_DETECTED -> {}
                    else -> {
                        navController.navigate(Route.MyEidIdentificationScreen.route) {
                            popUpTo(Route.Home.route) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }
    }

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
                }
                .testTag("myEidScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = R.string.main_home_my_eid_title,
                leftIcon = R.drawable.ic_m3_close_48dp_wght400,
                leftIconContentDescription = R.string.close_button,
                onLeftButtonClick = {
                    sharedMyEidViewModel.resetValues()
                    sharedMyEidViewModel.resetIdCardData()
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Home.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
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
                    }
                    .testTag("myEidContainer"),
        ) {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                TabView(
                    modifier =
                        modifier
                            .semantics {
                                testTagsAsResourceId = true
                            }
                            .testTag("myEidTabView"),
                    selectedTabIndex = selectedMyEidTabIndex.intValue,
                    onTabSelected = { index -> selectedMyEidTabIndex.intValue = index },
                    listOf(
                        Pair(
                            stringResource(R.string.myeid_my_data),
                        ) {
                            val personalData = idCardData?.personalData

                            MyEidMyDataView(
                                modifier,
                                firstname = personalData?.givenNames().orEmpty(),
                                lastname = personalData?.surname().orEmpty(),
                                citizenship = personalData?.citizenship().orEmpty(),
                                personalCode = personalData?.personalCode().orEmpty(),
                                dateOfBirth =
                                    if (!personalData?.personalCode().isNullOrEmpty()) {
                                        formattedDateTime(
                                            DateOfBirthUtil.parseDateOfBirth(
                                                personalData.personalCode().orEmpty(),
                                            ).toString(),
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                                        ).date
                                    } else {
                                        ""
                                    },
                                documentNumber = personalData?.documentNumber().orEmpty(),
                                validTo =
                                    if (personalData?.expiryDate() != null) {
                                        formattedDateTime(
                                            personalData.expiryDate().toString(),
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                                        ).date
                                    } else {
                                        ""
                                    },
                            )
                        },
                        Pair(
                            stringResource(R.string.myeid_pins_and_certificates),
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier =
                                    modifier
                                        .padding(vertical = SPadding)
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .testTag("lazyColumnScrollView"),
                                verticalArrangement = Arrangement.spacedBy(SPadding),
                            ) {
                                item {
                                    Column(
                                        modifier =
                                            modifier
                                                .fillMaxWidth()
                                                .padding(bottom = SPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(SPadding),
                                    ) {
                                        MyEidPinAndCertificateView(
                                            title = stringResource(R.string.myeid_authentication_certificate_title),
                                            subtitle =
                                                stringResource(
                                                    R.string.myeid_certificate_valid_to,
                                                    sharedMyEidViewModel.getNotAfter(
                                                        idCardData?.authCertificate?.data?.x509Certificate(),
                                                    ),
                                                ),
                                            isPinBlocked = idCardData?.pin1RetryCount == 0,
                                            forgotPinText =
                                                if (idCardData?.pin1RetryCount == 0) {
                                                    stringResource(
                                                        R.string.myeid_pin_unblock_button,
                                                        CodeType.PIN1,
                                                    )
                                                } else {
                                                    stringResource(
                                                        R.string.myeid_forgot_pin,
                                                        CodeType.PIN1,
                                                    )
                                                },
                                            onForgotPinClick = {
                                                showForgotPin1Dialog.value = true
                                            },
                                            changePinText =
                                                stringResource(
                                                    R.string.myeid_change_pin,
                                                    CodeType.PIN1,
                                                ),
                                            onChangePinClick = {
                                                showChangePin1Dialog.value = true
                                            },
                                        )

                                        if (idCardData?.pin1RetryCount == 0) {
                                            Text(
                                                modifier =
                                                    modifier
                                                        .fillMaxWidth()
                                                        .focusable(true)
                                                        .testTag("myEidBlockedPin1DescriptionText"),
                                                text =
                                                    stringResource(
                                                        R.string.myeid_pin_blocked_with_unblock_message,
                                                        CodeType.PIN1,
                                                    ),
                                                color = Red500,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                                item {
                                    Column(
                                        modifier =
                                            modifier
                                                .fillMaxWidth()
                                                .padding(bottom = SPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(SPadding),
                                    ) {
                                        MyEidPinAndCertificateView(
                                            title = stringResource(R.string.myeid_signing_certificate_title),
                                            subtitle =
                                                stringResource(
                                                    R.string.myeid_certificate_valid_to,
                                                    sharedMyEidViewModel.getNotAfter(
                                                        idCardData?.signCertificate?.data?.x509Certificate(),
                                                    ),
                                                ),
                                            isPinBlocked = idCardData?.pin2RetryCount == 0,
                                            forgotPinText =
                                                if (idCardData?.pin2RetryCount == 0) {
                                                    stringResource(
                                                        R.string.myeid_pin_unblock_button,
                                                        CodeType.PIN2,
                                                    )
                                                } else {
                                                    stringResource(
                                                        R.string.myeid_forgot_pin,
                                                        CodeType.PIN2,
                                                    )
                                                },
                                            onForgotPinClick = {
                                                showForgotPin2Dialog.value = true
                                            },
                                            changePinText =
                                                stringResource(
                                                    R.string.myeid_change_pin,
                                                    CodeType.PIN2,
                                                ),
                                            onChangePinClick = {
                                                showChangePin2Dialog.value = true
                                            },
                                        )

                                        if (idCardData?.pin2RetryCount == 0) {
                                            Text(
                                                modifier =
                                                    modifier
                                                        .fillMaxWidth()
                                                        .focusable(true)
                                                        .testTag("myEidBlockedPin2DescriptionText"),
                                                text =
                                                    stringResource(
                                                        R.string.myeid_pin_blocked_with_unblock_message,
                                                        CodeType.PIN2,
                                                    ),
                                                color = Red500,
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                }
                                item {
                                    Column(
                                        modifier =
                                            modifier
                                                .fillMaxWidth()
                                                .padding(bottom = SPadding),
                                        horizontalAlignment = Alignment.Start,
                                        verticalArrangement = Arrangement.spacedBy(SPadding),
                                    ) {
                                        val isPukBlocked = idCardData?.pukRetryCount == 0
                                        Row(
                                            modifier =
                                                modifier
                                                    .fillMaxWidth()
                                                    .clickable(enabled = !isPukBlocked) {
                                                        showPukDialog.value = true
                                                    }
                                                    .semantics {
                                                        this.role = Role.Button
                                                        this.contentDescription =
                                                            "$changePukText. $changePukSubtitleText. $buttonName"
                                                                .lowercase()
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("myEidPukChangeButton"),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            MyEidPinAndCertificateView(
                                                modifier =
                                                    modifier
                                                        .alpha(if (!isPukBlocked) 1f else 0.7f),
                                                title = changePukText,
                                                isPinBlocked = isPukBlocked,
                                                subtitle = changePukSubtitleText,
                                                showForgotPin = false,
                                            )
                                        }
                                        Row(
                                            modifier =
                                                modifier
                                                    .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            if (isPukBlocked) {
                                                Text(
                                                    modifier =
                                                        modifier
                                                            .fillMaxWidth()
                                                            .focusable(true)
                                                            .testTag("myEidBlockedPukDescriptionText"),
                                                    text = stringResource(R.string.myeid_puk_blocked),
                                                    color = Red500,
                                                    style = MaterialTheme.typography.bodySmall,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    ),
                )
            }
        }
    }

    PinGuideDialog(
        showDialog = showChangePin1Dialog,
        pinChangeVariant = PinChangeVariant.ChangePin1,
        title = R.string.myeid_change_pin_info_title,
        titleExtra = CodeType.PIN1.name,
        guidelines = pin1Guidelines,
        confirmButton = R.string.myeid_change_pin,
        confirmButtonExtra = CodeType.PIN1.name,
        onResult = handlePinDialogResult,
    )

    PinGuideDialog(
        showDialog = showChangePin2Dialog,
        pinChangeVariant = PinChangeVariant.ChangePin2,
        title = R.string.myeid_change_pin_info_title,
        titleExtra = CodeType.PIN2.name,
        guidelines = pin2Guidelines,
        confirmButton = R.string.myeid_change_pin,
        confirmButtonExtra = CodeType.PIN2.name,
        onResult = handlePinDialogResult,
    )

    PinGuideDialog(
        showDialog = showPukDialog,
        pinChangeVariant = PinChangeVariant.ChangePuk,
        title = R.string.myeid_change_pin_info_title,
        titleExtra = CodeType.PUK.name,
        guidelines = pukGuidelines,
        confirmButton = R.string.myeid_change_pin,
        confirmButtonExtra = CodeType.PUK.name,
        onResult = handlePinDialogResult,
    )

    PinGuideDialog(
        showDialog = showForgotPin1Dialog,
        pinChangeVariant = PinChangeVariant.ForgotPin1,
        title = R.string.myeid_change_pin_info_title,
        titleExtra = CodeType.PIN1.name,
        guidelines = pin1Guidelines,
        confirmButton = R.string.myeid_pin_unblock_button,
        confirmButtonExtra = CodeType.PIN1.name,
        onResult = handlePinDialogResult,
    )

    PinGuideDialog(
        showDialog = showForgotPin2Dialog,
        pinChangeVariant = PinChangeVariant.ForgotPin2,
        title = R.string.myeid_change_pin_info_title,
        titleExtra = CodeType.PIN2.name,
        guidelines = pin2Guidelines,
        confirmButton = R.string.myeid_pin_unblock_button,
        confirmButtonExtra = CodeType.PIN2.name,
        onResult = handlePinDialogResult,
    )
}
