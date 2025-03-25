@file:Suppress("PackageName", "FunctionName")

package ee.ria.DigiDoc.ui.component.settings.advanced.signingservices

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.asFlow
import androidx.navigation.NavHostController
import ee.ria.DigiDoc.R
import ee.ria.DigiDoc.domain.model.settings.TSASetting
import ee.ria.DigiDoc.ui.component.shared.InvisibleElement
import ee.ria.DigiDoc.ui.component.support.textFieldValueSaver
import ee.ria.DigiDoc.ui.theme.Dimensions.SPadding
import ee.ria.DigiDoc.ui.theme.Dimensions.XSBorder
import ee.ria.DigiDoc.ui.theme.Dimensions.XSPadding
import ee.ria.DigiDoc.ui.theme.buttonRoundedCornerShape
import ee.ria.DigiDoc.utils.Route
import ee.ria.DigiDoc.viewmodel.shared.SharedCertificateViewModel
import ee.ria.DigiDoc.viewmodel.shared.SharedSettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TimestampServicesComponent(
    modifier: Modifier = Modifier,
    sharedSettingsViewModel: SharedSettingsViewModel,
    sharedCertificateViewModel: SharedCertificateViewModel,
    navController: NavHostController,
) {
    val context = LocalContext.current

    val configuration = sharedSettingsViewModel.updatedConfiguration.value

    val getSettingsTsaUrl = sharedSettingsViewModel.dataStore::getSettingsTSAUrl
    val getTsaSetting = sharedSettingsViewModel.dataStore::getTsaSetting
    val setSettingsTsaUrl = sharedSettingsViewModel.dataStore::setSettingsTSAUrl
    val setTsaSetting = sharedSettingsViewModel.dataStore::setTsaSetting
    val defaultTsaServiceUrl = configuration?.tsaUrl ?: getSettingsTsaUrl()
    var settingsTsaServiceChoice = remember { mutableStateOf(getTsaSetting().name) }
    var settingsTsaServiceUrl by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                text = defaultTsaServiceUrl,
                selection = TextRange(defaultTsaServiceUrl.length),
            ),
        )
    }
    sharedSettingsViewModel.updateTsaData(settingsTsaServiceUrl.text, context)
    val issuedTo by sharedSettingsViewModel.tsaIssuedTo.asFlow().collectAsState(
        "",
    )
    val validTo by sharedSettingsViewModel.tsaValidTo.asFlow().collectAsState(
        "",
    )

    val tsaCertificate by sharedSettingsViewModel.tsaCertificate.asFlow().collectAsState(
        null,
    )

    val filePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
            onResult = { uri ->
                if (uri == null) {
                    navController.popBackStack()
                    return@rememberLauncherForActivityResult
                }
                CoroutineScope(Dispatchers.IO).launch {
                    sharedSettingsViewModel.handleTsaFile(uri)
                    withContext(Main) {
                        sharedSettingsViewModel.updateTsaData(settingsTsaServiceUrl.text, context)
                    }
                }
            },
        )

    var urlText by remember { mutableStateOf(defaultTsaServiceUrl) }

    val issuedToTitleText = stringResource(R.string.main_settings_timestamp_cert_issued_to_title)
    val validToTitleText = stringResource(R.string.main_settings_timestamp_cert_valid_to_title)
    val showCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_show_certificate_button)
    val addCertificateButtonText = stringResource(R.string.main_settings_timestamp_cert_add_certificate_button)
    val noCertificateFoundText = stringResource(R.string.main_settings_timestamp_cert_no_certificate_found)

    val clearButtonText = stringResource(R.string.clear_text)
    val buttonName = stringResource(id = R.string.button_name)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(SPadding)
                .padding(top = SPadding),
    ) {
        Text(
            text = stringResource(R.string.main_settings_tsa_url_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = modifier.padding(bottom = SPadding),
        )

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = XSPadding, bottom = SPadding),
            shape = buttonRoundedCornerShape,
            border =
                BorderStroke(
                    width = XSBorder,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Row(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(SPadding)
                        .clickable {
                            settingsTsaServiceChoice.value = TSASetting.DEFAULT.name
                            setTsaSetting(TSASetting.DEFAULT)
                        },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.main_settings_siva_default_access_title),
                    modifier = modifier.weight(1f),
                )
                RadioButton(
                    selected = settingsTsaServiceChoice.value == TSASetting.DEFAULT.name,
                    onClick = {
                        settingsTsaServiceChoice.value = TSASetting.DEFAULT.name
                        setTsaSetting(TSASetting.DEFAULT)
                    },
                )
            }
        }

        Card(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(top = XSPadding, bottom = SPadding)
                    .clickable {
                        settingsTsaServiceChoice.value = TSASetting.MANUAL.name
                        setTsaSetting(TSASetting.MANUAL)
                    },
            shape = buttonRoundedCornerShape,
            border =
                BorderStroke(
                    width = XSBorder,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Column(
                modifier = modifier.padding(SPadding),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.main_settings_siva_default_manual_access_title),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = modifier.weight(1f),
                    )
                    RadioButton(
                        selected = settingsTsaServiceChoice.value == TSASetting.MANUAL.name,
                        onClick = {
                            settingsTsaServiceChoice.value = TSASetting.MANUAL.name
                            setTsaSetting(TSASetting.MANUAL)
                        },
                    )
                }

                if (settingsTsaServiceChoice.value == TSASetting.MANUAL.name) {
                    Spacer(modifier = modifier.height(XSPadding))

                    OutlinedTextField(
                        enabled = settingsTsaServiceChoice.value == TSASetting.MANUAL.name,
                        value = settingsTsaServiceUrl,
                        singleLine = true,
                        onValueChange = {
                            settingsTsaServiceUrl = it
                            setSettingsTsaUrl(it.text)
                        },
                        shape = RectangleShape,
                        label = { Text(stringResource(R.string.main_settings_tsa_url_title)) },
                        modifier = modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { urlText = "" }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_icon_remove),
                                    contentDescription = "$clearButtonText $buttonName",
                                )
                            }
                        },
                        keyboardOptions =
                            KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Uri,
                            ),
                    )

                    Spacer(modifier = modifier.height(SPadding))

                    Text(
                        text = stringResource(R.string.main_settings_timestamp_cert_title),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    if (tsaCertificate != null) {
                        Text(
                            text = "$issuedToTitleText $issuedTo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "$validToTitleText $validTo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = noCertificateFoundText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = modifier.height(SPadding))

                    FlowRow(
                        modifier = modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (tsaCertificate != null) {
                            TextButton(onClick = {
                                tsaCertificate?.let {
                                    sharedCertificateViewModel.setCertificate(
                                        it,
                                    )
                                    navController.navigate(
                                        Route.CertificateDetail.route,
                                    )
                                }
                            }) {
                                Text(
                                    modifier =
                                        modifier
                                            .semantics {
                                                contentDescription =
                                                    "$showCertificateButtonText $buttonName"
                                                testTagsAsResourceId = true
                                            }
                                            .testTag("timestampServicesShowCertificateActionButton"),
                                    text = showCertificateButtonText,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        TextButton(onClick = {
                            filePicker.launch("*/*")
                        }) {
                            Text(
                                modifier =
                                    modifier
                                        .semantics {
                                            contentDescription =
                                                "$addCertificateButtonText $buttonName"
                                            testTagsAsResourceId = true
                                        }
                                        .testTag("timestampServicesAddCertificateActionButton"),
                                text = addCertificateButtonText,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }

        InvisibleElement(modifier = modifier)
    }
}
