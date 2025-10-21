// app/src/main/java/com/gamsung2/viewmodel/HomeViewModel.kt
package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class HomeUiState(
    val areas: List<Pair<String,String>> = listOf(
        "nearby" to "내 주변",
        "seoul" to "서울",
        "busan" to "부산",
        "daegu" to "대구",
        "jeju" to "제주"
    ),
    val selectedId: String = "nearby"
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    fun selectArea(id: String) {
        _state.value = _state.value.copy(selectedId = id)
    }
}
