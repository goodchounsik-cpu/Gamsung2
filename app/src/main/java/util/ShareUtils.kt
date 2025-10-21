package com.gamsung2.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

object ShareUtils {

    fun shareTextFile(
        context: Context,
        fileName: String,
        content: String,
        mime: String
    ) {
        val outFile = File(context.cacheDir, fileName).apply {
            writeText(content, Charsets.UTF_8)
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ContextCompat.startActivity(
            context,
            Intent.createChooser(intent, "공유"),
            null
        )
    }
}
