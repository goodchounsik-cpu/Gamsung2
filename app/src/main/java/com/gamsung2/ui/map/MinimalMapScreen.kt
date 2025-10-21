@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.map

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.ui.story.StoryViewModel

@Composable
fun MinimalMapScreen() {
    val activity = LocalContext.current as ComponentActivity
    val storyVm: StoryViewModel = viewModel(activity)
    val bucketState = storyVm.bucket.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("지도") }) }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp)) {
            Text(text = "버킷에 담긴 장소 수: ${bucketState.value.items.size}")
        }
    }
}
