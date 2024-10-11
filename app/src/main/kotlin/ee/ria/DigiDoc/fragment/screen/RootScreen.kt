@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RootScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)

    Scaffold(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                },
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding),
        ) {
            Box(
                modifier =
                    modifier
                        .padding(screenViewLargePadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .testTag("scrollView"),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.rooted_device),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier =
                        modifier
                            .padding(vertical = screenViewLargePadding)
                            .testTag("rootDevice"),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
