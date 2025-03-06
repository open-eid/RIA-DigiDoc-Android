@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.shared

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.iconSizeXXS
import ee.ria.DigiDoc.ui.theme.Dimensions.itemSpacingPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.screenViewExtraLargePadding
import ee.ria.DigiDoc.utils.extensions.notAccessible

@Composable
fun ExpandableButton(
    modifier: Modifier,
    @StringRes title: Int,
    detailText: String,
    contentDescription: String,
) {
    var isExpanded by remember { mutableStateOf(false) }

    val buttonName = stringResource(id = R.string.button_name)

    Box(
        modifier =
            modifier
                .padding(top = XSPadding)
                .animateContentSize()
                .testTag("signersCertificateTechnicalInformationContainerView"),
    ) {
        Column(
            modifier
                .semantics {
                    this.contentDescription = "$contentDescription, $buttonName"
                }
                .clickable {
                    isExpanded = !isExpanded
                },
        ) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .notAccessible(),
            ) {
                Box(
                    modifier = modifier.notAccessible(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector =
                            if (isExpanded) {
                                ImageVector.vectorResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
                            } else {
                                ImageVector.vectorResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
                            },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier =
                            modifier
                                .size(iconSizeXXS)
                                .notAccessible(),
                    )
                }
                Spacer(modifier = modifier.padding(horizontal = itemSpacingPadding))
                Text(
                    modifier =
                        modifier
                            .testTag("signersCertificateTechnicalInformationButtonTitle"),
                    text = stringResource(id = title),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            if (isExpanded) {
                Text(
                    text = detailText,
                    modifier =
                        modifier
                            .padding(
                                horizontal = itemSpacingPadding,
                                vertical = screenViewExtraLargePadding,
                            )
                            .focusable()
                            .testTag("signersCertificateTechnicalInformationText"),
                )
            }
        }
    }
}
