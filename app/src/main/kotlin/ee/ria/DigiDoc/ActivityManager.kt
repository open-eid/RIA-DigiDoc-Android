@file:Suppress("PackageName")

package ee.ria.DigiDoc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityManager
    @Inject
    constructor() {
        private val _shouldRecreateActivity = MutableLiveData(false)
        val shouldRecreateActivity: LiveData<Boolean> get() = _shouldRecreateActivity

        fun setShouldRecreateActivity(shouldRecreate: Boolean) {
            _shouldRecreateActivity.postValue(shouldRecreate)
        }

        fun recreateActivity(mainActivity: MainActivity) {
            mainActivity.recreate()
        }
    }
