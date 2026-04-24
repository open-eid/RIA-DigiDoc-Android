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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import ee.ria.DigiDoc.utilsLib.locale.LocaleUtil.getLocale
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SharedMenuViewModel
    @Inject
    constructor(
        private val textToSpeechWrapper: TextToSpeechWrapper,
    ) : ViewModel() {
        private var _isTtsInitialized = MutableLiveData(false)
        val isTtsInitialized: LiveData<Boolean> = _isTtsInitialized

        init {
            viewModelScope.launch {
                initializeTextToSpeechWrapper()
            }
        }

        private suspend fun initializeTextToSpeechWrapper() {
            if (isTtsInitialized.value == true) return

            val isInitialized = textToSpeechWrapper.initializeSuspend()

            _isTtsInitialized.value = isInitialized
        }

        suspend fun isEstonianLanguageUsed(): Boolean {
            if (isTtsInitialized.value != true) {
                initializeTextToSpeechWrapper()
            }
            return checkLanguage()
        }

        private fun checkLanguage(): Boolean {
            val textToSpeech = textToSpeechWrapper.getInstance()
            textToSpeech?.let {
                val textToSpeechVoice = textToSpeech.voice
                val appLanguage = Locale.getDefault().language

                // Check if Estonian is available or preferred
                val isESTLanguageAvailable =
                    isTextToSpeechLanguageAvailable(
                        textToSpeech.availableLanguages,
                        setOf(getLocale("est", "EST"), getLocale("et", "ET")),
                    )
                if (textToSpeechVoice != null) {
                    val textToSpeechLocale = textToSpeechVoice.locale
                    if (textToSpeechLocale != null) {
                        val textToSpeechLanguage = textToSpeechLocale.language
                        if (appLanguage == "et" &&
                            (
                                isESTLanguageAvailable ||
                                    (textToSpeechLanguage == "et" || textToSpeechLanguage == "est")
                            )
                        ) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        private fun isTextToSpeechLanguageAvailable(
            availableLocales: Set<Locale>?,
            locales: Set<Locale>,
        ): Boolean = availableLocales?.any { al -> locales.any { lo -> al.language == lo.language } } == true

        override fun onCleared() {
            super.onCleared()
            textToSpeechWrapper.shutdown()
        }
    }
