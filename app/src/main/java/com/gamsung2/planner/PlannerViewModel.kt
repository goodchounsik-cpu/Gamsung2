package com.gamsung2.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.SearchRepository
import com.gamsung2.model.PlaceCandidate
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val repo: SearchRepository
): ViewModel() {

    private val _selected = MutableStateFlow<List<PlaceCandidate>>(emptyList())
    val selected: StateFlow<List<PlaceCandidate>> = _selected

    fun add(pc: PlaceCandidate) {
        _selected.value = (_selected.value + pc).distinctBy { it.place.id }
    }
    fun remove(id: String) {
        _selected.value = _selected.value.filterNot { it.place.id == id }
    }
    fun togglePin(id: String) {
        _selected.value = _selected.value.map {
            if (it.place.id == id) it.copy(pinned = !it.pinned) else it
        }
    }

    fun clear() { _selected.value = emptyList() }

    fun searchAll(q: String, lat: Double?, lon: Double?, onResult: (List<PlaceCandidate>) -> Unit) {
        viewModelScope.launch {
            onResult(repo.searchAll(q, lat, lon))
        }
    }
}
