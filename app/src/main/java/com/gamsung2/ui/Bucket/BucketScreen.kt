package com.gamsung2.ui.bucket

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.ui.story.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketScreen(
    onBack: () -> Unit,
    onOpenPlace: (id: String, title: String) -> Unit = { _, _ -> }
) {
    // ★★★ 액티비티 스코프 VM 공유
    val activity = LocalContext.current as ComponentActivity
    val vm: StoryViewModel = viewModel(activity)
    val bucketState by vm.bucket.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("버킷리스트") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(16.dp)) {
            val items = bucketState.items
            if (items.isEmpty()) {
                Text("아직 담긴 곳이 없어요.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPlace(item.id, item.title) }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text("ID: ${item.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
