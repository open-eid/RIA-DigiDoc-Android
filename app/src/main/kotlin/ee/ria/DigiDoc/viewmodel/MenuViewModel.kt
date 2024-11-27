@file:Suppress("PackageName")

package ee.ria.DigiDoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ee.ria.DigiDoc.domain.model.tts.TextToSpeechWrapper
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MenuViewModel
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
                        setOf(Locale("est", "EST"), Locale("et", "ET")),
                    )
                if (textToSpeechVoice != null) {
                    val textToSpeechLocale = textToSpeechVoice.locale
                    if (textToSpeechLocale != null) {
                        val textToSpeechLanguage = textToSpeechLocale.language
                        if (appLanguage == "et" && (
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
        ): Boolean {
            return availableLocales?.any { al -> locales.any { lo -> al.language == lo.language } } == true
        }

        override fun onCleared() {
            super.onCleared()
            textToSpeechWrapper.shutdown()
        }
    }
