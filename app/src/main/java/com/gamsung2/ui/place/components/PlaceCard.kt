// app/src/main/java/com/gamsung2/ui/place/components/PlaceCard.kt
package com.gamsung2.ui.place.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gamsung2.data.Place
import com.gamsung2.ui.components.NetworkImage

@Composable
fun PlaceCard(
    place: Place,
    isLodging: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- 유연한 추출(필드/게터 여러 이름 지원) ---
    val name = place.getStringByNames("name", "title", "placeName") ?: "이름 없음"
    val imageUrl = place.getStringByNames("thumbUrl", "thumbnailUrl", "imageUrl", "photoUrl", "photo")
    val address = place.getStringByNames("address", "vicinity", "formattedAddress")
    val rating = place.getDoubleByNames("rating", "rate", "score", "stars")

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // 썸네일: 깜빡임 방지용 NetworkImage 사용 (16:9 권장)
            NetworkImage(
                url = imageUrl,               // null/blank면 플레이스홀더만 노출
                ratio = 16f / 9f,
                cornerRadius = 0f,
                modifier = Modifier.fillMaxWidth()
            )

            Column(Modifier.padding(12.dp)) {
                Text(text = name, style = MaterialTheme.typography.titleMedium)

                address?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }

                rating?.let { r ->
                    Spacer(Modifier.height(6.dp))
                    Text(text = "★ ${"%.1f".format(r)}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ----------------------- Helper -----------------------

/** 문자열 속성 추출: 게터(getXxx) → 필드 순서로 시도 */
private fun Any.getStringByNames(vararg names: String): String? {
    for (n in names) {
        // 1) getter 메서드 시도
        val getterNames = listOf("get${n.replaceFirstChar { it.uppercase() }}", n)
        for (m in getterNames) {
            try {
                val method = this::class.java.methods.firstOrNull { it.name == m && it.parameterCount == 0 }
                val v = method?.invoke(this) as? String
                if (!v.isNullOrBlank()) return v
            } catch (_: Exception) {}
        }
        // 2) 필드 직접 접근
        try {
            val f = this::class.java.getDeclaredField(n)
            f.isAccessible = true
            val v = f.get(this) as? String
            if (!v.isNullOrBlank()) return v
        } catch (_: Exception) {}
    }
    return null
}

/** 숫자 속성 추출(Double로 변환) */
private fun Any.getDoubleByNames(vararg names: String): Double? {
    for (n in names) {
        // getter
        val getterNames = listOf("get${n.replaceFirstChar { it.uppercase() }}", n)
        for (m in getterNames) {
            try {
                val method = this::class.java.methods.firstOrNull { it.name == m && it.parameterCount == 0 }
                val any = method?.invoke(this)
                val d = (any as? Number)?.toDouble() ?: (any as? String)?.toDoubleOrNull()
                if (d != null) return d
            } catch (_: Exception) {}
        }
        // field
        try {
            val f = this::class.java.getDeclaredField(n)
            f.isAccessible = true
            val any = f.get(this)
            val d = (any as? Number)?.toDouble() ?: (any as? String)?.toDoubleOrNull()
            if (d != null) return d
        } catch (_: Exception) {}
    }
    return null
}
