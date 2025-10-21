package com.gamsung2.util

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechRecognizerHelper(private val app: Application) {

    private var recognizer: SpeechRecognizer? = null
    private val _partial = MutableStateFlow("")
    val partial: StateFlow<String> = _partial

    private val _final = MutableStateFlow<String?>(null)
    val finalText: StateFlow<String?> = _final

    private val _listening = MutableStateFlow(false)
    val listening: StateFlow<Boolean> = _listening

    fun start(langTag: String) {
        stop()
        recognizer = SpeechRecognizer.createSpeechRecognizer(app).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) { _listening.value = true }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onPartialResults(partialResults: Bundle) {
                    val list = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!list.isNullOrEmpty()) _partial.value = list.first()
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onEndOfSpeech() { /* wait final */ }
                override fun onError(error: Int) { _listening.value = false }
                override fun onResults(results: Bundle) {
                    _listening.value = false
                    val list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    _final.value = list?.firstOrNull()
                }
            })
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag) // e.g., "ko-KR", "en-US", "ja-JP"
        }
        recognizer?.startListening(intent)
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
        _listening.value = false
    }
}
