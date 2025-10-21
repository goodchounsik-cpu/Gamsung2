// app/src/main/java/com/gamsung2/viewmodel/SearchViewModel.kt
package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SearchUiState(
    val nearbyQuery: String = "",
    val nationQuery: String = "",
    val radiusKm: Int = 5
)

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state

    fun setNearbyQuery(q: String) = _state.tryEmit(_state.value.copy(nearbyQuery = q))
    fun setNationQuery(q: String) = _state.tryEmit(_state.value.copy(nationQuery = q))
    fun setRadius(km: Int) = _state.tryEmit(_state.value.copy(radiusKm = km))
}
