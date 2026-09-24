// SPDX-FileCopyrightText: Estonian Information System Authority
// SPDX-License-Identifier: LGPL-2.1-or-later

@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.common.Constant.Defaults.DEFAULT_UUID_VALUE
import ee.ria.DigiDoc.cryptolib.CDOC2Settings
import ee.ria.DigiDoc.domain.model.settings.CDOCSetting
import ee.ria.DigiDoc.domain.preferences.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class EncryptionServicesViewModel
    @Inject
    constructor(
        private val cdoc2Settings: CDOC2Settings,
        private val dataStore: DataStore,
    ) : ViewModel() {
        private val _useOnlineEncryption = MutableStateFlow(cdoc2Settings.getUseOnlineEncryption())
        val useOnlineEncryption = _useOnlineEncryption.asStateFlow()

        private val _selectedCDOC2Service = MutableStateFlow(dataStore.getCDOC2SelectedService(DEFAULT_UUID_VALUE))
        val selectedCDOC2Service = _selectedCDOC2Service.asStateFlow()

        private val _cdocSetting = MutableStateFlow(dataStore.getCdocSetting(false))
        val cdocSetting = _cdocSetting.asStateFlow()

        private val _cdoc2Uuid = MutableStateFlow(dataStore.getCDOC2UUID(selectedCDOC2Service.toString()))
        val cdoc2Uuid = _cdoc2Uuid.asStateFlow()

        private val _cdoc2FetchUrl = MutableStateFlow(dataStore.getCDOC2FetchURL(""))
        val cdoc2FetchUrl = _cdoc2FetchUrl.asStateFlow()

        private val _cdoc2PostUrl = MutableStateFlow(dataStore.getCDOC2PostURL(""))
        val cdoc2PostUrl = _cdoc2PostUrl.asStateFlow()

        fun setUseOnlineEncryption(value: Boolean) {
            dataStore.setUseOnlineEncryption(value)
            _useOnlineEncryption.value = value
        }

        fun setSelectedCDOC2Service(value: String) {
            dataStore.setCDOC2SelectedService(value)
            _selectedCDOC2Service.value = value
        }

        fun setCdocSetting(value: CDOCSetting) {
            dataStore.setCdocSetting(value)
            _cdocSetting.value = value
        }

        fun setCdoc2Uuid(value: String) {
            dataStore.setCDOC2UUID(value)
            _cdoc2Uuid.value = value
        }

        fun setCdoc2FetchUrl(value: String) {
            dataStore.setCDOC2FetchURL(value)
            _cdoc2FetchUrl.value = value
        }

        fun setCdoc2PostUrl(value: String) {
            dataStore.setCDOC2PostURL(value)
            _cdoc2PostUrl.value = value
        }
    }
