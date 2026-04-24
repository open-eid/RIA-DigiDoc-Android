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

package ee.ria.DigiDoc.viewmodel.shared

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import java.util.Locale

class SharedMenuViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    // FIXME:
    @Ignore("This test is ignored because it is flaky.")
    fun menuViewModel_init_successTtsInitialized() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isMenuViewModelTtsInitalizedObserver: Observer<Boolean> = mock()
            sharedMenuViewModel.isTtsInitialized.observeForever(isMenuViewModelTtsInitalizedObserver)

            delay(5000) // hack to wait for TTS initialization

            verify(isMenuViewModelTtsInitalizedObserver).onChanged(true)

            sharedMenuViewModel.isTtsInitialized.removeObserver(isMenuViewModelTtsInitalizedObserver)
        }

    @Test
    fun menuViewModel_isEstonianLanguageUsed_returnTrue() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            val estonianLocale = getLocale("et", "ET")

            val mockTextToSpeech = mock(TextToSpeech::class.java)
            val mockTextToSpeechVoice = mock(Voice::class.java)
            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)
            `when`(mockTtsWrapper.getInstance()).thenReturn(mockTextToSpeech)
            `when`(mockTextToSpeech.voice).thenReturn(mockTextToSpeechVoice)
            `when`(mockTextToSpeech.availableLanguages).thenReturn(setOf(estonianLocale))
            `when`(mockTextToSpeechVoice.locale).thenReturn(estonianLocale)

            Locale.setDefault(estonianLocale)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = sharedMenuViewModel.isEstonianLanguageUsed()

            assertTrue(isEstonianLanguageUsed)
        }

    @Test
    fun menuViewModel_isEstonianLanguageUsed_returnFalse() =
        runTest {
            val mockTtsWrapper: TextToSpeechWrapper = mock()

            val englishLocale = getLocale("en", "US")

            val mockTextToSpeech = mock(TextToSpeech::class.java)
            val mockTextToSpeechVoice = mock(Voice::class.java)
            `when`(mockTtsWrapper.initializeSuspend()).thenReturn(true)
            `when`(mockTtsWrapper.getInstance()).thenReturn(mockTextToSpeech)
            `when`(mockTextToSpeech.voice).thenReturn(mockTextToSpeechVoice)
            `when`(mockTextToSpeech.availableLanguages).thenReturn(setOf())
            `when`(mockTextToSpeechVoice.locale).thenReturn(englishLocale)

            Locale.setDefault(englishLocale)

            val sharedMenuViewModel = SharedMenuViewModel(mockTtsWrapper)
            val isEstonianLanguageUsed = sharedMenuViewModel.isEstonianLanguageUsed()

            assertFalse(isEstonianLanguageUsed)
        }
}
