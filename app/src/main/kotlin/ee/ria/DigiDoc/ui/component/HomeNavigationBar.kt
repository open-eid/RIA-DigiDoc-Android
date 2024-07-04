@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import ee.ria.DigiDoc.ui.component.shared.PreventResize
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Transparent

@Composable
fun HomeNavigationBar(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = FocusRequester(),
    navController: NavHostController,
) {
    val buttonFocusRequester = remember { focusRequester }

    LaunchedEffect(Unit) {
        buttonFocusRequester.requestFocus()
    }

    var navigationSelectedItem by remember {
        mutableIntStateOf(0)
    }
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxWidth(),
    ) {
        HomeNavigationItem().bottomNavigationItems().forEachIndexed { index, navigationItem ->
            NavigationBarItem(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .background(
                            color =
                                if (index == navigationSelectedItem) {
                                    MaterialTheme.colorScheme.background
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                        )
                        .weight(1f)
                        .semantics {
                            this.contentDescription = navigationItem.contentDescription
                        }
                        .focusProperties { canFocus = true }
                        .focusTarget()
                        .focusRequester(buttonFocusRequester)
                        .focusable()
                        .focusGroup()
                        .clearAndSetSemantics {},
                alwaysShowLabel = true,
                colors =
                    NavigationBarItemColors(
                        selectedIconColor =
                            if (index == navigationSelectedItem) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.background
                            },
                        selectedTextColor =
                            if (index == navigationSelectedItem) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.background
                            },
                        selectedIndicatorColor = Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                        unselectedTextColor = MaterialTheme.colorScheme.tertiary,
                        disabledIconColor = MaterialTheme.colorScheme.tertiary,
                        disabledTextColor = MaterialTheme.colorScheme.tertiary,
                    ),
                selected = index == navigationSelectedItem,
                label = {
                    PreventResize {
                        Text(
                            text = navigationItem.label,
                            style = MaterialTheme.typography.bodyLarge,
                            overflow = TextOverflow.Visible,
                            modifier =
                                modifier
                                    .fillMaxWidth()
                                    .focusable(false)
                                    .clearAndSetSemantics {},
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                icon = {
                    Icon(
                        imageVector = navigationItem.icon,
                        contentDescription = navigationItem.contentDescription,
                        modifier =
                            modifier
                                .focusable(false)
                                .clearAndSetSemantics {},
                    )
                },
                onClick = {
                    navigationSelectedItem = index
                    navController.navigate(navigationItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeNavigationBarPreview() {
    val navController = rememberNavController()
    RIADigiDocTheme {
        HomeNavigationBar(
            navController = navController,
        )
    }
}
