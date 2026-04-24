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
