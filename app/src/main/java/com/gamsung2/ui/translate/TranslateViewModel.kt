package com.gamsung2.ui.translate

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.util.SpeechRecognizerHelper
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import com.gamsung2.ui.translate.TranslateViewModel

@HiltViewModel
class TranslateViewModel @Inject constructor(
    app: Application
) : AndroidViewModel(app) {

    // UI state
    private val _srcText = MutableStateFlow("")
    val srcText: StateFlow<String> = _srcText

    private val _dstText = MutableStateFlow("")
    val dstText: StateFlow<String> = _dstText

    private val _downloading = MutableStateFlow(false)
    val downloading: StateFlow<Boolean> = _downloading

    private val _listening = MutableStateFlow(false)
    val listening: StateFlow<Boolean> = _listening

    // languages (기본: 한국어 -> 영어)
    private val _srcLang = MutableStateFlow(TranslateLanguage.KOREAN)   // "ko"
    private val _dstLang = MutableStateFlow(TranslateLanguage.ENGLISH)  // "en"
    val srcLang: StateFlow<String> = _srcLang
    val dstLang: StateFlow<String> = _dstLang

    private var translator: Translator? = null
    private val stt = SpeechRecognizerHelper(app)
    private var tts: TextToSpeech? = null

    init {
        // STT state bridge
        viewModelScope.launch {
            stt.listening.collect { _listening.value = it }
        }
        viewModelScope.launch {
            stt.partial.collect { if (it.isNotBlank()) _srcText.value = it }
        }
        viewModelScope.launch {
            stt.finalText.collect { final ->
                final?.let { _srcText.value = it; translate() }
            }
        }
        // TTS
        tts = TextToSpeech(getApplication(), null)
    }

    fun setLangs(source: String, target: String) {
        if (source == _srcLang.value && target == _dstLang.value) return
        _srcLang.value = source
        _dstLang.value = target
        releaseTranslator()
    }

    fun swapLangs() {
        val s = _srcLang.value
        _srcLang.value = _dstLang.value
        _dstLang.value = s
        releaseTranslator()
        // 기존 번역 텍스트도 스왑 UX
        val a = _srcText.value
        _srcText.value = _dstText.value
        _dstText.value = a
    }

    fun updateSrcText(text: String) {
        _srcText.value = text
    }

    fun startSpeech(langTag: String) {
        stt.start(langTag)
    }

    fun stopSpeech() {
        stt.stop()
    }

    fun translate() {
        val src = _srcText.value
        if (src.isBlank()) { _dstText.value = ""; return }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(_srcLang.value)
            .setTargetLanguage(_dstLang.value)
            .build()

        val tr = translator ?: Translation.getClient(options).also { translator = it }

        _downloading.value = true
        tr.downloadModelIfNeeded()
            .addOnSuccessListener {
                tr.translate(src)
                    .addOnSuccessListener { out ->
                        _dstText.value = out
                        _downloading.value = false
                    }
                    .addOnFailureListener {
                        _dstText.value = "번역 실패: ${it.message}"
                        _downloading.value = false
                    }
            }
            .addOnFailureListener {
                _dstText.value = "모델 다운로드 실패: ${it.message}"
                _downloading.value = false
            }
    }

    fun speakDst(langTag: String) {
        val text = _dstText.value
        if (text.isBlank()) return
        val locale = Locale.forLanguageTag(langTag)
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts-id")
    }

    private fun releaseTranslator() {
        translator?.close()
        translator = null
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeech()
        releaseTranslator()
        tts?.shutdown()
        tts = null
    }
}
