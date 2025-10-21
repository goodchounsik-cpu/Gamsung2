package com.gamsung2.domain.plan

enum class Bucket { COURSE, FOOD, LODGING }
enum class VisitStatus { PLANNED, DONE }

data class PlanItem(
    val id: String,
    val title: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val bucket: Bucket = Bucket.COURSE,
    val status: VisitStatus = VisitStatus.PLANNED,
    val photos: List<String> = emptyList(),
    val visitedAt: Long? = null
)

data class TripStory(
    val courses: List<PlanItem> = emptyList(),
    val foods: List<PlanItem> = emptyList(),
    val lodgings: List<PlanItem> = emptyList()
)

data class BucketWish(
    val items: List<PlanItem> = emptyList()
)
