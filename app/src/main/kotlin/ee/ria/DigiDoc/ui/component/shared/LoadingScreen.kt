// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.ui.theme.Dimensions.MPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.loadingBarSize

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Scaffold(
        snackbarHost = { StatusSnackbarHost() },
    ) { innerPadding ->
        Surface(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusGroup()
                    .semantics {
                        testTagsAsResourceId = true
                    }.testTag("loadingScreen"),
        ) {
            LoadingIndicator(modifier = modifier)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContentLoadingScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Surface(
        modifier =
            modifier
                .padding(contentPadding)
                .fillMaxSize()
                .focusGroup()
                .semantics {
                    testTagsAsResourceId = true
                }.testTag("loadingScreen"),
    ) {
        LoadingIndicator(modifier = modifier)
    }
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(vertical = MPadding)
                .testTag("activityOverlay"),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier =
                modifier
                    .size(loadingBarSize)
                    .testTag("activityIndicator"),
        )
    }
}
