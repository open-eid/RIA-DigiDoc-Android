@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.fragment.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Black
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.utils.secure.SecureUtil.markAsSecure

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureScreen(
    modifier: Modifier = Modifier,
    onClickToFileChoosingScreen: () -> Unit = {},
    onClickToRecentDocumentsScreen: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = (context as Activity)
    markAsSecure(context, activity.window)
    val buttonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        buttonFocusRequester.requestFocus()
    }

    val chooseFileText =
        stringResource(
            id = R.string.signature_home_create_text,
        )
    val recentDocumentsText =
        stringResource(
            id = R.string.recent_documents_title,
        )

    RIADigiDocTheme {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = screenViewLargePadding,
                        vertical = screenViewExtraLargePadding,
                    )
                    .focusable()
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(id = R.string.signature_home_create_text),
                modifier =
                    modifier
                        .padding(vertical = screenViewLargePadding)
                        .notAccessible(),
                color = Black,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                modifier =
                    modifier
                        .focusProperties { canFocus = true }
                        .focusTarget()
                        .focusRequester(buttonFocusRequester)
                        .focusable()
                        .testTag("signatureHomeCreateButton"),
                title = R.string.signature_home_create_button,
                contentDescription = chooseFileText,
                onClickItem = onClickToFileChoosingScreen,
                isFocusable = true,
            )
            PrimaryButton(
                modifier =
                    modifier
                        .focusProperties { canFocus = true }
                        .focusable()
                        .testTag("signatureHomeRecentDocumentsButton"),
                title = R.string.main_home_recent,
                contentDescription = recentDocumentsText,
                onClickItem = onClickToRecentDocumentsScreen,
                isFocusable = true,
                isSubButton = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SignatureScreenPreview() {
    RIADigiDocTheme {
        SignatureScreen {}
    }
}
