// app/src/main/java/com/gamsung2/ui/fav/FavoritesScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.fav

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.data.local.FavoritePlaceEntity

@Composable
fun FavoritesScreen(
    onBack: (() -> Unit)? = null, // ← navController.popBackStack()을 넘겨 받음
    items: List<FavoritePlaceEntity> = emptyList() // 실제 데이터 연결 시 주입
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, items) {
        if (query.isBlank()) items else items.filter {
            it.name.contains(query, true) || (it.note?.contains(query, true) == true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("즐겨찾기") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                        }
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("즐겨찾기 검색 (이름/메모)") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
                items(filtered, key = { it.id }) { p ->
                    ListItem(
                        headlineContent = { Text(p.name) },
                        supportingContent = { p.note?.let { Text(it) } }
                    )
                    Divider()
                }
            }
        }
    }
}
