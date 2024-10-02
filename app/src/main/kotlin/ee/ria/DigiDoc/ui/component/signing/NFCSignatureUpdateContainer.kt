@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import android.view.accessibility.AccessibilityEvent.TYPE_ANNOUNCEMENT
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PrimaryButton
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewLargePadding
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.NFCViewModel

@Composable
fun NFCSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    nfcViewModel: NFCViewModel,
    onCancelButtonClick: () -> Unit = {},
) {
    val context = LocalContext.current

    val nfcDialogDefaultText = stringResource(id = R.string.signature_update_nfc_hold)
    var nfcDialogText by remember { mutableStateOf(nfcDialogDefaultText) }

    LaunchedEffect(nfcViewModel.message) {
        nfcViewModel.message.asFlow().collect { message ->
            message?.let {
                nfcDialogText = context.getString(message)
            }
        }
    }

    LaunchedEffect(nfcViewModel.errorState) {
        nfcViewModel.errorState.asFlow().collect { error ->
            error?.let {
                onCancelButtonClick()
            }
        }
    }

    LaunchedEffect(nfcDialogText) {
        if (nfcDialogText.isNotEmpty()) {
            AccessibilityUtil.sendAccessibilityEvent(
                context,
                TYPE_ANNOUNCEMENT,
                nfcDialogText,
            )
        }
    }

    Column(
        modifier = modifier.padding(screenViewLargePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_icon_nfc),
            contentDescription = null,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(screenViewLargePadding)
                    .notAccessible(),
        )
        Text(
            text = nfcDialogText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .wrapContentSize()
                    .padding(screenViewLargePadding),
        )

        PrimaryButton(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = screenViewLargePadding),
            title = R.string.cancel_button,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Red500,
            onClickItem = onCancelButtonClick,
            isSubButton = true,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NFCSignatureUpdateContainerPreview() {
    val nfcViewModel: NFCViewModel = hiltViewModel()
    RIADigiDocTheme {
        NFCSignatureUpdateContainer(
            nfcViewModel = nfcViewModel,
        )
    }
}
