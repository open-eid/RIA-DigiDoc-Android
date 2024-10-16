@file:Suppress("PackageName")

package ee.ria.DigiDoc.manager

import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityManagerImpl
    @Inject
    constructor() : ActivityManager {
        private val _shouldRecreateActivity = MutableLiveData(false)
        override val shouldRecreateActivity: LiveData<Boolean> get() = _shouldRecreateActivity
        private val _shouldResetLogging = MutableLiveData(true)
        override val shouldResetLogging: LiveData<Boolean> get() = _shouldResetLogging

        override fun setShouldRecreateActivity(shouldRecreate: Boolean) {
            _shouldRecreateActivity.postValue(shouldRecreate)
        }

        override fun setShouldResetLogging(shouldResetLogging: Boolean) {
            _shouldResetLogging.postValue(shouldResetLogging)
        }

        override fun recreateActivity(mainActivity: ComponentActivity) {
            mainActivity.recreate()
        }
    }
