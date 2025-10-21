// app/src/main/java/com/gamsung2/nav/GamsungNav.kt
package com.gamsung2.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gamsung2.ui.fav.FavoritesScreen
import com.gamsung2.ui.home.EventEditorScreen
import com.gamsung2.ui.home.HomeScreen
import com.gamsung2.ui.home.MonthCalendarScreen
import com.gamsung2.ui.map.MapScreen
import com.gamsung2.ui.place.CategoryPlaceListScreen
import com.gamsung2.ui.place.PlaceDetailRoute
import com.gamsung2.ui.search.UnifiedSearchScreen
import com.gamsung2.ui.theme.RecommendFullScreen
import com.gamsung2.ui.theme.ThemeDetailScreen
import com.gamsung2.ui.theme.ThemeGalleryScreen
import com.gamsung2.ui.theme.ThemeTravelScreen
import com.gamsung2.ui.translate.TranslateScreen

@Composable
fun GamsungNav(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME_NEAR
    ) {
        // 홈(주변/전국)
        composable(Routes.HOME_NEAR) { HomeScreen(navController, tabKey = "near") }
        composable(Routes.HOME_ALL)  { HomeScreen(navController, tabKey = "all") }

        // 테마 갤러리
        composable(Routes.THEME_GALLERY) {
            ThemeGalleryScreen(
                title = "추천 테마",
                count = 4,
                onBack = { navController.popBackStack() }
            )
        }

        // 테마 여행
        composable(
            route = "${Routes.THEME_TRAVEL}?key={key}&title={title}&region={region}",
            arguments = listOf(
                navArgument(Routes.Args.KEY)    { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE)  { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.REGION) { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStack ->
            val key    = backStack.arguments?.getString(Routes.Args.KEY).orEmpty()
            val title  = backStack.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val region = backStack.arguments?.getString(Routes.Args.REGION).orEmpty()
            ThemeTravelScreen(navController, key, title, region)
        }

        // 추천 코스 전체
        composable(
            route = "${Routes.RECO_FULL}?${Routes.Args.KEY}={${Routes.Args.KEY}}&" +
                    "${Routes.Args.GROUP}={${Routes.Args.GROUP}}&" +
                    "${Routes.Args.REGION}={${Routes.Args.REGION}}",
            arguments = listOf(
                navArgument(Routes.Args.KEY)    { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.GROUP)  { type = NavType.StringType; defaultValue = "가족" },
                navArgument(Routes.Args.REGION) { type = NavType.StringType; defaultValue = "서울" },
            )
        ) { backStack ->
            val key    = backStack.arguments?.getString(Routes.Args.KEY).orEmpty()
            val group  = backStack.arguments?.getString(Routes.Args.GROUP).orEmpty()
            val region = backStack.arguments?.getString(Routes.Args.REGION).orEmpty()
            RecommendFullScreen(navController, key, group, region)
        }

        // 테마 상세
        composable(
            route = "${Routes.THEME_DETAIL}?key={key}&title={title}",
            arguments = listOf(
                navArgument(Routes.Args.KEY)   { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE) { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStack ->
            val key   = backStack.arguments?.getString(Routes.Args.KEY).orEmpty()
            val title = backStack.arguments?.getString(Routes.Args.TITLE).orEmpty()
            ThemeDetailScreen(
                title = title,
                themeKey = key,
                onBack = { navController.popBackStack() },
                onCardClick = { placeId, placeTitle, companion ->
                    navController.safeNavigate(
                        Routes.placeDetailRoute(placeId, companion, placeTitle)
                    )
                },
                onSearchSimilar = { initial ->
                    navController.safeNavigate(
                        "${Routes.SEARCH}?${Routes.Args.QUERY}=${Uri.encode(initial.orEmpty())}"
                    )
                }
            )
        }

        // 카테고리별 장소 목록
        composable(
            route = Routes.CATEGORY_PLACE_LIST +
                    "?category={category}&title={title}&themeTitle={themeTitle}" +
                    "&lat={lat}&lng={lng}&radiusKm={radiusKm}&wide={wide}",
            arguments = listOf(
                navArgument("category")   { type = NavType.StringType; defaultValue = "lodging" },
                navArgument("title")      { type = NavType.StringType; defaultValue = "목록" },
                navArgument("themeTitle") { type = NavType.StringType; defaultValue = "" },
                navArgument("lat")        { type = NavType.StringType; defaultValue = "" },
                navArgument("lng")        { type = NavType.StringType; defaultValue = "" },
                navArgument("radiusKm")   { type = NavType.StringType; defaultValue = "3" },
                navArgument(Routes.Args.WIDE) { type = NavType.StringType; defaultValue = "0" },
            )
        ) { backStack ->
            val category   = backStack.arguments?.getString("category") ?: "lodging"
            val title      = backStack.arguments?.getString("title") ?: "목록"
            val themeTitle = backStack.arguments?.getString("themeTitle").orEmpty()
            val lat        = backStack.arguments?.getString("lat")?.toDoubleOrNull()
            val lng        = backStack.arguments?.getString("lng")?.toDoubleOrNull()
            val radiusKm   = backStack.arguments?.getString("radiusKm")?.toDoubleOrNull() ?: 3.0
            val wide       = (backStack.arguments?.getString(Routes.Args.WIDE) ?: "0") == "1"

            CategoryPlaceListScreen(
                navController = navController,
                category = category,
                title = title,
                themeTitle = themeTitle,
                lat = lat, lng = lng,
                radiusKm = radiusKm,
                showWide = wide
            )
        }

        // 장소 상세
        composable(
            route =
                "${Routes.PLACE_DETAIL}/{${Routes.Args.PLACE_ID}}" +
                        "?${Routes.Args.COMPANION}={${Routes.Args.COMPANION}}" +
                        "&${Routes.Args.TITLE}={${Routes.Args.TITLE}}" +
                        "&${Routes.Args.LAT}={${Routes.Args.LAT}}" +
                        "&${Routes.Args.LON}={${Routes.Args.LON}}",
            arguments = listOf(
                navArgument(Routes.Args.PLACE_ID)  { type = NavType.StringType },
                navArgument(Routes.Args.COMPANION) { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE)     { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LAT)       { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LON)       { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStack ->
            val placeId   = backStack.arguments?.getString(Routes.Args.PLACE_ID) ?: ""
            val companion = backStack.arguments?.getString(Routes.Args.COMPANION).orEmpty()
            val title     = backStack.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val lat       = backStack.arguments?.getString(Routes.Args.LAT)?.toDoubleOrNull()
            val lon       = backStack.arguments?.getString(Routes.Args.LON)?.toDoubleOrNull()

            PlaceDetailRoute(
                placeId = placeId,
                title = title,
                companion = companion,
                lat = lat,
                lon = lon,
                onBack = { navController.popBackStack() },
                onNavigateToMap = { la, lo, _ ->
                    navController.safeNavigate(Routes.mapRoute(la, lo, title))
                },
                onFindNearby = { category, la, lo, aroundTitle ->
                    navController.safeNavigate(
                        Routes.CATEGORY_PLACE_LIST +
                                "?category=$category" +
                                "&title=${Uri.encode(if (category == "lodging") "숙소 찾기" else "식당 찾기")}" +
                                "&themeTitle=${Uri.encode("$aroundTitle 근처")}" +
                                "&lat=$la&lng=$lo&radiusKm=3&wide=0"
                    )
                }
            )
        }

        // 지도 / 달력
        composable(Routes.MAP)            { MapScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CALENDAR_MONTH) { MonthCalendarScreen(navController) }

        // 일정 편집
        composable(
            route = "${Routes.EVENT_EDITOR}?date={date}&eventId={eventId}",
            arguments = listOf(
                navArgument(Routes.Args.DATE)     { type = NavType.StringType },
                navArgument(Routes.Args.EVENT_ID) { type = NavType.LongType; defaultValue = -1L },
            )
        ) { backStack ->
            val date = backStack.arguments?.getString(Routes.Args.DATE).orEmpty()
            val eid  = backStack.arguments?.getLong(Routes.Args.EVENT_ID) ?: -1L
            EventEditorScreen(
                date = date,
                eventId = if (eid >= 0) eid else null,
                onClose = { navController.popBackStack() }
            )
        }

        // ✅ 즐겨찾기 / 번역: 여기 반드시 존재해야 함
        composable(Routes.FAVORITES) { FavoritesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.TRANSLATE) { TranslateScreen(onBack = { navController.popBackStack() }) }

        // 통합 검색
        composable(
            route = "${Routes.SEARCH}?${Routes.Args.QUERY}={${Routes.Args.QUERY}}",
            arguments = listOf(
                navArgument(Routes.Args.QUERY) { type = NavType.StringType; defaultValue = "" },
            )
        ) { backStack ->
            val initial = backStack.arguments?.getString(Routes.Args.QUERY).orEmpty()
            UnifiedSearchScreen(
                initialQuery = initial,
                onClose = { navController.popBackStack() },
                onAddToPlan = { navController.popBackStack() }
            )
        }
    }
}

/** 중복 클릭로 인한 예외 방지 */
private fun NavHostController.safeNavigate(route: String) {
    val current = currentBackStackEntry?.destination?.route
    if (current != route) navigate(route)
}
