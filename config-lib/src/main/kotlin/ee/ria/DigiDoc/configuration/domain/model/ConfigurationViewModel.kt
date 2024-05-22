@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.configuration.ConfigurationProvider
import ee.ria.DigiDoc.configuration.repository.ConfigurationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel
    @Inject
    constructor(
        private val repository: ConfigurationRepository,
    ) : ViewModel() {
        private val _configuration = MutableLiveData<ConfigurationProvider>()
        val configuration: MutableLiveData<ConfigurationProvider> = _configuration

        suspend fun fetchConfiguration(
            context: Context,
            lastUpdate: Long,
        ) {
            CoroutineScope(IO).launch {
                try {
                    val configurationProvider = repository.getCentralConfiguration(context)
                    val confUpdateDate = configurationProvider?.configurationUpdateDate
                    if (lastUpdate == 0L || (confUpdateDate != null && confUpdateDate.after(Date(lastUpdate)))) {
                        _configuration.postValue(configurationProvider)
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        suspend fun getConfiguration(): ConfigurationProvider? {
            return repository.getConfiguration()
        }
    }
