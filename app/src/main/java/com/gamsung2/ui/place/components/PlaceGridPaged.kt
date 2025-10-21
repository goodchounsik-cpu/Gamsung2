// app/src/main/java/com/gamsung2/ui/place/components/PlaceGridPaged.kt
package com.gamsung2.ui.place.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.model.Place
import kotlinx.coroutines.flow.distinctUntilChanged

private val GridGap = 12.dp

@Composable
fun PlaceGridPaged(
    items: List<Place>,
    isLoading: Boolean,
    onLoadMore: () -> Unit,
    // 선택: 끝에 도달했을 때 표시할지 (없으면 false 로 동작)
    endReached: Boolean = false,
    // 그리드 외관
    columns: GridCells = GridCells.Fixed(2),
    contentPadding: PaddingValues = PaddingValues(10.dp),
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    // 각 아이템을 그리는 슬롯
    itemContent: @Composable (Place) -> Unit
) {
    // ====== 스크롤 끝 감지 → onLoadMore() ======
    LaunchedEffect(state, items.size, isLoading, endReached) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.lastOrNull()?.index to state.layoutInfo.totalItemsCount }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (lastVisible == null || total == 0) return@collect
                val thresholdFromEnd = 4          // 끝에서 N개 남았을 때 미리 로드
                val shouldLoadMore = lastVisible >= total - 1 - thresholdFromEnd
                if (shouldLoadMore && !isLoading && !endReached) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = columns,
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(GridGap),
        horizontalArrangement = Arrangement.spacedBy(GridGap),
    ) {
        // 본문 아이템
        items(items, key = { it.id }) { p ->
            itemContent(p)
        }

        // 로딩 셔머 (2열 그리드에 2~4개 정도)
        if (isLoading) {
            items(4) {
                PlaceCardShimmer()
            }
        }

        // 끝 표시(선택): 한 줄 전체 span으로 안내 문구/여백
        if (endReached && items.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                EndOfListSpacer()
            }
        }
    }
}
