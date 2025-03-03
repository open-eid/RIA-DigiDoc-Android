@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TabView(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit,
    tabItems: List<Pair<String, @Composable () -> Unit>>,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            tabItems.forEachIndexed { index, (title, _) ->
                Tab(
                    text = { Text(text = title) },
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
