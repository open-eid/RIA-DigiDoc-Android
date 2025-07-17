@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.asFlow
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.getAccessibilityEventType
import ee.ria.DigiDoc.utils.accessibility.AccessibilityUtil.Companion.sendAccessibilityEvent
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.NFCViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NFCSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    nfcViewModel: NFCViewModel,
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val nfcDialogDefaultText = stringResource(id = R.string.signature_update_nfc_hold)
    var nfcDialogText by remember { mutableStateOf(nfcDialogDefaultText) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                context.getString(
                    error.first,
                    error.second,
                    error.third,
                )
                onError()
            }
        }
    }

    LaunchedEffect(Unit, nfcDialogText) {
        if (nfcDialogText.isNotEmpty()) {
            delay(500)
            sendAccessibilityEvent(
                context,
                getAccessibilityEventType(),
                nfcDialogText,
            )
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SPadding)
                .padding(vertical = LPadding)
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("signatureUpdateNFCContainer"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(vertical = MPadding)
                    .notAccessible(),
            horizontalArrangement = Arrangement.spacedBy(MPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier =
                    modifier
                        .size(iconSizeXXL)
                        .notAccessible(),
                imageVector = ImageVector.vectorResource(R.drawable.ic_m3_phonelink_ring_48dp_wght400),
                contentDescription = null,
            )
            Icon(
                modifier =
                    modifier
                        .size(iconSizeXXL)
                        .notAccessible(),
                imageVector = ImageVector.vectorResource(R.drawable.ic_m3_id_card_48dp_wght400),
                contentDescription = null,
            )
        }
        Text(
            text = nfcDialogText,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier =
                modifier
                    .wrapContentSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .padding(SPadding)
                    .testTag("nfcDialogText"),
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
