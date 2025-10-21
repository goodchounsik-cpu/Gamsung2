// app/src/main/java/com/gamsung2/ui/event/EventDetailScreen.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2.ui.event

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gamsung2.viewmodel.EventViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun EventDetailScreen(
    navController: NavController? = null,
    eventId: Long,
    vm: EventViewModel = viewModel()
) {
    // 단건 구독
    val event = vm.observeEvent(eventId)
        .collectAsStateWithLifecycle(initialValue = null).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "이벤트 상세") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController?.navigate("editEvent/$eventId") }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "수정"
                        )
                    }
                    IconButton(onClick = {
                        vm.deleteEvent(eventId)
                        navController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "삭제"
                        )
                    }
                }
            )
        }
    ) { inner ->
        if (event == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // 포맷터
        val dateFmt = remember {
            DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", Locale.KOREA)
        }
        val timeFmt = remember {
            DateTimeFormatter.ofPattern("a h:mm", Locale.KOREA)
        }

        fun formatDate(iso: String): String =
            runCatching { LocalDate.parse(iso).format(dateFmt) }.getOrElse { iso }

        fun formatTime(hhmm: String?): String =
            hhmm?.let { s ->
                runCatching { LocalTime.parse(s).format(timeFmt) }
                    .getOrElse { s }
            } ?: "시간 없음"

        val timeText = when {
            event.allDay -> "종일"
            event.startTime != null && event.endTime != null ->
                "${formatTime(event.startTime)} ~ ${formatTime(event.endTime)}"
            event.startTime != null -> formatTime(event.startTime)
            else -> "시간 없음"
        }

        val memoText = event.memo?.takeIf { it.isNotBlank() } ?: "메모가 없습니다."

        // 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "날짜", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatDate(event.date),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "시간", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            ElevatedCard {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "메모", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = memoText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController?.navigate("editEvent/$eventId") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "수정")
                }

                Button(
                    onClick = {
                        vm.deleteEvent(eventId)
                        navController?.popBackStack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "삭제")
                }
            }
        }
    }
}
