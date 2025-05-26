@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import android.view.accessibility.AccessibilityEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.myeid.MyEidIdentificationMethodSetting
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.PaceTunnelException
import ee.ria.DigiDoc.smartcardreader.ApduResponseException
import ee.ria.DigiDoc.smartcardreader.SmartCardReaderStatus
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.PrimaryOutlinedButton
import ee.ria.DigiDoc.ui.component.shared.SecurePinTextField
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCSignatureUpdateContainer
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.NFCViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMyEidViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidPinScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedMyEidViewModel: SharedMyEidViewModel,
    nfcViewModel: NFCViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val messages by SnackBarManager.messages.collectAsStateWithLifecycle(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val idCardStatus by sharedMyEidViewModel.idCardStatus.asFlow().collectAsState(SmartCardReaderStatus.IDLE)

    val idCardData by sharedMyEidViewModel.idCardData.asFlow().collectAsState(null)

    val identificationMethod by sharedMyEidViewModel.identificationMethod.asFlow().collectAsState(null)

    val content by sharedMyEidViewModel.pinScreenContent.collectAsState()

    val currentPinState = remember { mutableStateOf(byteArrayOf()) }
    val newPinState = remember { mutableStateOf(byteArrayOf()) }
    val newPinRepeatedState = remember { mutableStateOf(byteArrayOf()) }

    var showCurrentPinField = rememberSaveable { mutableStateOf(true) }
    var showNewPinField = rememberSaveable { mutableStateOf(false) }
    var showNewRepeatPinField = rememberSaveable { mutableStateOf(false) }

    var pinErrorText = rememberSaveable { mutableStateOf("") }

    var actionContinue = stringResource(R.string.action_continue)

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    val codeType = content?.codeType ?: CodeType.PIN1
    val isForgottenPin = content?.isForgottenPin == true
    val title = stringResource(content?.title ?: R.string.myeid_pin_change_title, codeType.name)

    val pinCodeLabel = stringResource(id = R.string.signature_update_nfc_pin, codeType)

    val showNFCScreen = remember { mutableStateOf(false) }

    val pinDifferentRequirementText =
        stringResource(
            R.string.myeid_new_pin_different_requirement,
            codeType,
            codeType,
        )

    val pinLengthRequirementText =
        stringResource(
            R.string.id_card_sign_pin_invalid_length,
            if (isForgottenPin && showCurrentPinField.value) {
                CodeType.PUK.name
            } else {
                codeType.name
            },
            sharedMyEidViewModel.getPinCodeMinimumLength(
                if (isForgottenPin) {
                    CodeType.PUK
                } else {
                    codeType
                },
            ),
            Constant.MyEID.PIN_MAXIMUM_LENGTH,
        )

    val isCurrentPinValid =
        sharedMyEidViewModel.isPinCodeLengthValid(
            if (isForgottenPin) {
                CodeType.PUK
            } else {
                codeType
            },
            currentPinState.value,
        )
    val isNewPinValid =
        sharedMyEidViewModel.isPinCodeValid(
            codeType,
            currentPinState.value,
            newPinState.value,
            idCardData?.personalData?.personalCode().orEmpty(),
        )
    val isNewRepeatedPinValid =
        sharedMyEidViewModel.isPinCodeLengthValid(codeType, newPinRepeatedState.value) &&
            sharedMyEidViewModel.pinCodesMatch(newPinState.value, newPinRepeatedState.value)

    val pinChangedSuccess =
        stringResource(
            if (isForgottenPin) {
                R.string.myeid_pin_unblocked_success
            } else {
                R.string.myeid_pin_changed_success
            },
            codeType,
        )

    val pinChangingState by sharedMyEidViewModel.pinChangingState.asFlow().collectAsState(false)
    val errorState by sharedMyEidViewModel.errorState.asFlow().collectAsState(null)
    val isPinBlocked by sharedMyEidViewModel.isPinBlocked.asFlow().collectAsState(false)

    val currentPinText =
        if (isForgottenPin) {
            stringResource(
                R.string.myeid_current_pin_code_title,
                CodeType.PUK,
            )
        } else {
            stringResource(
                R.string.myeid_current_pin_code_title,
                codeType.name,
            )
        }

    val newPinText =
        stringResource(
            R.string.myeid_new_pin_code_title,
            codeType.name,
        )

    val newPinRepeatedText =
        stringResource(
            R.string.myeid_repeat_new_pin_code_title,
            codeType.name,
        )

    val pinChangeTitleFocusRequester = remember { FocusRequester() }
    val currentPinFocusRequester = remember { FocusRequester() }
    val newPinFocusRequester = remember { FocusRequester() }
    val newPinRepeatedFocusRequester = remember { FocusRequester() }
    val newPinDescriptionFocusRequester = remember { FocusRequester() }
    val newPinRepeatedDescriptionFocusRequester = remember { FocusRequester() }

    fun resetPins() {
        newPinRepeatedState.value = byteArrayOf()

        newPinState.value = byteArrayOf()

        currentPinState.value = byteArrayOf()
    }

    fun resetToBeginning() {
        resetPins()
        showNewPinField.value = false
        showNewRepeatPinField.value = false
        showCurrentPinField.value = true
    }

    LaunchedEffect(Unit, showCurrentPinField) {
        if (isTalkBackEnabled(context)) {
            delay(1000)
            pinChangeTitleFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(pinErrorText.value) {
        if (isTalkBackEnabled(context) && pinErrorText.value.isNotEmpty()) {
            AccessibilityUtil.sendAccessibilityEvent(
                context,
                AccessibilityEvent.TYPE_ANNOUNCEMENT,
                pinErrorText.value.lowercase(),
            )
        }
    }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    LaunchedEffect(pinChangingState) {
        if (pinChangingState == true) {
            sharedMyEidViewModel.resetPinChangingState()
            resetPins()
            showMessage(pinChangedSuccess)
            navController.navigateUp()
        }
    }

    LaunchedEffect(errorState, isPinBlocked) {
        errorState?.let {
            var message = ""
            when (context.resources.getResourceTypeName(it.first)) {
                "plurals" -> {
                    message = context.resources.getQuantityString(it.first, it.third ?: 1, it.second, it.third)
                }
                "string" -> {
                    message = context.getString(it.first, it.second, it.third)
                }
            }

            showNFCScreen.value = false
            showMessage(message)
            resetToBeginning()
            if (isPinBlocked) {
                resetPins()
                sharedMyEidViewModel.resetValues()
                navController.navigateUp()
            }
        }

        sharedMyEidViewModel.resetErrorState()
    }

    LaunchedEffect(showNewPinField, showNewRepeatPinField, newPinState.value, newPinRepeatedState.value) {
        val personalCode = idCardData?.personalData?.personalCode().orEmpty()
        pinErrorText.value = ""

        if (showNewPinField.value) {
            if (sharedMyEidViewModel.isPinCodeLengthValid(codeType, newPinState.value)) {
                pinErrorText.value =
                    when {
                        sharedMyEidViewModel.pinCodesMatch(currentPinState.value, newPinState.value) ->
                            context.getString(R.string.myeid_new_and_current_pin_match_error, codeType.name)

                        sharedMyEidViewModel.isNewPinPartOfPersonalCode(newPinState.value, personalCode) ->
                            context.getString(R.string.myeid_pin_part_personal_code_error, codeType.name)

                        sharedMyEidViewModel.isNewPinPartOfBirthDate(newPinState.value, personalCode) ->
                            context.getString(R.string.myeid_pin_part_dob_error, codeType.name)

                        sharedMyEidViewModel.isPinCodeTooEasy(newPinState.value) ->
                            context.getString(R.string.myeid_pin_too_easy_error, codeType.name)

                        else -> ""
                    }
            }
        }

        if (showNewRepeatPinField.value) {
            if (!sharedMyEidViewModel.pinCodesMatch(newPinState.value, newPinRepeatedState.value)) {
                pinErrorText.value = context.getString(R.string.myeid_pin_repeat_error, codeType.name)
            }
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

    BackHandler {
        if (showNewRepeatPinField.value) {
            newPinRepeatedState.value = byteArrayOf()

            showNewRepeatPinField.value = false
            showCurrentPinField.value = false
            showNewPinField.value = true
        } else if (showCurrentPinField.value) {
            resetPins()
            sharedMyEidViewModel.resetScreenContent()
            navController.navigateUp()
        } else {
            resetToBeginning()
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
                .testTag("myEidPinScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = null,
                leftIcon =
                    if (showCurrentPinField.value) {
                        R.drawable.ic_m3_close_48dp_wght400
                    } else {
                        R.drawable.ic_m3_arrow_back_48dp_wght400
                    },
                leftIconContentDescription =
                    if (showCurrentPinField.value) {
                        R.string.close_button
                    } else {
                        R.string.back
                    },
                onLeftButtonClick = {
                    if (showNewRepeatPinField.value) {
                        showNewRepeatPinField.value = false
                        showCurrentPinField.value = false
                        showNewPinField.value = true

                        newPinRepeatedState.value = byteArrayOf()
                    } else if (showNewPinField.value) {
                        resetToBeginning()

                        newPinRepeatedState.value = byteArrayOf()

                        newPinState.value = byteArrayOf()
                    } else {
                        resetPins()
                        sharedMyEidViewModel.resetScreenContent()
                        navController.navigateUp()
                    }
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
        bottomBar = {
            if (showNewRepeatPinField.value && !showNFCScreen.value) {
                PrimaryOutlinedButton(
                    modifier = modifier,
                    title =
                        if (isForgottenPin) {
                            R.string.myeid_pin_unblock_button
                        } else {
                            R.string.myeid_save_new_pin
                        },
                    titleExtra = codeType.name,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    enabled = isNewRepeatedPinValid,
                ) {
                    if (identificationMethod == MyEidIdentificationMethodSetting.NFC) {
                        showNFCScreen.value = true
                    }

                    scope.launch(IO) {
                        if (activity == null) {
                            withContext(Main) {
                                showMessage(context, R.string.error_general_client)
                            }
                        } else if (isForgottenPin) {
                            sharedMyEidViewModel.getToken(
                                activity = activity,
                            ) { token, exc ->
                                if (token != null && exc == null) {
                                    // NFC operations must run on the same thread as the startDiscovery callback.
                                    // Only "runBlocking" works here — coroutines or new threads break the NFC session.
                                    runBlocking {
                                        sharedMyEidViewModel.unblockAndEditPin(
                                            token = token,
                                            codeType = codeType,
                                            currentPuk = currentPinState.value,
                                            newPin = newPinRepeatedState.value,
                                        )
                                        withContext(Main) {
                                            resetPins()
                                        }
                                    }
                                } else {
                                    if (exc?.message?.contains("TagLostException") == true) {
                                        showMessage(context, R.string.signature_update_nfc_tag_lost)
                                    } else if (exc is ApduResponseException) {
                                        showMessage(context, R.string.signature_update_nfc_technical_error)
                                    } else if (exc is PaceTunnelException) {
                                        showMessage(context, R.string.signature_update_nfc_wrong_can)
                                    } else {
                                        showMessage(context, R.string.signature_update_nfc_technical_error)
                                    }
                                    showNFCScreen.value = false
                                }
                            }
                        } else {
                            sharedMyEidViewModel.getToken(
                                activity = activity,
                            ) { token, exc ->
                                if (token != null && exc == null) {
                                    // NFC operations must run on the same thread as the startDiscovery callback.
                                    // Only "runBlocking" works here — coroutines or new threads break the NFC session.
                                    runBlocking {
                                        sharedMyEidViewModel.editPin(
                                            token = token,
                                            codeType = codeType,
                                            currentPin = currentPinState.value,
                                            newPin = newPinRepeatedState.value,
                                        )
                                        withContext(Main) {
                                            resetPins()
                                        }
                                    }
                                } else {
                                    if (exc?.message?.contains("TagLostException") == true) {
                                        showMessage(context, R.string.signature_update_nfc_tag_lost)
                                    } else if (exc is ApduResponseException) {
                                        showMessage(context, R.string.signature_update_nfc_technical_error)
                                    } else if (exc is PaceTunnelException) {
                                        showMessage(context, R.string.signature_update_nfc_wrong_can)
                                    } else {
                                        showMessage(context, R.string.signature_update_nfc_technical_error)
                                    }
                                    showNFCScreen.value = false
                                }
                            }
                        }
                    }
                }
            } else if (!showNFCScreen.value) {
                PrimaryOutlinedButton(
                    modifier = modifier,
                    title = R.string.action_continue,
                    contentDescription = actionContinue,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    enabled =
                        if (showCurrentPinField.value) {
                            isCurrentPinValid
                        } else {
                            isNewPinValid
                        },
                ) {
                    if (currentPinState.value.isEmpty()) {
                        showCurrentPinField.value = false
                        showNewRepeatPinField.value = false
                        showCurrentPinField.value = true
                    } else if (currentPinState.value.isNotEmpty() && newPinState.value.isEmpty()) {
                        showCurrentPinField.value = false
                        showNewRepeatPinField.value = false
                        showNewPinField.value = true
                    } else if (currentPinState.value.isNotEmpty() &&
                        newPinState.value.isNotEmpty() &&
                        newPinRepeatedState.value.isEmpty()
                    ) {
                        showCurrentPinField.value = false
                        showNewPinField.value = false
                        showNewRepeatPinField.value = true
                    }
                }
            }
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
                    .testTag("myEidPinContainer"),
        ) {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                if (showNFCScreen.value) {
                    NFCSignatureUpdateContainer(
                        nfcViewModel = nfcViewModel,
                    ) {
                        showNFCScreen.value = false
                    }
                } else {
                    Text(
                        text = title,
                        maxLines = 2,
                        modifier =
                            modifier
                                .focusRequester(pinChangeTitleFocusRequester)
                                .zIndex(1f)
                                .fillMaxWidth()
                                .padding(SPadding)
                                .semantics {
                                    heading()
                                    this.contentDescription = title.lowercase()
                                    traversalIndex = 1f
                                    testTagsAsResourceId = true
                                }
                                .focusable(enabled = true)
                                .focusTarget()
                                .focusProperties { canFocus = true }
                                .testTag("myEidPinChangeTitle"),
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    Icon(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .size(iconSizeM),
                        imageVector =
                            ImageVector.vectorResource(id = R.drawable.ic_m3_vpn_key_48dp_wght400),
                        contentDescription = null,
                    )

                    Column(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .padding(vertical = SPadding)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        focusManager.clearFocus()
                                    })
                                },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(SPadding),
                    ) {
                        if (showCurrentPinField.value) {
                            Text(
                                text = currentPinText,
                                modifier =
                                    modifier
                                        .focusable(false)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidPinCurrentPinCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SecurePinTextField(
                                    modifier =
                                        modifier
                                            .weight(1f)
                                            .zIndex(3f)
                                            .semantics {
                                                traversalIndex = 3f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("myEidCurrentPinTextField"),
                                    pin = currentPinState,
                                    pinCodeLabel = pinCodeLabel,
                                    pinNumberFocusRequester = currentPinFocusRequester,
                                    previousFocusRequester = pinChangeTitleFocusRequester,
                                    pinCodeTextEdited = null,
                                    trailingIconContentDescription = "$clearButtonText $buttonName",
                                    isError = !isCurrentPinValid,
                                    keyboardImeAction = ImeAction.Next,
                                    keyboardActions =
                                        KeyboardActions(
                                            onNext = {
                                                if (isCurrentPinValid) {
                                                    showCurrentPinField.value = false
                                                    showNewRepeatPinField.value = false
                                                    showNewPinField.value = true
                                                } else {
                                                    focusManager.clearFocus()
                                                }
                                            },
                                        ),
                                )
                                if (isTalkBackEnabled(context) && currentPinState.value.isNotEmpty()) {
                                    IconButton(
                                        modifier =
                                            modifier
                                                .zIndex(4f)
                                                .align(Alignment.CenterVertically)
                                                .semantics {
                                                    traversalIndex = 4f
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidCurrentPinRemoveButton"),
                                        onClick = {
                                            currentPinState.value = byteArrayOf()
                                            scope.launch(Main) {
                                                currentPinFocusRequester.requestFocus()
                                                focusManager.clearFocus()
                                                delay(200)
                                                currentPinFocusRequester.requestFocus()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .size(iconSizeXXS)
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("idCardCurrentPinRemoveIconButton"),
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                            contentDescription = "$clearButtonText $buttonName",
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .zIndex(2f)
                                        .focusable(true)
                                        .semantics {
                                            this.contentDescription = pinLengthRequirementText.lowercase()
                                            traversalIndex = 2f
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidCurrentPinDescriptionText"),
                                text = pinLengthRequirementText,
                                color =
                                    if (!isCurrentPinValid) {
                                        Red500
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        if (showNewPinField.value) {
                            Text(
                                text = newPinText,
                                modifier =
                                    modifier
                                        .focusable(false)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewPinCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SecurePinTextField(
                                    modifier =
                                        modifier
                                            .weight(1f)
                                            .zIndex(7f)
                                            .semantics {
                                                traversalIndex = 7f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("myEidNewPinTextField"),
                                    pin = newPinState,
                                    pinCodeLabel = pinCodeLabel,
                                    pinNumberFocusRequester = newPinFocusRequester,
                                    previousFocusRequester = newPinDescriptionFocusRequester,
                                    pinCodeTextEdited = null,
                                    trailingIconContentDescription = "$clearButtonText $buttonName",
                                    isError = !isNewPinValid,
                                    keyboardImeAction = ImeAction.Next,
                                    keyboardActions =
                                        KeyboardActions(
                                            onNext = {
                                                if (isNewPinValid) {
                                                    showCurrentPinField.value = false
                                                    showNewPinField.value = false
                                                    showNewRepeatPinField.value = true
                                                } else {
                                                    focusManager.clearFocus()
                                                }
                                            },
                                        ),
                                )
                                if (isTalkBackEnabled(context) && newPinState.value.isNotEmpty()) {
                                    IconButton(
                                        modifier =
                                            modifier
                                                .zIndex(8f)
                                                .align(Alignment.CenterVertically)
                                                .semantics {
                                                    traversalIndex = 8f
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidNewPinRemoveButton"),
                                        onClick = {
                                            newPinState.value = byteArrayOf()
                                            scope.launch(Main) {
                                                newPinFocusRequester.requestFocus()
                                                focusManager.clearFocus()
                                                delay(200)
                                                newPinFocusRequester.requestFocus()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .size(iconSizeXXS)
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("idCardNewPinRemoveIconButton"),
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                            contentDescription = "$clearButtonText $buttonName",
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier =
                                    modifier
                                        .focusRequester(newPinDescriptionFocusRequester)
                                        .fillMaxWidth()
                                        .zIndex(5f)
                                        .focusable(enabled = true)
                                        .focusTarget()
                                        .focusProperties { canFocus = true }
                                        .semantics {
                                            this.contentDescription =
                                                "$pinDifferentRequirementText $pinLengthRequirementText"
                                            traversalIndex = 5f
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewPinDescriptionText"),
                                text = "$pinDifferentRequirementText $pinLengthRequirementText",
                                color =
                                    if (!isNewPinValid) {
                                        Red500
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                style = MaterialTheme.typography.bodySmall,
                            )

                            if (!pinErrorText.value.isEmpty()) {
                                Text(
                                    modifier =
                                        modifier
                                            .zIndex(6f)
                                            .padding(vertical = XSPadding)
                                            .fillMaxWidth()
                                            .focusable(true)
                                            .semantics {
                                                traversalIndex = 6f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("myEidNewPinErrorText"),
                                    text = pinErrorText.value,
                                    color = Red500,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                            LaunchedEffect(Unit) {
                                if (isTalkBackEnabled(context)) {
                                    delay(1000)
                                    newPinDescriptionFocusRequester.requestFocus()
                                }
                            }
                        }

                        if (showNewRepeatPinField.value) {
                            Text(
                                text = newPinRepeatedText,
                                modifier =
                                    modifier
                                        .focusable(false)
                                        .semantics {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewPinRepeatCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Row(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                SecurePinTextField(
                                    modifier =
                                        modifier
                                            .weight(1f)
                                            .zIndex(11f)
                                            .semantics {
                                                traversalIndex = 11f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("myEidNewPinRepeatedTextField"),
                                    pin = newPinRepeatedState,
                                    pinCodeLabel = pinCodeLabel,
                                    pinNumberFocusRequester = newPinRepeatedFocusRequester,
                                    previousFocusRequester = newPinRepeatedDescriptionFocusRequester,
                                    pinCodeTextEdited = null,
                                    trailingIconContentDescription = "$clearButtonText $buttonName",
                                    isError = !isNewRepeatedPinValid,
                                )
                                if (isTalkBackEnabled(context) && newPinRepeatedState.value.isNotEmpty()) {
                                    IconButton(
                                        modifier =
                                            modifier
                                                .zIndex(12f)
                                                .align(Alignment.CenterVertically)
                                                .semantics {
                                                    traversalIndex = 12f
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidNewPinRepeatedRemoveButton"),
                                        onClick = {
                                            newPinRepeatedState.value = byteArrayOf()
                                            scope.launch(Main) {
                                                newPinRepeatedFocusRequester.requestFocus()
                                                focusManager.clearFocus()
                                                delay(200)
                                                newPinRepeatedFocusRequester.requestFocus()
                                            }
                                        },
                                    ) {
                                        Icon(
                                            modifier =
                                                modifier
                                                    .size(iconSizeXXS)
                                                    .semantics {
                                                        testTagsAsResourceId = true
                                                    }
                                                    .testTag("idCardNewPinRepeatedRemoveIconButton"),
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                            contentDescription = "$clearButtonText $buttonName",
                                        )
                                    }
                                }
                            }
                            Text(
                                modifier =
                                    modifier
                                        .focusRequester(newPinRepeatedDescriptionFocusRequester)
                                        .fillMaxWidth()
                                        .zIndex(9f)
                                        .focusable(enabled = true)
                                        .focusTarget()
                                        .focusProperties { canFocus = true }
                                        .semantics {
                                            this.contentDescription =
                                                "$pinDifferentRequirementText $pinLengthRequirementText".lowercase()
                                            traversalIndex = 9f
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewPinRepeatDescriptionText"),
                                text = "$pinDifferentRequirementText $pinLengthRequirementText",
                                color =
                                    if (!isNewRepeatedPinValid) {
                                        Red500
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                style = MaterialTheme.typography.bodySmall,
                            )

                            if (!pinErrorText.value.isEmpty()) {
                                Text(
                                    modifier =
                                        modifier
                                            .zIndex(10f)
                                            .padding(vertical = XSPadding)
                                            .fillMaxWidth()
                                            .focusable(true)
                                            .semantics {
                                                traversalIndex = 10f
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("myEidNewPinErrorText"),
                                    text = pinErrorText.value,
                                    color = Red500,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }

                            LaunchedEffect(Unit) {
                                if (isTalkBackEnabled(context)) {
                                    delay(1000)
                                    newPinRepeatedDescriptionFocusRequester.requestFocus()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
