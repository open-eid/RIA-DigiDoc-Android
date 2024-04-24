@file:Suppress("PackageName")

package ee.ria.DigiDoc.configuration.domain.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ee.ria.DigiDoc.configuration.ConfigurationProvider

class ConfigurationViewModel : ViewModel() {
    val workerResult: MutableLiveData<ConfigurationProvider> = MutableLiveData()
}
