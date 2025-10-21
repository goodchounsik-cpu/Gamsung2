package com.gamsung2.ui.search

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
fun SearchScreen() {
    val activity = LocalContext.current as ComponentActivity
    val storyVm: StoryViewModel = viewModel(viewModelStoreOwner = activity)
    val bucketState = storyVm.bucket.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->                       // ← 파라미터를 받아서
        Column(Modifier.padding(innerPadding).padding(16.dp)) {
            Text(text = "버킷에 담긴 장소 수: ${bucketState.value.items.size}")
        }
    }
}
