@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.methods.SigningMethod
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.settings.SettingsSwitchItem
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.shared.TopBar
import ee.ria.DigiDoc.ui.component.signing.IdCardView
import ee.ria.DigiDoc.ui.component.signing.MobileIdView
import ee.ria.DigiDoc.ui.component.signing.NFCView
import ee.ria.DigiDoc.ui.component.signing.SmartIdView
import ee.ria.DigiDoc.ui.theme.Dimensions.MSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SignatureInputScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    sharedMenuViewModel: SharedMenuViewModel,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedContainerViewModel: SharedContainerViewModel,
) {
    val context = LocalActivity.current as Activity
    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }
    var isIdCardProcessStarted by rememberSaveable { mutableStateOf(false) }
    var isSigning by rememberSaveable { mutableStateOf(false) }
    val chosenMethod by remember {
        mutableStateOf(
            SigningMethod.entries.find {
                it.methodName == sharedSettingsViewModel.dataStore.getSignatureAddMethod()
            } ?: SigningMethod.NFC,
        )
    }
    val chosenMethodName by remember { mutableIntStateOf(chosenMethod.label) }
    var isValidToSign by remember { mutableStateOf(false) }
    var signAction by remember { mutableStateOf<() -> Unit>({}) }
    var cancelAction by remember { mutableStateOf<() -> Unit>({}) }

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val chosenMethodNameText = stringResource(chosenMethodName)
    val signatureMethodText = stringResource(R.string.signature_method)
    val rememberMeText = stringResource(R.string.signature_update_remember_me)
    var nfcSupported by remember { mutableStateOf(false) }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                modifier = modifier.padding(vertical = SPadding),
                hostState = snackBarHostState,
            )
        },
        topBar = {
            TopBar(
                modifier = modifier,
                sharedMenuViewModel = sharedMenuViewModel,
                title = null,
                leftIconContentDescription =
                    if (isSigning) {
                        R.string.signing_cancel
                    } else {
                        R.string.back
                    },
                onLeftButtonClick = {
                    if (isSigning) {
                        cancelAction()
                        isSigning = false
                    } else {
                        navController.navigateUp()
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

        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SPadding)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MSPadding),
        ) {
            Text(
                modifier =
                    modifier
                        .semantics {
                            heading()
                        },
                text = stringResource(R.string.signature_update_title),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
            )

            if (!isSigning && !isIdCardProcessStarted) {
                Column(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .padding(vertical = XSPadding),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = signatureMethodText,
                        modifier =
                            modifier
                                .focusable(false)
                                .notAccessible()
                                .testTag("signatureInputMethodTitle"),
                        color = MaterialTheme.colorScheme.onSecondary,
                        textAlign = TextAlign.Start,
                        style = MaterialTheme.typography.labelLarge,
                    )

                    Row(
                        modifier =
                            modifier
                                .fillMaxWidth()
                                .background(Color.Transparent)
                                .clickable {
                                    navController.navigate(
                                        Route.SignatureMethodScreen.route,
                                    )
                                },
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier =
                                modifier
                                    .semantics {
                                        contentDescription = "$signatureMethodText $chosenMethodNameText"
                                    },
                            text = chosenMethodNameText,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start,
                        )

                        Spacer(modifier = modifier.weight(1f))
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_m3_arrow_right_48dp_wght400),
                            contentDescription = null,
                            modifier =
                                modifier
                                    .padding(MSPadding)
                                    .size(iconSizeXXS)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                                    .notAccessible(),
                        )
                    }
                }
            }

            when (chosenMethod) {
                SigningMethod.MOBILE_ID ->
                    MobileIdView(
                        modifier = modifier,
                        activity = context,
                        onError = {
                            isSigning = false
                            cancelAction()
                        },
                        onSuccess = {
                            isSigning = false
                            navController.navigateUp()
                        },
                        isSigning = isSigning,
                        rememberMe = rememberMe,
                        sharedSettingsViewModel = sharedSettingsViewModel,
                        sharedContainerViewModel = sharedContainerViewModel,
                        isValidToSign = { isValid ->
                            isValidToSign = isValid
                        },
                        signAction = { action ->
                            signAction = action
                        },
                        cancelAction = { action ->
                            cancelAction = action
                        },
                    )

                SigningMethod.SMART_ID ->
                    SmartIdView(
                        modifier = modifier,
                        activity = context,
                        onError = {
                            isSigning = false
                            cancelAction()
                        },
                        onSuccess = {
                            isSigning = false
                            navController.navigateUp()
                        },
                        isSigning = isSigning,
                        rememberMe = rememberMe,
                        sharedSettingsViewModel = sharedSettingsViewModel,
                        sharedContainerViewModel = sharedContainerViewModel,
                        isValidToSign = { isValid ->
                            isValidToSign = isValid
                        },
                        signAction = { action ->
                            signAction = action
                        },
                        cancelAction = { action ->
                            cancelAction = action
                        },
                    )

                SigningMethod.ID_CARD ->
                    IdCardView(
                        modifier = modifier,
                        activity = context,
                        onError = {
                            isSigning = false
                            isIdCardProcessStarted = false
                            cancelAction()
                        },
                        onSuccess = {
                            isSigning = false
                            navController.navigateUp()
                        },
                        isStarted = { started ->
                            if (started) {
                                isIdCardProcessStarted = true
                            }
                        },
                        isSigning = isSigning,
                        sharedSettingsViewModel = sharedSettingsViewModel,
                        sharedContainerViewModel = sharedContainerViewModel,
                        isValidToSign = { isValid ->
                            isValidToSign = isValid
                        },
                        signAction = { action ->
                            signAction = {
                                isSigning = true
                                action()
                            }
                        },
                        cancelAction = { action ->
                            isSigning = false
                            cancelAction = action
                        },
                    )

                SigningMethod.NFC ->
                    NFCView(
                        modifier = modifier,
                        activity = context,
                        onError = {
                            isSigning = false
                            cancelAction()
                        },
                        onSuccess = {
                            isSigning = false
                            navController.navigateUp()
                        },
                        isSigning = isSigning,
                        rememberMe = rememberMe,
                        sharedSettingsViewModel = sharedSettingsViewModel,
                        sharedContainerViewModel = sharedContainerViewModel,
                        isSupported = { supported ->
                            nfcSupported = supported
                        },
                        isValidToSign = { isValid ->
                            isValidToSign = isValid
                        },
                        signAction = { action ->
                            signAction = action
                        },
                        cancelAction = { action ->
                            cancelAction = action
                        },
                    )
            }

            if (!isSigning && (chosenMethod != SigningMethod.NFC || nfcSupported)) {
                if (chosenMethod != SigningMethod.ID_CARD) {
                    SettingsSwitchItem(
                        modifier = modifier,
                        checked = rememberMe,
                        onCheckedChange = {
                            rememberMe = it
                        },
                        title = rememberMeText,
                        contentDescription = rememberMeText,
                        testTag = "signatureInputRememberMeSwitch",
                    )

                    if (rememberMe) {
                        Text(
                            text = stringResource(R.string.signature_update_remember_me_message),
                        )
                    }

                    Spacer(modifier = modifier.height(SPadding))
                }

                Button(
                    onClick = {
                        isSigning = true
                        signAction()
                    },
                    enabled = isValidToSign,
                    modifier =
                        modifier
                            .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Text(
                        text = stringResource(R.string.sign_button),
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
            }

            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureInputScreenPreview() {
    RIADigiDocTheme {
        SignatureInputScreen(
            navController = rememberNavController(),
            sharedMenuViewModel = hiltViewModel(),
            sharedSettingsViewModel = hiltViewModel(),
            sharedContainerViewModel = hiltViewModel(),
        )
    }
}
