package com.gamsung2.ui.translate

/** 언어 코드/라벨 */
data class Lang(val code: String, val label: String)

/** 표시용 라벨 */
internal fun codeToLabel(code: String): String =
    SupportedLangs.firstOrNull { it.code == code }?.label ?: code

/** 즐겨 쓰는/지원 언어 모음 */
internal val SupportedLangs: List<Lang> = listOf(
    Lang("ko", "한국어"),
    Lang("en", "영어"),
    Lang("ja", "일본어"),
    Lang("zh", "중국어(간체)"),
    Lang("zh-TW", "중국어(번체)"),
    Lang("de", "독일어"),
    Lang("fr", "프랑스어"),
    Lang("es", "스페인어"),
    Lang("pt", "포르투갈어"),
    Lang("it", "이탈리아어"),
    Lang("ru", "러시아어"),
    Lang("vi", "베트남어"),
    Lang("th", "태국어"),
    Lang("id", "인도네시아어"),
    Lang("hi", "힌디어"),
    Lang("ar", "아랍어"),
    Lang("tr", "터키어"),
    Lang("nl", "네덜란드어"),
    Lang("sv", "스웨덴어"),
    Lang("pl", "폴란드어")
)
