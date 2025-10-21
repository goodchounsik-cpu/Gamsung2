// app/src/main/java/com/gamsung2/ui/planner/TripPlannerScreen.kt
package com.gamsung2.ui.planner

import androidx.compose.runtime.Composable
import com.gamsung2.domain.plan.PlanItem
import com.gamsung2.ui.search.UnifiedSearchScreen

@Composable
fun TripPlannerScreen(
    initialQuery: String = "",
    onClose: () -> Unit,
    onAddToPlan: (PlanItem) -> Unit
) {
    // 현재는 통합검색 화면을 그대로 래핑
    UnifiedSearchScreen(
        initialQuery = initialQuery,
        onClose = onClose,
        onAddToPlan = onAddToPlan
    )
}
