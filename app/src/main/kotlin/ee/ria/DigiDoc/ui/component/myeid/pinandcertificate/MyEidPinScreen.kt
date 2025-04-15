@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.pinandcertificate

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.common.Constant
import ee.ria.DigiDoc.domain.model.pin.PinChoice
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.PrimaryOutlinedButton
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeM
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.addInvisibleElement
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.isTalkBackEnabled
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.removeInvisibleElement
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager.showMessage
import ee.ria.DigiDoc.viewmodel.MyEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedPinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidPinScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    myEidViewModel: MyEidViewModel = hiltViewModel(),
    sharedMenuViewModel: SharedMenuViewModel,
    sharedPinViewModel: SharedPinViewModel,
) {
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    val content by sharedPinViewModel.screenContent.collectAsState()

    var currentPin by remember { mutableStateOf(TextFieldValue("")) }
    var newPin by remember { mutableStateOf(TextFieldValue("")) }
    var newPinRepeated by remember { mutableStateOf(TextFieldValue("")) }

    var showCurrentPinField = rememberSaveable { mutableStateOf(true) }
    var showNewPinField = rememberSaveable { mutableStateOf(false) }
    var showNewRepeatPinField = rememberSaveable { mutableStateOf(false) }

    var actionContinue = stringResource(R.string.action_continue)

    var currentPinWithInvisibleSpaces = TextFieldValue(addInvisibleElement(currentPin.text))
    var newPinWithInvisibleSpaces = TextFieldValue(addInvisibleElement(newPin.text))
    var newPinRepeatedWithInvisibleSpaces = TextFieldValue(addInvisibleElement(newPinRepeated.text))

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    val title = content?.title ?: R.string.myeid_pin_change_title
    val pinChoice = content?.pinChoice ?: PinChoice.PIN1
    val isForgottenPin = content?.isForgottenPin == true

    val pinDifferentRequirementText =
        stringResource(
            R.string.myeid_new_pin_different_requirement,
            pinChoice,
            pinChoice,
        )

    val pinLengthRequirementText =
        stringResource(
            R.string.id_card_sign_pin_invalid_length,
            if (isForgottenPin && showCurrentPinField.value) {
                PinChoice.PUK.name
            } else {
                pinChoice.name
            },
            myEidViewModel.getPinCodeMinimumLength(
                if (isForgottenPin) {
                    PinChoice.PUK
                } else {
                    pinChoice
                },
            ),
            Constant.MyEID.PIN_MAXIMUM_LENGTH,
        )

    val isCurrentPinValid =
        myEidViewModel.isPinCodeLengthValid(
            if (isForgottenPin) {
                PinChoice.PUK
            } else {
                pinChoice
            },
            currentPin.text,
        )
    val isNewPinValid = myEidViewModel.isPinCodeLengthValid(pinChoice, newPin.text)
    val isNewRepeatedPinValid = myEidViewModel.isPinCodeLengthValid(pinChoice, newPinRepeated.text)

    val pinChangedSuccess =
        stringResource(
            if (isForgottenPin) {
                R.string.myeid_pin_unblocked_success
            } else {
                R.string.myeid_pin_changed_success
            },
            pinChoice,
        )

    BackHandler {
        sharedPinViewModel.resetScreenContent()
        navController.navigateUp()
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

                        newPinRepeated = TextFieldValue("")
                        newPinRepeatedWithInvisibleSpaces = TextFieldValue(text = "", selection = TextRange.Zero)
                    } else if (showNewPinField.value) {
                        showNewRepeatPinField.value = false
                        showNewPinField.value = false
                        showCurrentPinField.value = true

                        newPinRepeated = TextFieldValue("")
                        newPinRepeatedWithInvisibleSpaces = TextFieldValue(text = "", selection = TextRange.Zero)

                        newPin = TextFieldValue("")
                        newPinWithInvisibleSpaces =
                            TextFieldValue(
                                text = "",
                                selection = TextRange.Zero,
                            )
                    } else {
                        newPinRepeated = TextFieldValue("")
                        newPinRepeatedWithInvisibleSpaces = TextFieldValue(text = "", selection = TextRange.Zero)

                        newPin = TextFieldValue("")
                        newPinWithInvisibleSpaces = TextFieldValue(text = "", selection = TextRange.Zero)

                        currentPin = TextFieldValue("")
                        currentPinWithInvisibleSpaces = TextFieldValue(text = "", selection = TextRange.Zero)

                        sharedPinViewModel.resetScreenContent()

                        navController.navigateUp()
                    }
                },
                onRightSecondaryButtonClick = {
                    isSettingsMenuBottomSheetVisible.value = true
                },
            )
        },
        bottomBar = {
            if (showNewRepeatPinField.value) {
                PrimaryOutlinedButton(
                    modifier = modifier,
                    title =
                        if (isForgottenPin) {
                            R.string.myeid_pin_unblock_button
                        } else {
                            R.string.myeid_save_new_pin
                        },
                    titleExtra = pinChoice.name,
                    contentDescription = actionContinue,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface,
                    enabled = isNewRepeatedPinValid,
                ) {
                    navController.navigateUp()
                    showMessage(pinChangedSuccess)
                }
            } else {
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
                    if (currentPin.text.isEmpty()) {
                        showCurrentPinField.value = false
                        showNewRepeatPinField.value = false
                        showCurrentPinField.value = true
                    } else if (currentPin.text.isNotEmpty() && newPin.text.isEmpty()) {
                        showCurrentPinField.value = false
                        showNewRepeatPinField.value = false
                        showNewPinField.value = true
                    } else if (currentPin.text.isNotEmpty() &&
                        newPin.text.isNotEmpty() &&
                        newPinRepeated.text.isEmpty()
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
                        .padding(SPadding),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(title, pinChoice.name),
                    maxLines = 2,
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(SPadding)
                            .semantics { heading() }
                            .focusable(enabled = true)
                            .focusTarget()
                            .focusProperties { canFocus = true },
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
                            .padding(vertical = SPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(SPadding),
                ) {
                    if (showCurrentPinField.value) {
                        Text(
                            text =
                                if (isForgottenPin) {
                                    stringResource(
                                        R.string.myeid_current_pin_code_title,
                                        PinChoice.PUK,
                                    )
                                } else {
                                    stringResource(
                                        R.string.myeid_current_pin_code_title,
                                        pinChoice.name,
                                    )
                                },
                            modifier =
                                modifier
                                    .focusable(false)
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("myEidPinCurrentPinCodeTitle"),
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
                            OutlinedTextField(
                                value =
                                    if (!isTalkBackEnabled(context)) {
                                        currentPin
                                    } else {
                                        currentPinWithInvisibleSpaces.copy(
                                            selection = TextRange(currentPinWithInvisibleSpaces.text.length),
                                        )
                                    },
                                singleLine = true,
                                onValueChange = {
                                    if (it.text.isDigitsOnly() && it.text.length <= Constant.MyEID.PIN_MAXIMUM_LENGTH) {
                                        if (!isTalkBackEnabled(context)) {
                                            currentPin =
                                                it.copy(selection = TextRange(it.text.length))
                                        } else {
                                            val noInvisibleElement =
                                                TextFieldValue(removeInvisibleElement(it.text))
                                            currentPin =
                                                noInvisibleElement.copy(
                                                    selection =
                                                        TextRange(
                                                            noInvisibleElement.text.length,
                                                        ),
                                                )
                                        }
                                    }
                                },
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidCurrentPinTextField"),
                                trailingIcon = {
                                    val image =
                                        if (passwordVisible) {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                        } else {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                        }
                                    val description =
                                        if (passwordVisible) {
                                            stringResource(
                                                id = R.string.hide_password,
                                            )
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    IconButton(
                                        modifier =
                                            modifier
                                                .semantics { traversalIndex = 9f }
                                                .testTag("myEidCurrentPinPasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                visualTransformation =
                                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.NumberPassword,
                                    ),
                                isError = !isCurrentPinValid,
                            )
                            if (isTalkBackEnabled(context) && currentPin.text.isNotEmpty()) {
                                IconButton(
                                    modifier =
                                        modifier
                                            .align(Alignment.CenterVertically),
                                    onClick = { currentPin = TextFieldValue("") },
                                ) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .size(iconSizeXXS)
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidCurrentPinRemoveIconButton"),
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
                                    .focusable(true)
                                    .semantics { contentDescription = pinLengthRequirementText }
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
                            text = stringResource(R.string.myeid_new_pin_code_title, pinChoice.name),
                            modifier =
                                modifier
                                    .focusable(false)
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("myEidNewPinCodeTitle"),
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
                            OutlinedTextField(
                                value =
                                    if (!isTalkBackEnabled(context)) {
                                        newPin
                                    } else {
                                        newPinWithInvisibleSpaces.copy(
                                            selection = TextRange(newPinWithInvisibleSpaces.text.length),
                                        )
                                    },
                                singleLine = true,
                                onValueChange = {
                                    if (it.text.isDigitsOnly() && it.text.length <= Constant.MyEID.PIN_MAXIMUM_LENGTH) {
                                        if (!isTalkBackEnabled(context)) {
                                            newPin = it.copy(selection = TextRange(it.text.length))
                                        } else {
                                            val noInvisibleElement =
                                                TextFieldValue(removeInvisibleElement(it.text))
                                            newPin =
                                                noInvisibleElement.copy(
                                                    selection =
                                                        TextRange(
                                                            noInvisibleElement.text.length,
                                                        ),
                                                )
                                        }
                                    }
                                },
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewPinTextField"),
                                trailingIcon = {
                                    val image =
                                        if (passwordVisible) {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                        } else {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                        }
                                    val description =
                                        if (passwordVisible) {
                                            stringResource(
                                                id = R.string.hide_password,
                                            )
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    IconButton(
                                        modifier =
                                            modifier
                                                .semantics { traversalIndex = 9f }
                                                .testTag("myEidNewPinPasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                visualTransformation =
                                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.NumberPassword,
                                    ),
                                isError = !isNewPinValid,
                            )
                            if (isTalkBackEnabled(context) && newPin.text.isNotEmpty()) {
                                IconButton(
                                    modifier =
                                        modifier
                                            .align(Alignment.CenterVertically),
                                    onClick = { newPin = TextFieldValue("") },
                                ) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .size(iconSizeXXS)
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidNewPinRemoveIconButton"),
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
                                    .focusable(true)
                                    .semantics { contentDescription = pinLengthRequirementText }
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
                    }

                    if (showNewRepeatPinField.value) {
                        Text(
                            text = stringResource(R.string.myeid_repeat_new_pin_code_title, pinChoice.name),
                            modifier =
                                modifier
                                    .focusable(false)
                                    .semantics {
                                        testTagsAsResourceId = true
                                    }
                                    .testTag("myEidNewPinRepeatCodeTitle"),
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
                            OutlinedTextField(
                                value =
                                    if (!isTalkBackEnabled(context)) {
                                        newPinRepeated
                                    } else {
                                        newPinRepeatedWithInvisibleSpaces.copy(
                                            selection = TextRange(newPinRepeatedWithInvisibleSpaces.text.length),
                                        )
                                    },
                                singleLine = true,
                                onValueChange = {
                                    if (it.text.isDigitsOnly() && it.text.length <= Constant.MyEID.PIN_MAXIMUM_LENGTH) {
                                        if (!isTalkBackEnabled(context)) {
                                            newPinRepeated =
                                                it.copy(selection = TextRange(it.text.length))
                                        } else {
                                            val noInvisibleElement =
                                                TextFieldValue(removeInvisibleElement(it.text))
                                            newPinRepeated =
                                                noInvisibleElement.copy(
                                                    selection =
                                                        TextRange(
                                                            noInvisibleElement.text.length,
                                                        ),
                                                )
                                        }
                                    }
                                },
                                modifier =
                                    modifier
                                        .weight(1f)
                                        .semantics(mergeDescendants = true) {
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("myEidNewRepeatPinTextField"),
                                trailingIcon = {
                                    val image =
                                        if (passwordVisible) {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility)
                                        } else {
                                            ImageVector.vectorResource(id = R.drawable.ic_visibility_off)
                                        }
                                    val description =
                                        if (passwordVisible) {
                                            stringResource(
                                                id = R.string.hide_password,
                                            )
                                        } else {
                                            stringResource(id = R.string.show_password)
                                        }
                                    IconButton(
                                        modifier =
                                            modifier
                                                .semantics { traversalIndex = 9f }
                                                .testTag("myEidNewPinRepeatPasswordVisibleButton"),
                                        onClick = { passwordVisible = !passwordVisible },
                                    ) {
                                        Icon(imageVector = image, description)
                                    }
                                },
                                visualTransformation =
                                    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors =
                                    OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                keyboardOptions =
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done,
                                        keyboardType = KeyboardType.NumberPassword,
                                    ),
                                isError = !isNewRepeatedPinValid,
                            )
                            if (isTalkBackEnabled(context) && newPinRepeated.text.isNotEmpty()) {
                                IconButton(
                                    modifier =
                                        modifier
                                            .align(Alignment.CenterVertically),
                                    onClick = { newPinRepeated = TextFieldValue("") },
                                ) {
                                    Icon(
                                        modifier =
                                            modifier
                                                .size(iconSizeXXS)
                                                .semantics {
                                                    testTagsAsResourceId = true
                                                }
                                                .testTag("myEidNewPinRepeatRemoveIconButton"),
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
                                    .focusable(true)
                                    .semantics { contentDescription = pinLengthRequirementText }
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
                    }
                }
            }
        }
    }
}
