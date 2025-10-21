// app/src/main/java/com/gamsung2/data/FilmingIndex.kt
package com.gamsung2.data

import android.content.Context
import com.gamsung2.model.Place
import org.json.JSONArray

object FilmingIndex {
    fun load(context: Context): List<Place> {
        val json = context.assets.open("filming_index.json")
            .bufferedReader(Charsets.UTF_8).use { it.readText() }

        val arr = JSONArray(json)
        val list = mutableListOf<Place>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.optString("id")
            val name = o.optString("name")
            val subtitle = o.optString("subtitle", "")
            val lat = o.optDouble("lat")
            val lon = o.optDouble("lon")
            val badge = o.optString("badge", "촬영지")
            val rating = if (o.has("rating")) o.optDouble("rating") else null

            list += Place(
                id = id,                 // Long이어도 toString 비교로 이미 호환됨
                name = name,
                subtitle = subtitle,
                badge = badge,
                distanceKm = null,       // 필요 시 계산해서 넣기
                rating = rating?.toDouble()
            )
            // lat/lon은 Place에 필드가 없으면 화면 라우트 파라미터로 전달해 사용
        }
        return list
    }
}
