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

package ee.ria.DigiDoc.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.fragment.screen.WebEidScreen
import ee.ria.DigiDoc.ui.component.webeid.WebEidBrowser
import ee.ria.DigiDoc.ui.component.webeid.WebEidBrowserBottomSheet
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.utils.WebEidBrowserCache
import ee.ria.DigiDoc.utils.WebEidOperation
import ee.ria.DigiDoc.utils.WebEidUriUtil
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.debugLog
import ee.ria.DigiDoc.utilsLib.logging.LoggingUtil.Companion.errorLog
import ee.ria.DigiDoc.viewmodel.WebEidViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedContainerViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedMenuViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel

private const val LOG_TAG = "WebEidFragment"

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WebEidFragment(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    webEidUri: Uri?,
    browserPackage: String? = null,
    viewModel: WebEidViewModel = hiltViewModel(),
    sharedSettingsViewModel: SharedSettingsViewModel = hiltViewModel(),
    sharedContainerViewModel: SharedContainerViewModel = hiltViewModel(),
    sharedMenuViewModel: SharedMenuViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current as Activity
    val context = LocalContext.current
    val browserSelectionUri by viewModel.browserSelectionUri.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.relyingPartyResponseEvents.collect { response ->
            val origin = viewModel.requestOrigin
            val isCertificateStep = viewModel.certificateRequest.value != null
            if (response.isCompletedOperation &&
                isCertificateStep &&
                origin != null &&
                !browserPackage.isNullOrEmpty()
            ) {
                WebEidBrowserCache.rememberResolved(origin, browserPackage)
            }
            val targetPackage = browserPackage ?: origin?.let { WebEidBrowserCache.recall(it) }
            debugLog(LOG_TAG, "Web eID response. Target browser $targetPackage")
            val browsers = if (targetPackage == null) installedBrowsers(context) else emptyList()
            if (browsers.size > 1) {
                viewModel.requestBrowserSelection(response.uri)
                return@collect
            }
            if (viewModel.startResponseDispatch()) {
                sendResponse(activity, response.uri, targetPackage ?: browsers.firstOrNull()?.packageName)
            }
        }
    }

    LaunchedEffect(webEidUri) {
        webEidUri?.let {
            when (WebEidUriUtil.getOperation(it)) {
                WebEidOperation.AUTH -> viewModel.handleAuth(it)
                WebEidOperation.CERT -> viewModel.handleCertificate(it)
                WebEidOperation.SIGN -> viewModel.handleSign(it)
                null -> viewModel.handleUnknown(it)
            }
        }
    }

    browserSelectionUri?.let { responseUri ->
        val browsers = remember { installedBrowsers(context) }
        val isAuthentication = webEidUri?.let { WebEidUriUtil.getOperation(it) } == WebEidOperation.AUTH
        WebEidBrowserBottomSheet(
            browsers = browsers,
            message =
                if (isAuthentication) {
                    R.string.web_eid_select_browser_message_auth
                } else {
                    R.string.web_eid_select_browser_message_signing
                },
            onSelect = { selectedPackage ->
                debugLog(LOG_TAG, "Web eID response. Selected browser $selectedPackage")
                val selectedOrigin = viewModel.requestOrigin
                if (viewModel.certificateRequest.value != null && selectedOrigin != null) {
                    WebEidBrowserCache.rememberSelected(selectedOrigin, selectedPackage)
                }
                if (viewModel.startResponseDispatch()) {
                    sendResponse(activity, responseUri, selectedPackage)
                }
            },
            onDismiss = { activity.finishAndRemoveTask() },
        )
    }

    Surface(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .semantics { testTagsAsResourceId = true }
                .testTag("webEidFragment"),
        color = MaterialTheme.colorScheme.background,
    ) {
        WebEidScreen(
            modifier = modifier,
            navController = navController,
            viewModel = viewModel,
            sharedSettingsViewModel = sharedSettingsViewModel,
            sharedContainerViewModel = sharedContainerViewModel,
            sharedMenuViewModel = sharedMenuViewModel,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun WebEidFragmentPreview() {
    RIADigiDocTheme {
        WebEidFragment(
            navController = rememberNavController(),
            webEidUri = null,
        )
    }
}

private fun sendResponse(
    activity: Activity,
    responseUri: Uri,
    browserPackage: String?,
) {
    try {
        activity.startActivity(responseIntent(responseUri, browserPackage))
    } catch (re: RuntimeException) {
        errorLog(LOG_TAG, "Unable to open Web eID response in $browserPackage", re)
        if (!browserPackage.isNullOrEmpty()) {
            try {
                activity.startActivity(responseIntent(responseUri, null))
            } catch (re2: RuntimeException) {
                errorLog(LOG_TAG, "Unable to open Web eID response", re2)
            }
        }
    }
    activity.finishAndRemoveTask()
}

private fun installedBrowsers(context: Context): List<WebEidBrowser> {
    val packageManager = context.packageManager
    val browseIntent =
        Intent(Intent.ACTION_VIEW, "https://example.com".toUri())
            .addCategory(Intent.CATEGORY_BROWSABLE)

    @Suppress("QueryPermissionsNeeded")
    return packageManager
        .queryIntentActivities(
            browseIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
        ).map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            WebEidBrowser(
                packageName = packageName,
                label = resolveInfo.loadLabel(packageManager).toString(),
                icon =
                    try {
                        packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
                    } catch (nnfe: PackageManager.NameNotFoundException) {
                        errorLog(LOG_TAG, "Unable to load browser icon for $packageName", nnfe)
                        null
                    },
            )
        }.distinctBy { it.packageName }
        .also { browsers ->
            debugLog(LOG_TAG, "Web eID browsers ${browsers.map { it.packageName }}")
        }
}

private fun responseIntent(
    responseUri: Uri,
    browserPackage: String?,
): Intent =
    Intent(Intent.ACTION_VIEW, responseUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        if (!browserPackage.isNullOrEmpty()) {
            setPackage(browserPackage)
        }
    }
