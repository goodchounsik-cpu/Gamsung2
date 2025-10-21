// app/src/main/java/com/gamsung2/ui/components/FavoriteButton.kt
package com.gamsung2.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.gamsung2.data.local.DbProvider
import com.gamsung2.data.local.FavoritePlaceEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun FavoriteButton(
    placeId: String,
    title: String,
    lat: Double,          // ✅ 위도 필수
    lng: Double,          // ✅ 경도 필수
    note: String? = null  // 메모 선택
) {
    val context = LocalContext.current
    val dao = remember { DbProvider.favoritePlaceDao(context) }
    val scope = rememberCoroutineScope()

    // 현재 즐겨찾기 여부
    var isFav by remember { mutableStateOf(false) }

    // placeId 기준으로 관찰 (DAO에 isFavoriteFlow가 있을 때)
    LaunchedEffect(placeId) {
        // 있으면 사용
        try {
            dao.isFavoriteFlow(placeId).collectLatest { isFav = it }
        } catch (_: Throwable) {
            // 없다면 모든 리스트에서 검사 (fallback)
            dao.getAllFlow().collectLatest { list ->
                isFav = list.any { it.placeId == placeId }
            }
        }
    }

    IconToggleButton(
        checked = isFav,
        onCheckedChange = { checked ->
            isFav = checked
            scope.launch {
                if (checked) {
                    // ✅ 필요한 필드 모두 채워서 저장
                    dao.upsert(
                        FavoritePlaceEntity(
                            id = 0,                  // autoGenerate
                            placeId = placeId,
                            name = title,
                            note = note,
                            lat = lat,
                            lng = lng,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // ✅ placeId로 삭제 (DAO에 구현됨)
                    dao.deleteByPlaceId(placeId)
                }
            }
        }
    ) {
        Icon(
            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null
        )
    }
}
