package com.gamsung2.ui.planner

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.ui.story.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    onBack: () -> Unit,
    onAddCourse: () -> Unit = {},
    onAddFood: () -> Unit = {},
    onAddLodging: () -> Unit = {},
    onOpenPlace: (id: String, title: String) -> Unit = { _, _ -> }
) {
    // 액티비티 스코프 VM 공유
    val activity = LocalContext.current as ComponentActivity
    val vm: StoryViewModel = viewModel(activity)
    val story by vm.story.collectAsState()
    val courses = story.courses.orEmpty()
    val foods   = story.foods.orEmpty()
    val lodges  = story.lodgings.orEmpty()

    val ordered = vm.orderedCourses() // (번호, PlanItem)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("나만의 여행") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 코스 섹션 (추천/초기화 액션 추가)
            SectionHeader(
                title = "코스",
                onAdd = onAddCourse,
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { vm.setCourseOrder(vm.recommendCourseOrder()) }) {
                            Text("코스 추천")
                        }
                        TextButton(onClick = { vm.setCourseOrder(emptyList()) }) {
                            Text("초기화")
                        }
                    }
                }
            )
            SimpleListNumbered(
                list = ordered,
                onClick = { onOpenPlace(it.id, it.title) },
                onDelete = { vm.removeFromStory(Bucket.COURSE, it.id) }
            )

            // 식당 섹션
            SectionHeader(title = "식당", onAdd = onAddFood)
            SimpleList(
                list = foods,
                onClick = { onOpenPlace(it.id, it.title) },
                onDelete = { vm.removeFromStory(Bucket.FOOD, it.id) }
            )

            // 숙소 섹션
            SectionHeader(title = "숙소", onAdd = onAddLodging)
            SimpleList(
                list = lodges,
                onClick = { onOpenPlace(it.id, it.title) },
                onDelete = { vm.removeFromStory(Bucket.LODGING, it.id) }
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAdd: () -> Unit,
    actions: (@Composable () -> Unit)? = null
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Row {
            actions?.invoke()
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("추가")
            }
        }
    }
}

@Composable
private fun SimpleListNumbered(
    list: List<Pair<Int, PlanItem>>,
    onClick: (PlanItem) -> Unit,
    onDelete: (PlanItem) -> Unit
) {
    if (list.isEmpty()) {
        Text("비어 있어요.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list, key = { it.second.id }) { (no, item) ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onClick(item) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${no}코스 · ${item.title}", style = MaterialTheme.typography.titleMedium)
                        Text("ID: ${item.id}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "삭제")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleList(
    list: List<PlanItem>,
    onClick: (PlanItem) -> Unit,
    onDelete: (PlanItem) -> Unit
) {
    if (list.isEmpty()) {
        Text("비어 있어요.", style = MaterialTheme.typography.bodyMedium)
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list, key = { it.id }) { item ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onClick(item) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text("ID: ${item.id}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onDelete(item) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "삭제")
                    }
                }
            }
        }
    }
}
