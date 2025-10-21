package com.gamsung2.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryPlaceListViewModel @Inject constructor(
    private val repo: PlaceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(CategoryPlaceUiState())
    val uiState: StateFlow<CategoryPlaceUiState> = _ui

    // 화면 파라미터
    private var initialized = false
    private var category: String = "lodging"   // "lodging" | "restaurant"
    private var themeTitle: String = ""
    private var lat: Double? = null
    private var lng: Double? = null
    private var radiusInit: Double = 1.0

    private var loadJob: Job? = null
    var isLoadingMore: Boolean = false
        private set

    /** 최초 1회만 세팅 */
    fun ensureInitialized(
        category: String,
        themeTitle: String,
        initLat: Double?,
        initLng: Double?,
        initRadiusKm: Double
    ) {
        if (initialized) return
        initialized = true

        this.category = category
        this.themeTitle = themeTitle
        this.lat = initLat
        this.lng = initLng
        this.radiusInit = initRadiusKm

        _ui.update { it.copy(radiusKm = initRadiusKm, page = 1, hasMore = true) }
        refresh()
    }

    fun setRadius(km: Double) {
        if (km == _ui.value.radiusKm) return
        _ui.update { it.copy(radiusKm = km, page = 1, hasMore = true) }
        refresh()
    }

    fun toggleLodgingType(t: LodgingType) {
        _ui.update {
            val next = it.lodgingTypes.toMutableSet().apply { if (!add(t)) remove(t) }
            it.copy(lodgingTypes = next, page = 1, hasMore = true)
        }
        refresh()
    }

    fun toggleCuisine(c: CuisineType) {
        _ui.update {
            val next = it.cuisines.toMutableSet().apply { if (!add(c)) remove(c) }
            it.copy(cuisines = next, page = 1, hasMore = true)
        }
        refresh()
    }

    fun updateMinRating(step: Double) {
        if (step == _ui.value.minRating) return
        _ui.update { it.copy(minRating = step, page = 1, hasMore = true) }
        refresh()
    }

    fun refresh() {
        load(page = 1, replace = true)
    }

    fun loadNext() {
        val s = _ui.value
        if (!s.hasMore || s.loading || isLoadingMore) return
        load(page = s.page + 1, replace = false)
    }

    private fun load(page: Int, replace: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (replace) _ui.update { it.copy(loading = true, error = null) }
            else isLoadingMore = true

            val state = _ui.value
            val typesCsv = if (category == "lodging")
                state.lodgingTypes.takeIf { it.isNotEmpty() }?.joinToString(",") { it.label }
            else null

            val cuisinesCsv = if (category == "restaurant")
                state.cuisines.takeIf { it.isNotEmpty() }?.joinToString(",") { it.label }
            else null

            runCatching {
                repo.search(
                    category = category,
                    lat = lat,
                    lng = lng,
                    radiusKm = state.radiusKm,
                    typesCsv = typesCsv,
                    cuisinesCsv = cuisinesCsv,
                    minRating = state.minRating.takeIf { it > 0.0 },
                    page = page,
                    pageSize = 20
                )
            }.onSuccess { newItems ->
                val merged = if (replace) newItems else state.items + newItems
                _ui.update {
                    it.copy(
                        loading = false,
                        error = null,
                        items = merged,
                        page = page,
                        hasMore = newItems.isNotEmpty()
                    )
                }
            }.onFailure { e ->
                _ui.update { it.copy(loading = false, error = e.message ?: "알 수 없는 오류") }
            }

            isLoadingMore = false
        }
    }
}
