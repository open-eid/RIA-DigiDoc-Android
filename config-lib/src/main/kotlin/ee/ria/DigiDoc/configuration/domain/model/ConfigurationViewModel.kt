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

@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.configuration.provider.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import ee.ria.DigiDoc.network.proxy.ManualProxy
import ee.ria.DigiDoc.network.proxy.ProxySetting
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel
    @Inject
    constructor(
        private val repository: ConfigurationRepository,
    ) : ViewModel() {
        private val _configuration = MutableLiveData<ConfigurationProvider?>()
        val configuration: MutableLiveData<ConfigurationProvider?> = _configuration

        suspend fun fetchConfiguration(
            lastUpdate: Long,
            proxySetting: ProxySetting?,
            manualProxy: ManualProxy,
        ) {
            withContext(IO) {
                try {
                    val configurationProvider = repository.getCentralConfiguration(proxySetting, manualProxy)
                    val confUpdateDate = configurationProvider?.configurationUpdateDate
                    if (lastUpdate == 0L || (confUpdateDate != null && confUpdateDate.after(Date(lastUpdate)))) {
                        _configuration.postValue(configurationProvider)
                    }
                } catch (_: Exception) {
                    // Handle error
                }
            }
        }

        fun getConfiguration(): ConfigurationProvider? = repository.getConfiguration()
    }
