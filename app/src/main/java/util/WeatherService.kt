// app/src/main/java/com/gamsung2/util/WeatherService.kt
package com.gamsung2.util

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

object WeatherService {
    /**
     * Open-Meteo 일간 예보 (무료/키 불필요)
     * https://open-meteo.com
     */
    fun fetchDaily(lat: Double, lng: Double, days: Int = 7): List<DailyForecast> {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                    "?latitude=$lat&longitude=$lng" +
                    "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=$days"
        )

        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 5000
            readTimeout = 7000
        }

        return try {
            conn.inputStream.bufferedReader().use { br ->
                val root = JSONObject(br.readText())
                val daily = root.getJSONObject("daily")

                val dates = daily.getJSONArray("time")
                val codes = daily.getJSONArray("weathercode")
                val tmax  = daily.getJSONArray("temperature_2m_max")
                val tmin  = daily.getJSONArray("temperature_2m_min")

                val fmt = DateTimeFormatter.ISO_LOCAL_DATE

                buildList {
                    for (i in 0 until dates.length()) {
                        val date = LocalDate.parse(dates.getString(i), fmt)
                        val code = codes.getInt(i)
                        val max  = tmax.getDouble(i)
                        val min  = tmin.getDouble(i)

                        add(
                            DailyForecast(
                                date = date,
                                type = mapCode(code),
                                tMin = (min * 10).roundToInt() / 10.0,
                                tMax = (max * 10).roundToInt() / 10.0
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WeatherService", "fetchDaily error", e)
            emptyList()
        } finally {
            conn.disconnect()
        }
    }

    /** Open-Meteo weathercode → 단순 분류 */
    private fun mapCode(code: Int): WeatherType = when (code) {
        0 -> WeatherType.SUNNY
        1, 2, 3, 45, 48 -> WeatherType.CLOUDY
        in 51..67, in 80..82, 95, 96, 99 -> WeatherType.RAIN
        in 71..77, 85, 86 -> WeatherType.SNOW
        else -> WeatherType.OTHER
    }
}
