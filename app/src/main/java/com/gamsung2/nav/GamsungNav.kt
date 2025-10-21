package com.gamsung2.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gamsung2.MapViewModel
import com.gamsung2.auth.AuthViewModel
import com.gamsung2.domain.plan.Bucket
import com.gamsung2.ui.auth.*
import com.gamsung2.ui.bucket.BucketScreen
import com.gamsung2.ui.home.HomeScreen
import com.gamsung2.ui.home.MonthCalendarScreen
import com.gamsung2.ui.map.MapScreen
import com.gamsung2.ui.place.CategoryPlaceListScreen
import com.gamsung2.ui.place.PlaceDetailRoute
import com.gamsung2.ui.search.UnifiedSearchScreen as SearchScreen
import com.gamsung2.ui.settings.EditProfileScreen
import com.gamsung2.ui.theme.RecommendFullScreen
import com.gamsung2.ui.theme.ThemeGalleryScreen
import com.gamsung2.ui.theme.ThemeTravelScreen
import com.gamsung2.ui.translate.FavListScreen
import com.gamsung2.ui.translate.TranslateRelationScreen
import com.gamsung2.ui.translate.TranslateViewModel

@Composable
fun GamsungNav(
    navController: NavHostController,
    authVm: AuthViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.AUTH_GATE
    ) {
        /* ---------- 인증 게이트 ---------- */
        composable(Routes.AUTH_GATE) {
            AuthGate(
                onReadyHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GATE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNeedLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.AUTH_GATE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        /* ---------- 로그인/회원가입 ---------- */
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = authVm,
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GATE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoSignUp = { navController.navigate(Routes.SIGNUP_CHOICE) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SIGNUP_CHOICE) {
            SignUpChoiceScreen(
                onGeneral = { navController.navigate(Routes.SIGNUP_GENERAL) },
                onBiz = { navController.navigate(Routes.SIGNUP_BIZ) },
                onGov = { /* TODO */ },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SIGNUP_GENERAL) {
            GeneralSignUpScreen(
                vm = authVm,
                onSignedUpAndConfirmed = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GATE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SIGNUP_BIZ) {
            BizSignUpScreen(
                vm = authVm,
                onDone = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH_GATE) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        /* ---------- 홈/지도/달력 ---------- */
        composable(Routes.HOME)      { HomeScreen(navController, tabKey = "near") }
        composable(Routes.HOME_NEAR) { HomeScreen(navController, tabKey = "near") }
        composable(Routes.HOME_ALL)  { HomeScreen(navController, tabKey = "all") }
        composable(Routes.CALENDAR_MONTH) { MonthCalendarScreen() }
        composable(Routes.MAP) { MapScreen(navController) }

        /* ---------- 즐겨찾기/번역 ---------- */
        composable(Routes.FAVORITES) {
            val mapVm: MapViewModel = hiltViewModel()
            FavListScreen(
                vm = mapVm,
                onBack = { navController.popBackStack() },
                onEdit = { /* TODO */ },
                onSnack = { /* TODO */ }
            )
        }
        composable(Routes.TRANSLATE) {
            val tVm = viewModel<TranslateViewModel>()
            TranslateRelationScreen(vm = tVm, onBack = { navController.popBackStack() })
        }

        /* ---------- 테마 ---------- */
        composable(Routes.THEME_GALLERY) {
            ThemeGalleryScreen(title = "추천 테마", count = 4, onBack = { navController.popBackStack() })
        }
        composable(
            route = "${Routes.THEME_TRAVEL}?key={key}&title={title}&region={region}",
            arguments = listOf(
                navArgument(Routes.Args.KEY)    { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE)  { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.REGION) { type = NavType.StringType; defaultValue = "" },
            )
        ) { entry: NavBackStackEntry ->
            val key    = entry.arguments?.getString(Routes.Args.KEY).orEmpty()
            val title  = entry.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val region = entry.arguments?.getString(Routes.Args.REGION).orEmpty()
            ThemeTravelScreen(navController, key, title, region)
        }
        composable(
            route = "${Routes.RECO_FULL}?${Routes.Args.KEY}={${Routes.Args.KEY}}&" +
                    "${Routes.Args.GROUP}={${Routes.Args.GROUP}}&" +
                    "${Routes.Args.REGION}={${Routes.Args.REGION}}"
        ) { entry: NavBackStackEntry ->
            val key    = entry.arguments?.getString(Routes.Args.KEY).orEmpty()
            val group  = entry.arguments?.getString(Routes.Args.GROUP).orEmpty()
            val region = entry.arguments?.getString(Routes.Args.REGION).orEmpty()
            RecommendFullScreen(navController, key, group, region)
        }

        /* ---------- 카테고리별 장소 목록 ---------- */
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
        ) { entry: NavBackStackEntry ->
            val category   = entry.arguments?.getString("category") ?: "lodging"
            val title      = entry.arguments?.getString("title") ?: "목록"
            val themeTitle = entry.arguments?.getString("themeTitle").orEmpty()
            val lat        = entry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng        = entry.arguments?.getString("lng")?.toDoubleOrNull()
            val radiusKm   = entry.arguments?.getString("radiusKm")?.toDoubleOrNull() ?: 3.0
            val wide       = (entry.arguments?.getString(Routes.Args.WIDE) ?: "0") == "1"

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

        /* ---------- 장소 상세 ---------- */
        composable(
            route = "${Routes.PLACE_DETAIL}/{${Routes.Args.PLACE_ID}}" +
                    "?${Routes.Args.COMPANION}={${Routes.Args.COMPANION}}" +
                    "&${Routes.Args.TITLE}={${Routes.Args.TITLE}}" +
                    "&${Routes.Args.LAT}={${Routes.Args.LAT}}" +
                    "&${Routes.Args.LON}={${Routes.Args.LON}}" +
                    "&${Routes.Args.BUCKET}={${Routes.Args.BUCKET}}",
            arguments = listOf(
                navArgument(Routes.Args.PLACE_ID)  { type = NavType.StringType },
                navArgument(Routes.Args.COMPANION) { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.TITLE)     { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LAT)       { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.LON)       { type = NavType.StringType; defaultValue = "" },
                navArgument(Routes.Args.BUCKET)    { type = NavType.StringType; defaultValue = "" },
            )
        ) { entry: NavBackStackEntry ->
            val placeId   = entry.arguments?.getString(Routes.Args.PLACE_ID) ?: ""
            val companion = entry.arguments?.getString(Routes.Args.COMPANION).orEmpty()
            val title     = entry.arguments?.getString(Routes.Args.TITLE).orEmpty()
            val lat       = entry.arguments?.getString(Routes.Args.LAT)?.toDoubleOrNull()
            val lon       = entry.arguments?.getString(Routes.Args.LON)?.toDoubleOrNull()
            val bucketArg = entry.arguments?.getString(Routes.Args.BUCKET).orEmpty()
            val initialBucket: Bucket? = when (bucketArg.lowercase()) {
                "course"  -> Bucket.COURSE
                "food"    -> Bucket.FOOD
                "lodging" -> Bucket.LODGING
                else      -> null
            }

            PlaceDetailRoute(
                placeId = placeId,
                title = title,
                companion = companion,
                lat = lat,
                lon = lon,
                initialBucket = initialBucket,
                onBack = { navController.popBackStack() },
                onNavigateToMap = { la, lo, label ->
                    val a = la ?: lat ?: return@PlaceDetailRoute
                    val o = lo ?: lon ?: return@PlaceDetailRoute
                    navController.navigate(Routes.mapRoute(lat = a, lon = o, label = label ?: ""))
                },
                onFindNearbyLodging = { la, lo ->
                    val a = la ?: lat ?: return@PlaceDetailRoute
                    val o = lo ?: lon ?: return@PlaceDetailRoute
                    navController.navigate(
                        Routes.categoryPlaceListRoute(
                            category = "lodging",
                            title = "숙소 찾기",
                            themeTitle = "${title.ifBlank { "추천 코스" }} 근처",
                            lat = a, lng = o, radiusKm = 3.0, wide = true
                        )
                    )
                },
                onFindNearbyFood = { la, lo ->
                    val a = la ?: lat ?: return@PlaceDetailRoute
                    val o = lo ?: lon ?: return@PlaceDetailRoute
                    navController.navigate(
                        Routes.categoryPlaceListRoute(
                            category = "restaurant",
                            title = "식당 찾기",
                            themeTitle = "${title.ifBlank { "추천 코스" }} 근처",
                            lat = a, lng = o, radiusKm = 3.0, wide = true
                        )
                    )
                },
                // 🔵 바텀시트 선택 이동 연결
                onNavigateToMyTrip = { _, _ ->
                    navController.navigate(Routes.CALENDAR_MONTH)
                },
                onNavigateToBucket = { _, _ ->
                    navController.navigate(Routes.BUCKET_HOME)
                }
            )
        }

        /* ---------- 검색 ---------- */
        composable(
            route = "${Routes.SEARCH}?${Routes.Args.QUERY}={${Routes.Args.QUERY}}&bucket={bucket}",
            arguments = listOf(
                navArgument(Routes.Args.QUERY) { type = NavType.StringType; defaultValue = "" },
                navArgument("bucket")          { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry: NavBackStackEntry ->
            val q = entry.arguments?.getString(Routes.Args.QUERY).orEmpty()

            SearchScreen(
                initialQuery = q,
                onClose = { navController.popBackStack() },
                onAddToPlan = { item ->
                    val bucketStr = when (item.bucket) {
                        Bucket.COURSE  -> "course"
                        Bucket.FOOD    -> "food"
                        Bucket.LODGING -> "lodging"
                    }
                    navController.navigate(
                        Routes.placeDetailRoute(
                            placeId = item.id,
                            title   = item.title,
                            bucket  = bucketStr
                        )
                    )
                }
            )
        }

        /* ---------- 버킷 홈 ---------- */
        composable(Routes.BUCKET_HOME) {
            BucketScreen(
                onBack = { navController.popBackStack() },
                onOpenPlace = { id, title ->
                    navController.navigate(Routes.placeDetailRoute(placeId = id, title = title))
                }
            )
        }

        /* ---------- 프로필 편집 ---------- */
        composable(Routes.EDIT_PROFILE) {
            val vm: AuthViewModel = hiltViewModel()
            EditProfileScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onDeleted = {
                    navController.navigate(Routes.AUTH_GATE) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun AuthGate(
    onReadyHome: () -> Unit,
    onNeedLogin: () -> Unit
) {
    LaunchedEffect(Unit) { onNeedLogin() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
