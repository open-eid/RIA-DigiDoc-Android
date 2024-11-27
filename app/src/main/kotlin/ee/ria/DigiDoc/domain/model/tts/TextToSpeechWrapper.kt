@file:Suppress("PackageName")

package ee.ria.DigiDoc.domain.model.tts

import android.speech.tts.TextToSpeech

interface TextToSpeechWrapper {
    suspend fun initializeSuspend(): Boolean

    fun getInstance(): TextToSpeech?

    fun shutdown()
}
