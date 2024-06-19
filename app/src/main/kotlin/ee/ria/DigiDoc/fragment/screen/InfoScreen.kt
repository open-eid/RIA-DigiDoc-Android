@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.BuildConfig
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.info.InfoComponent
import ee.ria.DigiDoc.ui.component.info.InfoComponentItem
import ee.ria.DigiDoc.ui.component.shared.BackButton
import ee.ria.DigiDoc.ui.component.shared.DynamicText
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewSmallPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                title = {
                    Text(
                        text = stringResource(id = R.string.main_about_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    BackButton(
                        onClickBack = {
                            navController.navigateUp()
                        },
                    )
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
                        top = screenViewSmallPadding,
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
                        top = screenViewSmallPadding,
                        end = screenViewLargePadding,
                    ),
                text = stringResource(id = R.string.main_about_software_developed_by_title),
                textAlign = TextAlign.Center,
            )
            DynamicText(
                modifier =
                    modifier.padding(
                        start = screenViewLargePadding,
                        top = screenViewSmallPadding,
                        end = screenViewLargePadding,
                    ),
                text = stringResource(id = R.string.main_about_contact_information_title),
                textAlign = TextAlign.Center,
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
