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

package ee.ria.DigiDoc.domain.model

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.platform.app.InstrumentationRegistry
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapperImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class TextToSpeechWrapperTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    lateinit var textToSpeechWrapper: TextToSpeechWrapper

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        textToSpeechWrapper = TextToSpeechWrapperImpl(context)
    }

    @Test
    fun textToSpeechWrapper_initializeSuspend_success() =
        runBlocking {
            val result = textToSpeechWrapper.initializeSuspend()

            assertTrue(result)
        }

    @Test
    fun textToSpeechWrapper_getInstance_success() =
        runBlocking {
            textToSpeechWrapper.initializeSuspend()

            val currentInstance = textToSpeechWrapper.getInstance()

            assertNotNull(currentInstance)
        }

    @Test
    fun textToSpeechWrapper_shutdown_success() =
        runBlocking {
            textToSpeechWrapper.initializeSuspend()

            val currentInstance = textToSpeechWrapper.getInstance()

            assertNotNull(currentInstance)

            textToSpeechWrapper.shutdown()

            val shutdownInstance = textToSpeechWrapper.getInstance()

            assertNull(shutdownInstance)
        }
}
