package com.gamsung2.ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import com.gamsung2.MapViewModel
import com.gamsung2.data.local.FavoritePlaceEntity
import kotlinx.coroutines.launch

@Composable
fun EditFavoriteDialog(
    vm: MapViewModel,
    target: FavoritePlaceEntity,
    onDismiss: () -> Unit,
    onSnack: (String) -> Unit
) {
    var name by remember { mutableStateOf(target.name) }
    var note by remember { mutableStateOf(target.note ?: "") }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("즐겨찾기 편집") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("메모") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    vm.editFavorite(target.id, name.trim(), note.trim().ifBlank { null })
                    onSnack("수정했어요")
                    onDismiss()
                }
            }) { Text("저장") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("취소") }
                Spacer(Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        scope.launch {
                            vm.deleteFavorite(target.id)
                            onSnack("삭제했어요")
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("삭제") }
            }
        }
    )
}
