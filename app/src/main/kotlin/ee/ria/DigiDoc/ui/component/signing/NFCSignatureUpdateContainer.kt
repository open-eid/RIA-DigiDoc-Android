/*
 * Copyright 2017 - 2026 Riigi Infosüsteemi Amet
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.LPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXL
import ee.ria.DigiDoc.ui.theme.Dimensions.invisibleElementHeight
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.extensions.notAccessible
import ee.ria.DigiDoc.viewmodel.NFCViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NFCSignatureUpdateContainer(
    modifier: Modifier = Modifier,
    nfcViewModel: NFCViewModel,
    onError: () -> Unit = {},
) {
    val context = LocalContext.current

    val defaultMessage = stringResource(id = R.string.signature_update_nfc_hold)
    var message by remember { mutableStateOf(defaultMessage) }

    val errorState by nfcViewModel.errorState.collectAsStateWithLifecycle()

    LaunchedEffect(nfcViewModel.message) {
        nfcViewModel.message.asFlow().collect { messageRes ->
            messageRes?.let { message = context.getString(it) }
        }
    }

    LaunchedEffect(errorState) {
        errorState?.let { error ->
            context.getString(error.message)
            onError()
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
        NfcProcessIcons()
        NfcStatusMessage(message = message)
    }
}

@Composable
private fun NfcProcessIcons() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = MPadding)
                .notAccessible(),
        horizontalArrangement = Arrangement.spacedBy(MPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NfcProcessIcon(R.drawable.ic_m3_phonelink_ring_48dp_wght400)
        NfcProcessIcon(R.drawable.ic_m3_id_card_48dp_wght400)
    }
}

@Composable
private fun NfcProcessIcon(drawableRes: Int) {
    Icon(
        modifier =
            Modifier
                .size(iconSizeXXL)
                .notAccessible(),
        imageVector = ImageVector.vectorResource(drawableRes),
        contentDescription = null,
    )
}

@Composable
private fun NfcStatusMessage(message: String) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Normal,
            modifier =
                Modifier
                    .wrapContentSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .padding(SPadding)
                    .testTag("nfcDialogText"),
        )
        NfcStatusAnnouncer(message = message)
    }
}

@Composable
private fun NfcStatusAnnouncer(message: String) {
    var announcement by remember { mutableStateOf("") }
    var isInitialMessage by remember { mutableStateOf(true) }

    LaunchedEffect(message) {
        if (isInitialMessage) {
            isInitialMessage = false
        } else {
            announcement = message
        }
    }

    Box(
        modifier =
            Modifier
                .size(invisibleElementHeight)
                .semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = announcement
                },
    )
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
