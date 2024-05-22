@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.signing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import ee.ria.DigiDoc.ui.component.shared.PreventResize

@Composable
fun SigningBottomBar(
    modifier: Modifier,
    onSignClick: () -> Unit = {},
    onEncryptClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.background,
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        signingBottomNavigationItems(
            onSignClick = onSignClick,
            onEncryptClick = onEncryptClick,
            onShareClick = onShareClick,
        ).forEachIndexed { _, navigationItem ->
            Box(
                modifier
                    .weight(1f)
                    .semantics {
                        this.contentDescription = navigationItem.contentDescription
                    }
                    .focusGroup()
                    .clickable(
                        enabled = true,
                        onClick = navigationItem.onClick,
                    ),
            ) {
                Column(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .focusGroup()
                            .wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = navigationItem.icon,
                        contentDescription = null,
                        modifier =
                            modifier
                                .focusable(false)
                                .clearAndSetSemantics {},
                    )
                    PreventResize {
                        Text(
                            text = navigationItem.label,
                            overflow = TextOverflow.Visible,
                            modifier =
                                modifier
                                    .focusable(false)
                                    .clearAndSetSemantics {},
                        )
                    }
                }
            }
        }
    }
}
