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

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.idcard.CodeType
import ee.ria.DigiDoc.idcard.PaceTunnelException
import ee.ria.DigiDoc.smartcardreader.ApduResponseException
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.PrimaryOutlinedButton
import ee.ria.DigiDoc.ui.component.shared.SecurePinTextField
import ee.ria.DigiDoc.ui.component.shared.StatusSnackbarHost
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.NFCSignatureUpdateContainer
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.utils.snackbar.SnackbarType
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
    val focusManager = LocalFocusManager.current

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val idCardData by sharedMyEidViewModel.idCardData.asFlow().collectAsState(null)

    val content by sharedMyEidViewModel.pinScreenContent.collectAsState()

    val currentPinState = sharedMyEidViewModel.currentPinState
    val newPinState = sharedMyEidViewModel.newPinState
    val newPinRepeatedState = sharedMyEidViewModel.newPinRepeatedState

    val showCurrentPinField = sharedMyEidViewModel.showCurrentPinField
    val showNewPinField = sharedMyEidViewModel.showNewPinField
    val showNewRepeatPinField = sharedMyEidViewModel.showNewRepeatPinField

    val pinErrorText = rememberSaveable { mutableStateOf("") }

    val actionContinue = stringResource(R.string.action_continue)

    val codeType = content?.codeType ?: CodeType.PIN1
    val isForgottenPin = content?.isForgottenPin == true
    val title = stringResource(content?.title ?: R.string.myeid_pin_change_title, codeType.name)

    val currentPinCodeType =
        if (isForgottenPin && showCurrentPinField.value) {
            CodeType.PUK
        } else {
            codeType
        }

    val pinCodeLabel =
        stringResource(
            id = R.string.signature_update_nfc_pin,
            currentPinCodeType.name,
        )

    val showNFCScreen = remember { mutableStateOf(false) }

    val pinDifferentRequirementText =
        stringResource(
            R.string.myeid_new_pin_different_requirement,
            codeType,
            codeType,
        )

    val pinLengthRequirementText =
        "${stringResource(
            R.string.id_card_sign_pin_invalid_length,
            currentPinCodeType.name,
            sharedMyEidViewModel.getPinCodeMinimumLength(
                currentPinCodeType,
            ),
            Constant.MyEID.PIN_MAXIMUM_LENGTH,
        )}. ${if (isForgottenPin && showCurrentPinField.value) {
            stringResource(R.string.myeid_puk_info)
        } else {
            ""
        }
        }"

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
            sendAccessibilityEvent(
                context,
                getAccessibilityEventType(),
                pinErrorText.value.lowercase(),
            )
        }
    }

    LaunchedEffect(pinChangingState) {
        if (pinChangingState == true) {
            sharedMyEidViewModel.resetPinChangingState()
            resetPins()
            showMessage(pinChangedSuccess, SnackbarType.SUCCESS)
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

    BackHandler {
        if (showNFCScreen.value) {
            showNFCScreen.value = false
            resetToBeginning()
        } else {
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
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("myEidPinScreen"),
        snackbarHost = { StatusSnackbarHost() },
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
                    if (showNFCScreen.value) {
                        showNFCScreen.value = false
                        resetToBeginning()
                    } else {
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
                    showNFCScreen.value = true

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
                    }.testTag("myEidPinContainer"),
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
                                }.focusable(enabled = true)
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
                                        }.testTag("myEidPinCurrentPinCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            SecurePinTextField(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding)
                                        .zIndex(3f)
                                        .semantics {
                                            traversalIndex = 3f
                                            testTagsAsResourceId = true
                                        }.testTag("myEidCurrentPinTextField"),
                                pin = currentPinState,
                                label = pinCodeLabel,
                                focusRequester = currentPinFocusRequester,
                                pinCodeTextEdited = null,
                                isError = !isCurrentPinValid,
                                keyboardImeAction = ImeAction.Next,
                                removeIconTestTag = "myEidCurrentPinRemoveButton",
                                onDone = {
                                    if (isCurrentPinValid) {
                                        showCurrentPinField.value = false
                                        showNewRepeatPinField.value = false
                                        showNewPinField.value = true
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                },
                            )
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
                                        }.testTag("myEidCurrentPinDescriptionText"),
                                text = pinLengthRequirementText,
                                color =
                                    if (!isCurrentPinValid) {
                                        MaterialTheme.colorScheme.error
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
                                        }.testTag("myEidNewPinCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            SecurePinTextField(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding)
                                        .zIndex(7f)
                                        .semantics {
                                            traversalIndex = 7f
                                            testTagsAsResourceId = true
                                        }.testTag("myEidNewPinTextField"),
                                pin = newPinState,
                                label = pinCodeLabel,
                                focusRequester = newPinFocusRequester,
                                pinCodeTextEdited = null,
                                isError = !isNewPinValid,
                                keyboardImeAction = ImeAction.Next,
                                removeIconTestTag = "myEidNewPinRemoveButton",
                                onDone = {
                                    if (isNewPinValid) {
                                        showCurrentPinField.value = false
                                        showNewPinField.value = false
                                        showNewRepeatPinField.value = true
                                    } else {
                                        focusManager.clearFocus()
                                    }
                                },
                            )
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
                                        }.testTag("myEidNewPinDescriptionText"),
                                text = "$pinDifferentRequirementText $pinLengthRequirementText",
                                color =
                                    if (!isNewPinValid) {
                                        MaterialTheme.colorScheme.error
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
                                            }.testTag("myEidNewPinErrorText"),
                                    text = pinErrorText.value,
                                    color = MaterialTheme.colorScheme.error,
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
                                        }.testTag("myEidNewPinRepeatCodeTitle")
                                        .notAccessible(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            SecurePinTextField(
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .padding(top = MPadding)
                                        .zIndex(11f)
                                        .semantics {
                                            traversalIndex = 11f
                                            testTagsAsResourceId = true
                                        }.testTag("myEidNewPinRepeatedTextField"),
                                pin = newPinRepeatedState,
                                label = pinCodeLabel,
                                focusRequester = newPinRepeatedFocusRequester,
                                pinCodeTextEdited = null,
                                isError = !isNewRepeatedPinValid,
                                removeIconTestTag = "myEidNewPinRepeatedRemoveButton",
                            )
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
                                        }.testTag("myEidNewPinRepeatDescriptionText"),
                                text = "$pinDifferentRequirementText $pinLengthRequirementText",
                                color =
                                    if (!isNewRepeatedPinValid) {
                                        MaterialTheme.colorScheme.error
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
                                            }.testTag("myEidNewPinErrorText"),
                                    text = pinErrorText.value,
                                    color = MaterialTheme.colorScheme.error,
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
