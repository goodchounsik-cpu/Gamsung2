package com.gamsung2.nav

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// 화면들
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

@Deprecated("Use GamsungNav in GamsungNav.kt")
@Composable
fun GamsungNavLegacy(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME_NEAR
    ) {
        composable(Routes.HOME_NEAR) { HomeScreen(navController, tabKey = "near") }
        composable(Routes.HOME_ALL)  { HomeScreen(navController, tabKey = "all") }

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
                navArgument(Routes.Args.REGION) { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val key    = backStackEntry.arguments?.getString(Routes.Args.KEY).orEmpty()
            val title  = backStackEntry.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val region = backStackEntry.arguments?.getString(Routes.Args.REGION).orEmpty()
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
                navArgument(Routes.Args.REGION) { type = NavType.StringType; defaultValue = "서울" }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val key    = backStackEntry.arguments?.getString(Routes.Args.KEY).orEmpty()
            val group  = backStackEntry.arguments?.getString(Routes.Args.GROUP).orEmpty()
            val region = backStackEntry.arguments?.getString(Routes.Args.REGION).orEmpty()
            RecommendFullScreen(navController, key, group, region)
        }

        // 테마 상세
        composable(
            route = "${Routes.THEME_DETAIL}?key={key}&title={title}",
            arguments = listOf(
                navArgument(Routes.Args.KEY)   { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE) { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val key   = backStackEntry.arguments?.getString(Routes.Args.KEY).orEmpty()
            val title = backStackEntry.arguments?.getString(Routes.Args.TITLE).orEmpty()
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
                navArgument(Routes.Args.WIDE) { type = NavType.StringType; defaultValue = "0" }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val category   = backStackEntry.arguments?.getString("category") ?: "lodging"
            val title      = backStackEntry.arguments?.getString("title") ?: "목록"
            val themeTitle = backStackEntry.arguments?.getString("themeTitle").orEmpty()
            val lat        = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng        = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            val radiusKm   = backStackEntry.arguments?.getString("radiusKm")?.toDoubleOrNull() ?: 3.0
            val wide       = (backStackEntry.arguments?.getString(Routes.Args.WIDE) ?: "0") == "1"

            CategoryPlaceListScreen(
                navController = navController,
                category = category,
                title = title,
                themeTitle = themeTitle,
                lat = lat,
                lng = lng,
                radiusKm = radiusKm,
                showWide = wide
            )
        }

        // 장소 상세
        composable(
            route = "${Routes.PLACE_DETAIL}/{${Routes.Args.PLACE_ID}}" +
                    "?${Routes.Args.COMPANION}={${Routes.Args.COMPANION}}" +
                    "&${Routes.Args.TITLE}={${Routes.Args.TITLE}}" +
                    "&${Routes.Args.LAT}={${Routes.Args.LAT}}" +
                    "&${Routes.Args.LON}={${Routes.Args.LON}}",
            arguments = listOf(
                navArgument(Routes.Args.PLACE_ID)  { type = NavType.StringType },
                navArgument(Routes.Args.COMPANION) { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE)     { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LAT)       { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LON)       { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val placeId   = backStackEntry.arguments?.getString(Routes.Args.PLACE_ID) ?: ""
            val companion = backStackEntry.arguments?.getString(Routes.Args.COMPANION).orEmpty()
            val title     = backStackEntry.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val lat       = backStackEntry.arguments?.getString(Routes.Args.LAT)?.toDoubleOrNull()
            val lon       = backStackEntry.arguments?.getString(Routes.Args.LON)?.toDoubleOrNull()

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
                    val t = if (category == "lodging") "숙소 찾기" else "식당 찾기"
                    navController.safeNavigate(
                        Routes.CATEGORY_PLACE_LIST +
                                "?category=$category" +
                                "&title=${Uri.encode(t)}" +
                                "&themeTitle=${Uri.encode("$aroundTitle 근처")}" +
                                "&lat=$la&lng=$lo&radiusKm=3&wide=0"
                    )
                }
            )
        }

        composable(Routes.MAP)            { MapScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.CALENDAR_MONTH) { MonthCalendarScreen() }

        // 일정 편집
        composable(
            route = "${Routes.EVENT_EDITOR}?date={date}&eventId={eventId}",
            arguments = listOf(
                navArgument(Routes.Args.DATE)     { type = NavType.StringType },
                navArgument(Routes.Args.EVENT_ID) { type = NavType.LongType; defaultValue = -1L }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val date = backStackEntry.arguments?.getString(Routes.Args.DATE).orEmpty()
            val eid  = backStackEntry.arguments?.getLong(Routes.Args.EVENT_ID) ?: -1L
            EventEditorScreen(
                date = date,
                eventId = if (eid >= 0) eid else null,
                onClose = { navController.popBackStack() }
            )
        }

        // 즐겨찾기 / 번역
        composable(Routes.FAVORITES) { FavoritesScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.TRANSLATE) { TranslateScreen(onBack = { navController.popBackStack() }) }

        // 통합 검색  ← ★ 여기서 getString(key=…) 오타가 가장 많이 납니다
        composable(
            route = "${Routes.SEARCH}?${Routes.Args.QUERY}={${Routes.Args.QUERY}}",
            arguments = listOf(
                navArgument(Routes.Args.QUERY) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry: NavBackStackEntry ->
            val initial = backStackEntry.arguments?.getString(Routes.Args.QUERY).orEmpty()
            UnifiedSearchScreen(
                initialQuery = initial,
                onClose = { navController.popBackStack() },
                onAddToPlan = { navController.popBackStack() }
            )
        }
    }
}

/** 중복 클릭로 인한 IllegalArgumentException 방지 */
private fun NavHostController.safeNavigate(route: String) {
    val current = currentBackStackEntry?.destination?.route
    if (current != route) navigate(route)
}
