@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.info.InfoComponent
import ee.ria.DigiDoc.ui.component.info.InfoComponentItem
import ee.ria.DigiDoc.ui.component.menu.SettingsMenuBottomSheet
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure
import ee.ria.DigiDoc.utils.snackbar.SnackBarManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarScope = rememberCoroutineScope()

    val messages by SnackBarManager.messages.collectAsState(emptyList())

    val isSettingsMenuBottomSheetVisible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(messages) {
        messages.forEach { message ->
            snackBarScope.launch {
                snackBarHostState.showSnackbar(message)
            }
            SnackBarManager.removeMessage(message)
        }
    }

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("infoScreen"),
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_about_title,
                onLeftButtonClick = {
                    navController.navigateUp()
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
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .testTag("scrollView"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.main_about_fonds),
                alignment = Alignment.Center,
                modifier = modifier.wrapContentSize(),
                contentDescription = stringResource(id = R.string.main_about_digidoc_and_el_logos),
            )
            Text(
                modifier =
                    modifier
                        .padding(
                            start = screenViewLargePadding,
                            top = screenViewLargePadding,
                            end = screenViewLargePadding,
                        )
                        .testTag("mainAboutRiaDigiDocVersionTitle"),
                text =
                    String.format(
                        stringResource(id = R.string.main_about_ria_digidoc_version_title),
                        BuildConfig.VERSION_NAME,
                    ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            start = screenViewLargePadding,
                            top = screenViewLargePadding,
                            end = screenViewLargePadding,
                        ),
                text = stringResource(id = R.string.main_about_software_developed_by_title),
                textStyle =
                    TextStyle(
                        textAlign = TextAlign.Center,
                    ),
            )
            DynamicText(
                modifier =
                    modifier
                        .padding(
                            start = screenViewLargePadding,
                            top = screenViewLargePadding,
                            end = screenViewLargePadding,
                        ),
                text = stringResource(id = R.string.main_about_contact_information_title),
                textStyle =
                    TextStyle(
                        textAlign = TextAlign.Center,
                    ),
            )
            Text(
                modifier =
                    modifier.padding(
                        start = screenViewLargePadding,
                        top = screenViewExtraLargePadding,
                        end = screenViewLargePadding,
                    ),
                text =
                    String.format(
                        stringResource(id = R.string.main_about_licenses_title),
                        BuildConfig.VERSION_NAME,
                    ),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            InfoComponentItem().componentItems().forEachIndexed { _, componentItem ->
                InfoComponent(
                    modifier = modifier,
                    name = componentItem.name,
                    licenseName = componentItem.licenseName,
                    licenseUrl = componentItem.licenseUrl,
                )
            }
            InvisibleElement(modifier = modifier)
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InfoScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        InfoScreen(
            navController = navController,
        )
    }
}
