package com.gamsung2.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DualSearchBar(
    modifier: Modifier = Modifier,
    nearbyPlaceholder: String = "내 주변 테마 검색",
    nationwidePlaceholder: String = "전국 장소/키워드 검색",
    onNearbySearch: (query: String) -> Unit,
    onNationwideSearch: (query: String) -> Unit,
    onPickRegion: (() -> Unit)? = null,
) {
    var nearby by remember { mutableStateOf("") }
    var nationwide by remember { mutableStateOf("") }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = nearby,
            onValueChange = { nearby = it },
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Map, null) },
            placeholder = { Text(nearbyPlaceholder) },
            trailingIcon = {
                TextButton(
                    onClick = { onNearbySearch(nearby.trim()) },
                    enabled = nearby.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) { Text("검색") }
            }
        )

        OutlinedTextField(
            value = nationwide,
            onValueChange = { nationwide = it },
            modifier = Modifier.weight(1f).height(48.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Public, null) },
            placeholder = { Text(nationwidePlaceholder) },
            trailingIcon = {
                Row {
                    if (onPickRegion != null) {
                        TextButton(
                            onClick = onPickRegion,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) { Text("지역") }
                    }
                    TextButton(
                        onClick = { onNationwideSearch(nationwide.trim()) },
                        enabled = nationwide.isNotBlank(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) { Text("검색") }
                }
            }
        )
    }
}
