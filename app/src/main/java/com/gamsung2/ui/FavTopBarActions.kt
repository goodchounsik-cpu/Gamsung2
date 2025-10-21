package com.gamsung2.ui

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.gamsung2.MapViewModel

@Composable
fun FavTopBarActions(
    vm: MapViewModel,
    onSnack: (String) -> Unit
) {
    val ctx = LocalContext.current

    // 가져오기(OpenDocument) – CSV/GPX 모두 허용
    val openDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = vm.importFromUri(ctx.contentResolver, uri)
        onSnack(if (ok) "가져오기 완료" else "가져오기 실패(형식 오류)")
    }

    // 내보내기(CreateDocument) – 확장자 선택에 따라 CSV/GPX
    var exportMime by remember { mutableStateOf("text/csv") }
    val createDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("favorites.csv")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val ok = if (exportMime == "text/csv")
            vm.exportCsv(ctx.contentResolver, uri)
        else
            vm.exportGpx(ctx.contentResolver, uri)
        onSnack(if (ok) "내보내기 완료" else "내보내기 실패(쓰기 오류)")
    }

    var menuOpen by remember { mutableStateOf(false) }
    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.FileUpload, null) }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
            text = { Text("CSV 가져오기") },
            onClick = { menuOpen = false; openDoc.launch(arrayOf("text/*", "application/*")) }
        )
        DropdownMenuItem(
            text = { Text("GPX 가져오기") },
            onClick = { menuOpen = false; openDoc.launch(arrayOf("application/gpx+xml", "application/*", "text/*")) }
        )
        Divider()
        DropdownMenuItem(
            leadingIcon = { Icon(Icons.Default.FileDownload, null) },
            text = { Text("CSV 내보내기") },
            onClick = {
                menuOpen = false
                exportMime = "text/csv"
                createDoc.launch("favorites.csv")
            }
        )
        DropdownMenuItem(
            text = { Text("GPX 내보내기") },
            onClick = {
                menuOpen = false
                exportMime = "application/gpx+xml"
                createDoc.launch("favorites.gpx")
            }
        )
    }
}
