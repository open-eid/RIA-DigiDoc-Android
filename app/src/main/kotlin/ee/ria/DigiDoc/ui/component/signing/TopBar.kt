@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.annotation.StringRes
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TopBar(
    modifier: Modifier,
    @StringRes title: Int,
    onBackButtonClick: () -> Unit = {},
) {
    val headingFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var headingTextLoaded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier =
            modifier
                .semantics {
                    isTraversalGroup = true
                    testTagsAsResourceId = true
                }
                .testTag("toolbar"),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.background,
            ),
        title = {
            PreventResize {
                Text(
                    text = stringResource(id = title),
                    maxLines = 1,
                    modifier =
                        modifier
                            .semantics {
                                heading()
                            }
                            .focusRequester(headingFocusRequester)
                            .focusable(enabled = true)
                            .focusTarget()
                            .focusProperties { canFocus = true }
                            .onGloballyPositioned {
                                if (!headingTextLoaded) {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        headingFocusRequester.requestFocus()
                                        focusManager.clearFocus()
                                        delay(200)
                                        headingFocusRequester.requestFocus()
                                        headingTextLoaded = true
                                    }
                                }
                            },
                )
            }
        },
        navigationIcon = {
            IconButton(
                modifier = Modifier.testTag("toolBarBackButton"),
                onClick = onBackButtonClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.background,
                )
            }
        },
    )
}
