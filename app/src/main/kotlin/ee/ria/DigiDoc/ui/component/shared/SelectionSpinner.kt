@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.ui.theme.Dimensions.countryHorizontalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.countryIconPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.countryVerticalPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.noBorderStroke
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme

@Composable
fun SelectionSpinner(
    list: Array<String>,
    preselected: Int,
    onSelectionChanged: (item: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf(preselected) }
    var expanded by remember { mutableStateOf(false) } // initial value

    OutlinedCard(
        modifier =
            modifier.clickable {
                expanded = !expanded
            },
        border = BorderStroke(noBorderStroke, Color.Transparent),
        shape = RectangleShape,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = list.get(selected),
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    modifier
                        .weight(1f)
                        .padding(horizontal = countryHorizontalPadding, vertical = countryVerticalPadding),
            )
            Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.padding(countryIconPadding))

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = modifier.fillMaxWidth(),
            ) {
                list.forEachIndexed { index, listEntry ->

                    DropdownMenuItem(
                        onClick = {
                            selected = index
                            expanded = false
                            onSelectionChanged(selected)
                        },
                        text = {
                            Text(
                                text = listEntry,
                                style = MaterialTheme.typography.titleLarge,
                                modifier =
                                    modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Start),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SpinnerSample_Preview() {
    RIADigiDocTheme {
        val list = arrayOf("Estonia", "Lithuania", "Latvia")

        SelectionSpinner(
            list,
            preselected = 1,
            onSelectionChanged = { },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
