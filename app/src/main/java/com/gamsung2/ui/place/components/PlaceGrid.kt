// app/src/main/java/com/gamsung2/ui/place/components/PlaceGrid.kt
package com.gamsung2.ui.place.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.data.Place

private val GridGap = 12.dp

@Composable
fun PlaceGrid(
    items: List<Place>,
    isLodging: Boolean,
    onItemClick: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(GridGap),
        horizontalArrangement = Arrangement.spacedBy(GridGap),
    ) {
        items(items, key = { it.id }) { p ->
            PlaceCard(
                place = p,
                isLodging = isLodging,
                onClick = { onItemClick(p) }
            )
        }
    }
}
