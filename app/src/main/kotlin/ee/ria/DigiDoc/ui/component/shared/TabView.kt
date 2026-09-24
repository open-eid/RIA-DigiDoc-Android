// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R

private const val DISABLED_TAB_ALPHA = 0.38f

data class TabItem(
    val title: String,
    val enabled: Boolean = true,
    val content: @Composable () -> Unit,
)

@Composable
fun TabView(
    modifier: Modifier = Modifier,
    testTag: String = "tabView",
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit,
    tabItems: List<TabItem>,
) {
    Column(
        modifier =
            modifier
                .semantics {
                    testTagsAsResourceId = true
                }.testTag(testTag)
                .fillMaxSize(),
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
        ) {
            tabItems.forEachIndexed { index, tabItem ->
                val isSelected = selectedTabIndex == index
                val selectedTab =
                    stringResource(
                        R.string.signature_update_signature_selected_container_tab,
                        tabItem.title,
                        index + 1,
                        tabItems.size,
                    )
                val unselectedTab =
                    stringResource(
                        R.string.signature_update_signature_unselected_container_tab,
                        tabItem.title,
                        index + 1,
                        tabItems.size,
                    )

                Tab(
                    modifier =
                        modifier.semantics {
                            contentDescription = ""
                            stateDescription = if (isSelected) selectedTab else unselectedTab
                            this.role = androidx.compose.ui.semantics.Role.Button
                        },
                    text = { Text(text = tabItem.title) },
                    selected = isSelected,
                    enabled = tabItem.enabled,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor =
                        if (tabItem.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_TAB_ALPHA)
                        },
                )
            }
        }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            tabItems[selectedTabIndex].content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabViewPreview() {
    TabView(
        tabItems =
            listOf(
                TabItem("Tab 1") {},
                TabItem("Tab 2", enabled = false) {},
            ),
        onTabSelected = {},
    )
}
