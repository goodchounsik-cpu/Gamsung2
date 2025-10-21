package com.gamsung2.export

import com.gamsung2.data.local.FavoritePlaceEntity

object TrackExporter {

    fun buildGpx(items: List<FavoritePlaceEntity>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.1" creator="Gamsung2"
 xmlns="http://www.topografix.com/GPX/1/1">
""")
        items.forEach { p ->
            append(
                """
<wpt lat="${p.lat}" lon="${p.lng}">
  <name>${xml(p.name)}</name>
  ${if (!p.note.isNullOrBlank()) "<desc>${xml(p.note)}</desc>" else ""}
</wpt>
""".trimIndent()
            )
            append('\n')
        }
        append("</gpx>")
    }

    fun buildKml(items: List<FavoritePlaceEntity>): String = buildString {
        append(
            """<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
"""
        )
        items.forEach { p ->
            append(
                """
<Placemark>
  <name>${xml(p.name)}</name>
  ${if (!p.note.isNullOrBlank()) "<description>${xml(p.note)}</description>" else ""}
  <Point><coordinates>${p.lng},${p.lat},0</coordinates></Point>
</Placemark>
""".trimIndent()
            )
            append('\n')
        }
        append("</Document></kml>")
    }

    private fun xml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
}
