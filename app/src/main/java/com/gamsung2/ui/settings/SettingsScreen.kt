package com.gamsung2.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gamsung2.data.local.AppDatabase
import com.gamsung2.data.local.FavoritePlaceEntity
import com.gamsung2.repository.FavoritePlaceRepository
import com.gamsung2.repository.MapSettingsStore
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo: FavoritePlaceRepository = remember {
        FavoritePlaceRepository(AppDatabase.getInstance(context).favoritePlaceDao())
    }
    val settings = remember { MapSettingsStore(context) }
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    val mapType by settings.mapType.collectAsState(initial = MapType.NORMAL)
    val trafficEnabled by settings.trafficEnabled.collectAsState(initial = false)

    // CSV 내보내기
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = exportCsv(context, uri, repo)
                snack.showSnackbar(if (ok) "CSV로 내보냈습니다." else "내보내기에 실패했습니다.")
            }
        }
    }

    // CSV 가져오기
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val list = importCsv(context, uri)
                if (list != null) {
                    repo.replaceAll(list)
                    snack.showSnackbar("CSV에서 ${list.size}건을 불러왔습니다.")
                } else {
                    snack.showSnackbar("가져오기에 실패했습니다.")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("지도", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                var open by remember { mutableStateOf(false) }

                // 지도 유형 드롭다운
                ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
                    OutlinedTextField(
                        value = mapTypeLabel(mapType),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("지도 유형") },
                        modifier = Modifier
                            .menuAnchor()
                            .weight(1f)
                    )
                    ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                        MapType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(mapTypeLabel(type)) },
                                onClick = {
                                    open = false
                                    scope.launch { settings.setMapType(type) }
                                }
                            )
                        }
                    }
                }

                // 교통 정보 토글
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("교통정보")
                    Switch(
                        checked = trafficEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { settings.setTrafficEnabled(checked) }
                        }
                    )
                }
            }

            HorizontalDivider()

            Text("데이터", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {
                    exportLauncher.launch("gamsung2_favorites.csv")
                }) { Text("CSV 내보내기") }

                OutlinedButton(onClick = {
                    importLauncher.launch(
                        arrayOf("text/*", "text/comma-separated-values", "text/csv")
                    )
                }) { Text("CSV 가져오기") }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "• 내보내기는 CSV(UTF-8)로 저장됩니다.\n" +
                        "• 가져오기는 헤더: id,name,lat,lng,note,createdAt 형식을 읽습니다.\n" +
                        "  (id는 무시되고 새로 부여됩니다.)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun mapTypeLabel(type: MapType): String = when (type) {
    MapType.NORMAL    -> "일반"
    MapType.SATELLITE -> "위성"
    MapType.TERRAIN   -> "지형"
    MapType.HYBRID    -> "하이브리드"
    else              -> "기타"
}

// ===== CSV 유틸 =====
private suspend fun exportCsv(
    context: Context,
    uri: Uri,
    repo: FavoritePlaceRepository
): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        val list = repo.favoritesFlow.first()
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { out ->
            out.appendLine("id,name,lat,lng,note,createdAt")
            list.forEach { e ->
                out.appendLine(
                    listOf(
                        e.id.toString(),
                        e.name.csvEscape(),
                        e.lat.toString(),
                        e.lng.toString(),
                        (e.note ?: "").csvEscape(),
                        e.createdAt.toString()
                    ).joinToString(",")
                )
            }
        } ?: return@withContext false
        true
    }.getOrDefault(false)
}

private suspend fun importCsv(
    context: Context,
    uri: Uri
): List<FavoritePlaceEntity>? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val reader = BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8))
            val result = mutableListOf<FavoritePlaceEntity>()
            reader.lineSequence()
                .drop(1)
                .forEach { line ->
                    if (line.isBlank()) return@forEach
                    val cols = parseCsvLine(line)
                    if (cols.size >= 4) {
                        val name = cols[1]
                        val lat = cols[2].toDoubleOrNull() ?: return@forEach
                        val lng = cols[3].toDoubleOrNull() ?: return@forEach
                        val note = cols.getOrNull(4)?.ifBlank { null }
                        val createdAt = cols.getOrNull(5)?.toLongOrNull() ?: System.currentTimeMillis()
                        result += FavoritePlaceEntity(
                            id = 0,
                            placeId = "${name}@${lat},${lng}",
                            name = name,
                            note = note,
                            lat = lat,
                            lng = lng,
                            createdAt = createdAt
                        )
                    }
                }
            result
        }
    }.getOrNull()
}

private fun parseCsvLine(line: String): List<String> {
    val out = mutableListOf<String>()
    val sb = StringBuilder()
    var quoted = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' -> {
                if (quoted && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"'); i++
                } else {
                    quoted = !quoted
                }
            }
            c == ',' && !quoted -> { out += sb.toString(); sb.setLength(0) }
            else -> sb.append(c)
        }
        i++
    }
    out += sb.toString()
    return out
}

private fun String.csvEscape(): String =
    if (contains(',') || contains('"') || contains('\n')) {
        "\"" + replace("\"", "\"\"") + "\""
    } else this
