package com.gamsung2.ui.place

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.PlaceRepository
import com.gamsung2.model.Place
import com.gamsung2.nav.PlaceDetailArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PlaceDetailUiState(
    val loading: Boolean = true,
    val place: Place? = null,
    val headerImageUrl: String? = null,
    val error: String? = null,
    val args: PlaceDetailArgs
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val repo: PlaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = PlaceDetailArgs.from(savedStateHandle)

    private val _ui = MutableStateFlow(
        PlaceDetailUiState(
            loading = true,
            place = null,
            headerImageUrl = null,
            error = null,
            args = args
        )
    )
    val ui: StateFlow<PlaceDetailUiState> = _ui

    init {
        // 실제 서버 없으니 데모용 로드 (추후 Repository의 detail API로 교체)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                // 데모: 주변 3km / 1페이지에서 id 매칭 흉내
                val list = repo.search(
                    category = "lodging",
                    lat = args.lat,
                    lng = args.lon,
                    radiusKm = 3.0,
                    minRating = null,
                    page = 1,
                    pageSize = 20
                )
                val picked = list.firstOrNull {
                    it.id?.toString() == args.placeId || it.id == args.placeId
                } ?: list.firstOrNull()

                val header = makeHeaderUrl(seed = picked?.id?.toString() ?: args.placeId)

                _ui.value = _ui.value.copy(
                    loading = false,
                    place = picked,
                    headerImageUrl = header
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = t.message ?: "불러오기 실패"
                )
            }
        }
    }

    private fun makeHeaderUrl(seed: String?): String? {
        if (seed.isNullOrBlank()) return null
        // 데모용: 안정적인 시드 기반 랜덤 이미지
        return "https://picsum.photos/seed/${seed.hashCode()}/1200/900"
    }
}
