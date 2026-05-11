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

@Composable
fun TabView(
    modifier: Modifier = Modifier,
    testTag: String = "tabView",
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit,
    tabItems: List<Pair<String, @Composable () -> Unit>>,
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
            tabItems.forEachIndexed { index, (title, _) ->
                val isSelected = selectedTabIndex == index
                val selectedTab =
                    stringResource(
                        R.string.signature_update_signature_selected_container_tab,
                        title,
                        index + 1,
                        tabItems.size,
                    )
                val unselectedTab =
                    stringResource(
                        R.string.signature_update_signature_unselected_container_tab,
                        title,
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
                    text = { Text(text = title) },
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            tabItems[selectedTabIndex].second()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TabViewPreview() {
    TabView(
        tabItems = listOf(Pair("Tab 1", {}), Pair("Tab 2", {})),
        onTabSelected = {},
    )
}
