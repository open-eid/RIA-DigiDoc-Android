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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.info.InfoComponent
import ee.ria.DigiDoc.ui.component.info.InfoComponentItem
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.component.signing.TopBar
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure

@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(
                modifier = modifier,
                title = R.string.main_about_title,
                onBackButtonClick = {
                    navController.navigateUp()
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
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
                    modifier.padding(
                        start = screenViewLargePadding,
                        top = screenViewLargePadding,
                        end = screenViewLargePadding,
                    ),
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
                    modifier.padding(
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
                    modifier.padding(
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
                    name = componentItem.name,
                    licenseName = componentItem.licenseName,
                    licenseUrl = componentItem.licenseUrl,
                )
            }
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
