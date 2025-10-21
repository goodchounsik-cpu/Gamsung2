package com.gamsung2.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onGroupClick: () -> Unit = {},
    onSosClick: () -> Unit = {},
    onKtx: () -> Unit = {},
    onBus: () -> Unit = {},
    onRent: () -> Unit = {}
) {
    var transportExpanded by remember { mutableStateOf(false) }

    val groupColor = Color(0xFF1976D2) // 파랑
    val sosColor   = Color(0xFFE53935) // 빨강

    TopAppBar(
        title = { Text(title) },
        actions = {
            // 그룹
            Button(
                onClick = onGroupClick,
                colors = ButtonDefaults.buttonColors(containerColor = groupColor, contentColor = Color.White),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("그룹")
            }

            Spacer(Modifier.width(8.dp))

            // 교통수단 (드롭다운)
            Box {
                FilledTonalButton(
                    onClick = { transportExpanded = true },
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) { Text("교통수단") }

                DropdownMenu(
                    expanded = transportExpanded,
                    onDismissRequest = { transportExpanded = false },
                    offset = DpOffset(0.dp, 8.dp)
                ) {
                    DropdownMenuItem(text = { Text("KTX") }, onClick = { transportExpanded = false; onKtx() })
                    DropdownMenuItem(text = { Text("버스") }, onClick = { transportExpanded = false; onBus() })
                    DropdownMenuItem(text = { Text("렌트") }, onClick = { transportExpanded = false; onRent() })
                }
            }

            Spacer(Modifier.width(8.dp))

            // SOS
            Button(
                onClick = onSosClick,
                colors = ButtonDefaults.buttonColors(containerColor = sosColor, contentColor = Color.White),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("SOS")
            }
        }
    )
}
