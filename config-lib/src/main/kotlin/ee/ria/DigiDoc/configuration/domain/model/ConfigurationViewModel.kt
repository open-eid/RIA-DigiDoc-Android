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
