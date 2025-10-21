package com.gamsung2.util

import com.gamsung2.data.local.FavoritePlaceEntity

object CsvUtils {
    private const val HEADER = "name,note,lat,lng"

    fun toCsv(list: List<FavoritePlaceEntity>): String = buildString {
        appendLine(HEADER)
        list.forEach { p ->
            appendLine(
                listOf(p.name, p.note ?: "", p.lat.toString(), p.lng.toString())
                    .joinToString(",") { escape(it) }
            )
        }
    }

    data class Row(val name: String, val note: String?, val lat: Double, val lng: Double)

    fun parseCsv(text: String): List<Row> =
        text.lineSequence()
            .dropWhile { it.isBlank() }
            .drop(1) // header
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val cols = splitCsv(line)
                if (cols.size < 4) return@mapNotNull null
                val lat = cols[2].toDoubleOrNull() ?: return@mapNotNull null
                val lng = cols[3].toDoubleOrNull() ?: return@mapNotNull null
                Row(cols[0], cols[1].ifBlank { null }, lat, lng)
            }.toList()

    private fun escape(s: String) =
        if (s.contains(',') || s.contains('"') || s.contains('\n'))
            "\"" + s.replace("\"", "\"\"") + "\""
        else s

    private fun splitCsv(line: String): List<String> {
        val out = ArrayList<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"'); i++
                } else if (c == '"') inQuotes = false
                else sb.append(c)
            } else {
                when (c) {
                    ',' -> { out += sb.toString(); sb.clear() }
                    '"' -> inQuotes = true
                    else -> sb.append(c)
                }
            }
            i++
        }
        out += sb.toString()
        return out
    }
}
