@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.myeid.mydata

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.focusable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.ui.component.shared.TagBadge
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Green_2_50
import ee.ria.DigiDoc.ui.theme.Green_2_700
import ee.ria.DigiDoc.ui.theme.RIADigiDocTheme
import ee.ria.DigiDoc.ui.theme.Red50
import ee.ria.DigiDoc.ui.theme.Red500
import ee.ria.DigiDoc.utils.extensions.notAccessible

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyEidMyDataItem(
    modifier: Modifier = Modifier,
    showTagBadge: Boolean,
    status: MyEidDocumentStatus? = null,
    @StringRes detailKey: Int,
    detailValue: String,
    contentDescription: String? = null,
    testTag: String = "",
) {
    val context = LocalContext.current

    val detailKeyText =
        if (detailKey != 0) {
            stringResource(id = detailKey)
        } else {
            ""
        }
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .focusable(true, interactionSource)
                .indication(interactionSource, LocalIndication.current)
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription ?: ""
                },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                modifier
                    .weight(1f)
                    .padding(vertical = SPadding),
        ) {
            Text(
                text = detailKeyText,
                modifier =
                    modifier
                        .focusable(false)
                        .semantics {
                            testTagsAsResourceId = true
                        }
                        .testTag(testTag)
                        .notAccessible(),
                color = MaterialTheme.colorScheme.onSecondary,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = detailValue,
                modifier =
                    modifier
                        .focusable(false)
                        .testTag(testTag)
                        .notAccessible(),
                textAlign = TextAlign.Start,
            )
        }

        if (showTagBadge && status != null) {
            TagBadge(
                modifier = modifier,
                text = status.getLocalized(context),
                backgroundColor =
                    if (status == MyEidDocumentStatus.VALID) {
                        Green_2_50
                    } else {
                        Red500
                    },
                contentColor =
                    if (status == MyEidDocumentStatus.VALID) {
                        Green_2_700
                    } else {
                        Red50
                    },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MyEidMyDataItemPreview() {
    RIADigiDocTheme {
        MyEidMyDataItem(
            detailKey = R.string.myeid_firstname,
            detailValue = "John Doe",
            showTagBadge = true,
            status = MyEidDocumentStatus.VALID,
        )
    }
}
