// app/src/main/java/com/gamsung2/ui/home/AreaSearchBar.kt
package com.gamsung2.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

data class AreaUi(val id: String, val name: String)

@Composable
fun AreaSearchBar(
    selected: AreaUi,
    candidates: List<AreaUi>,
    onPick: (AreaUi) -> Unit,
    onUseHere: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = if (query.isBlank()) selected.name else query,
            onValueChange = { query = it; expanded = true },
            leadingIcon = { Icon(Icons.Filled.Place, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onUseHere) {
                    Icon(Icons.Filled.LocationSearching, contentDescription = "내 주변")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            label = { Text("지역 선택 또는 검색") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Search)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.98f)
        ) {
            val list = if (query.isBlank()) candidates
            else candidates.filter { it.name.contains(query.trim()) }

            list.forEach { a ->
                DropdownMenuItem(
                    text = { Text(a.name) },
                    onClick = {
                        onPick(a)
                        query = ""
                        expanded = false
                    }
                )
            }
        }
    }
}
