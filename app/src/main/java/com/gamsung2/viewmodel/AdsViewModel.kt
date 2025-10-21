package com.gamsung2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.repository.AdsRepository
import com.gamsung2.data.ads.LocalBizAd
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdsViewModel(
    repo: AdsRepository,
    userLat: Double? = null,
    userLon: Double? = null
) : ViewModel() {
    val ads: StateFlow<List<LocalBizAd>> =
        repo.localBizAds(userLat, userLon)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
