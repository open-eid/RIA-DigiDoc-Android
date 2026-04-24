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

package ee.ria.DigiDoc

import androidx.activity.ComponentActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.manager.ActivityManager
import ee.ria.DigiDoc.manager.ActivityManagerImpl
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ActivityManagerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockComponentActivity: ComponentActivity

    @Mock
    private lateinit var mockObserver: Observer<Boolean>

    private lateinit var activityManager: ActivityManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        activityManager = ActivityManagerImpl()
        doNothing().`when`(mockComponentActivity).recreate()
    }

    @Test
    fun activityManager_setShouldRecreateActivity_successChangingToTrue() {
        activityManager.shouldRecreateActivity.observeForever(mockObserver)

        activityManager.setShouldRecreateActivity(true)

        verify(mockObserver).onChanged(true)
    }

    @Test
    fun activityManager_setShouldRecreateActivity_initialValueFalse() {
        val value = activityManager.shouldRecreateActivity.value
        value?.let { assertFalse(it) }
    }

    @Test
    fun activityManager_recreateActivity_successRecreatingActivity() {
        activityManager.recreateActivity(mockComponentActivity)

        verify(mockComponentActivity, times(1)).recreate()
    }
}
