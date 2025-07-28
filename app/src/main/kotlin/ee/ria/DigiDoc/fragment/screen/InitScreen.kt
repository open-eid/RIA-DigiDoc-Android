@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.menu.LanguageChoiceButtonGroup
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XLPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.Dimensions.zeroPadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.Route

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InitScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
) {
    Box(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("initScreen")
                .systemBarsPadding()
                .fillMaxWidth(),
    ) {
        Column(
            modifier =
                modifier
                    .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(id = R.drawable.image_eesti_shield),
                contentDescription = stringResource(id = R.string.app_name),
                modifier =
                    modifier
                        .height(iconSizeXXL)
                        .padding(
                            start = XLPadding,
                            top = LPadding,
                            bottom = zeroPadding,
                            end = XLPadding,
                        ),
            )
            Text(
                text = stringResource(id = R.string.digidoc_all_caps),
                style = MaterialTheme.typography.displayLarge,
                modifier =
                    modifier
                        .padding(bottom = LPadding)
                        .fillMaxWidth()
                        .wrapContentHeight(),
            )
            LanguageChoiceButtonGroup(
                modifier = modifier,
                onClickAction = {
                    navController.navigate(
                        Route.Home.route,
                    )
                },
            )
        }

        Text(
            text = stringResource(id = R.string.ria),
            style = MaterialTheme.typography.displaySmall,
            modifier =
                modifier
                    .padding(bottom = MPadding)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .wrapContentHeight(),
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun InitScreenPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        InitScreen(
            navController = navController,
        )
    }
}
