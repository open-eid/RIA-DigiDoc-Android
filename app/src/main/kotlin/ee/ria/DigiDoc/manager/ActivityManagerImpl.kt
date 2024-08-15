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

        override fun setShouldRecreateActivity(shouldRecreate: Boolean) {
            _shouldRecreateActivity.postValue(shouldRecreate)
        }

        override fun recreateActivity(mainActivity: ComponentActivity) {
            mainActivity.recreate()
        }
    }
