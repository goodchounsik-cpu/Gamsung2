// app/src/main/java/com/gamsung2/MapViewModel.kt
package com.gamsung2

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.data.local.FavoritePlaceEntity
import com.gamsung2.repository.FavoritePlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URLDecoder
import java.util.Locale

@HiltViewModel
class MapViewModel @Inject constructor(
    private val repo: FavoritePlaceRepository
) : ViewModel() {

    val places: StateFlow<List<FavoritePlaceEntity>> =
        repo.favoritesFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    private fun buildPlaceId(name: String, lat: Double, lng: Double): String {
        val latS = String.format(Locale.US, "%.5f", lat)
        val lngS = String.format(Locale.US, "%.5f", lng)
        return "${name.trim()}@$latS,$lngS"
    }

    fun addFavorite(name: String, note: String?, lat: Double, lng: Double) {
        val placeId = buildPlaceId(name, lat, lng)
        viewModelScope.launch(Dispatchers.IO) {
            repo.upsert(
                FavoritePlaceEntity(
                    id = 0L,
                    placeId = placeId,
                    name = name,
                    note = note,
                    lat = lat,
                    lng = lng
                )
            )
        }
    }

    suspend fun deleteFavorite(id: Long) {
        withContext(Dispatchers.IO) { repo.deleteById(id) }
    }

    suspend fun editFavorite(id: Long, name: String, note: String?) {
        withContext(Dispatchers.IO) {
            val cur = places.value.firstOrNull { it.id == id } ?: return@withContext
            repo.upsert(cur.copy(name = name, note = note))
        }
    }

    fun importFromUri(cr: ContentResolver, uri: Uri): Boolean {
        return try {
            val head = cr.openInputStream(uri)?.use { `is` ->
                val sniff = ByteArray(64)
                val read = `is`.read(sniff)
                if (read > 0) String(sniff, 0, read) else ""
            } ?: return false

            cr.openInputStream(uri)?.use { full ->
                if (head.contains("<gpx", ignoreCase = true)) importGpx(full) else importCsv(full)
                true
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun importCsv(input: InputStream) {
        input.bufferedReader(Charsets.UTF_8).use { br ->
            br.lineSequence()
                .dropWhile { it.isBlank() || it.startsWith("#") }
                .forEach { line ->
                    val cols = line.split(',')
                    if (cols.size >= 4) {
                        val name = cols[0].trim().trim('"')
                        val note = cols[1].trim().trim('"').ifEmpty { null }
                        val lat = cols[2].trim().toDoubleOrNull()
                        val lng = cols[3].trim().toDoubleOrNull()
                        if (lat != null && lng != null) addFavorite(name, note, lat, lng)
                    }
                }
        }
    }

    private fun importGpx(input: InputStream) {
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = true }
        val xpp = factory.newPullParser().apply {
            setInput(InputStreamReader(input, Charsets.UTF_8))
        }

        var event = xpp.eventType
        var lat: Double? = null
        var lng: Double? = null
        var name: String? = null
        var desc: String? = null
        var currentTag: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    currentTag = xpp.name
                    if (currentTag.equals("wpt", true)) {
                        lat = xpp.getAttributeValue(null, "lat")?.toDoubleOrNull()
                        lng = xpp.getAttributeValue(null, "lon")?.toDoubleOrNull()
                        name = null; desc = null
                    }
                }
                XmlPullParser.TEXT -> {
                    val t = xpp.text ?: ""
                    when (currentTag?.lowercase()) {
                        "name" -> name = runCatching { URLDecoder.decode(t, "UTF-8") }.getOrElse { t }
                        "desc" -> desc = runCatching { URLDecoder.decode(t, "UTF-8") }.getOrElse { t }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (xpp.name.equals("wpt", true)) {
                        val la = lat; val ln = lng; val nm = name
                        if (la != null && ln != null && !nm.isNullOrBlank()) {
                            addFavorite(nm, desc, la, ln)
                        }
                        lat = null; lng = null; name = null; desc = null
                    }
                    currentTag = null
                }
            }
            event = xpp.next()
        }
    }

    fun exportCsv(cr: ContentResolver, uri: Uri): Boolean = try {
        val list = places.value
        cr.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                w.appendLine("name,note,lat,lng")
                list.forEach { p ->
                    val n = p.name.replace(",", " ")
                    val d = (p.note ?: "").replace(",", " ")
                    w.appendLine("$n,$d,${p.lat},${p.lng}")
                }
                w.flush()
            }
        } != null
    } catch (_: Exception) { false }

    fun exportGpx(cr: ContentResolver, uri: Uri): Boolean = try {
        val list = places.value
        cr.openOutputStream(uri)?.use { os ->
            OutputStreamWriter(os, Charsets.UTF_8).use { w ->
                w.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
                w.appendLine("""<gpx version="1.1" creator="Gamsung2" xmlns="http://www.topografix.com/GPX/1/1">""")
                list.forEach { p ->
                    w.appendLine("""  <wpt lat="${p.lat}" lon="${p.lng}">""")
                    w.appendLine("""    <name>${xmlEscape(p.name)}</name>""")
                    p.note?.takeIf { it.isNotBlank() }?.let { note ->
                        w.appendLine("""    <desc>${xmlEscape(note)}</desc>""")
                    }
                    w.appendLine("  </wpt>")
                }
                w.appendLine("</gpx>")
                w.flush()
            }
        } != null
    } catch (_: Exception) { false }

    private fun xmlEscape(s: String) = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
