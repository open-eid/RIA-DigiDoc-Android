@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.IdCardView
import ee.ria.DigiDoc.ui.component.MobileIdView
import ee.ria.DigiDoc.ui.component.PrimaryButton
import ee.ria.DigiDoc.ui.component.SignatureAddRadioGroup
import ee.ria.DigiDoc.ui.component.SmartIdView
import ee.ria.DigiDoc.ui.theme.Dimensions.alertDialogInnerPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.alertDialogOuterPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewVerticalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.textVerticalPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignatureScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    signatureAddController: NavHostController,
    onClickToSigningScreen: () -> Unit = {},
    isDialogOpen: Boolean = true,
) {
    val openSignatureDialog = remember { mutableStateOf(isDialogOpen) }
    val dismissDialog = {
        signatureAddController.navigate(Route.MobileId.route) {
            popUpTo(signatureAddController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
            openSignatureDialog.value = false
        }
    }
    RIADigiDocTheme {
        if (!openSignatureDialog.value) {
            BasicAlertDialog(
                onDismissRequest = dismissDialog,
            ) {
                Surface(
                    modifier =
                        modifier
                            .wrapContentHeight()
                            .wrapContentWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(alertDialogOuterPadding),
                ) {
                    Column(
                        modifier =
                            modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .padding(alertDialogInnerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SignatureAddRadioGroup(
                            modifier = modifier,
                            navController = signatureAddController,
                        )
                        NavHost(
                            navController = signatureAddController,
                            startDestination = Route.MobileId.route,
                        ) {
                            composable(route = Route.MobileId.route) {
                                MobileIdView(cancelButtonClick = dismissDialog)
                            }
                            composable(route = Route.SmartId.route) {
                                SmartIdView(cancelButtonClick = dismissDialog)
                            }
                            composable(route = Route.IdCard.route) {
                                IdCardView(cancelButtonClick = dismissDialog)
                            }
                            composable(route = Route.NFC.route) {
                                Text(
                                    "Signature with NFC",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(vertical = textVerticalPadding),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier =
                    modifier
                        .fillMaxSize()
                        .padding(horizontal = screenViewHorizontalPadding, vertical = screenViewVerticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(id = R.string.signature_home_create_text),
                    modifier =
                        modifier
                            .focusable(false)
                            .clearAndSetSemantics {},
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(
                    title = R.string.signature_home_create_button,
                    contentDescription = stringResource(id = R.string.signature_home_create_text),
                    onClickItem = onClickToSigningScreen,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureScreenPreview() {
    val navController = rememberNavController()
    val signatureAddController = rememberNavController()
    RIADigiDocTheme {
        SignatureScreen(
            navController = navController,
            signatureAddController = signatureAddController,
        )
    }
}
