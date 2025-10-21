package com.gamsung2.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.gamsung2.data.ads.LocalBizAd

class AdsRepository {
    fun localBizAds(userLat: Double?, userLon: Double?): Flow<List<LocalBizAd>> = flow {
        // TODO: 실제 API 호출 대체
        delay(150)
        emit(
            listOf(
                LocalBizAd("biz1","수제 버거 세트 할인","런치 20%","", "버거몽", "○○로 11", "app://place/biz1", null, null, null, null),
                LocalBizAd("biz2","카페 라떼 1+1",null,"", "카페비", "△△길 23", "app://place/biz2", null, null, null, null),
            )
        )
    }
}
