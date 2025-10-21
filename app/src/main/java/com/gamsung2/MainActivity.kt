// app/src/main/java/com/gamsung2/MainActivity.kt
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gamsung2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.gamsung2.auth.AuthViewModel
import com.gamsung2.nav.Routes
import com.gamsung2.nav.GamsungNav
import com.gamsung2.ui.components.BottomBar
import com.gamsung2.ui.components.SosTopBar
import com.gamsung2.ui.group.AddGroupMemberDialog
import com.gamsung2.ui.group.GroupSchedule
import com.gamsung2.ui.group.MemberDialogMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.math.max

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ ScaledApp 의존 제거: 바로 MyApp()로 그립니다.
        setContent { MyApp() }
    }
}

@Composable
private fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Activity 범위의 AuthViewModel
    val authVm: AuthViewModel = hiltViewModel()
    val ui by authVm.ui.collectAsState()

    val isLoggedIn = ui.session != null
    val displayName = ui.displayName ?: if (isLoggedIn) "회원" else "Gamsung2"

    // 최초 로그인 토스트(세션 생길 때 1회)
    var greeted by remember { mutableStateOf(false) }
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && !greeted) {
            Toast.makeText(context, "로그인되었습니다.", Toast.LENGTH_SHORT).show()
            greeted = true
        }
        if (!isLoggedIn) greeted = false
    }

    // ===== 그룹 다이얼로그 상태 =====
    var showDialog by remember { mutableStateOf(false) }
    var dialogMode by remember { mutableStateOf(MemberDialogMode.ADD) }
    var editingIndex by remember { mutableStateOf(-1) }
    val phones = remember { mutableStateListOf<String>() }
    var schedule by remember { mutableStateOf<GroupSchedule?>(null) }

    // 여행 종료 시 자동 해제
    LaunchedEffect(schedule?.endAt, phones.size) {
        val endAt = schedule?.endAt ?: return@LaunchedEffect
        delay(max(0L, endAt - System.currentTimeMillis()))
        if (phones.isNotEmpty()) {
            phones.clear()
            Toast.makeText(context, "여행 종료: 그룹 자동 해제 완료", Toast.LENGTH_SHORT).show()
        }
        schedule = null
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                SosTopBar(
                    title = if (isLoggedIn) "${displayName}님" else displayName,
                    subtitle = if (isLoggedIn) "여행중입니다." else null,
                    isLoggedIn = isLoggedIn,
                    onGroupClick = {
                        dialogMode = MemberDialogMode.ADD
                        editingIndex = -1
                        showDialog = true
                    },
                    onProfile = { if (isLoggedIn) navController.navigate(Routes.EDIT_PROFILE) },
                    onLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.AUTH_GATE) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        if (isLoggedIn) {
                            authVm.logout {
                                navController.navigate(Routes.AUTH_GATE) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { inner ->
            Surface(Modifier.padding(inner)) {
                // 같은 AuthViewModel을 Nav 그래프에 주입
                GamsungNav(
                    navController = navController,
                    authVm = authVm
                )
            }
        }

        if (showDialog) {
            val initialPhone =
                if (dialogMode == MemberDialogMode.EDIT && editingIndex in phones.indices)
                    phones[editingIndex] else null

            AddGroupMemberDialog(
                mode = dialogMode,
                initialPhone = initialPhone,
                onDismiss = { showDialog = false },
                existingPhones = phones,
                currentSchedule = schedule,
                onEditRequest = { idx ->
                    dialogMode = MemberDialogMode.EDIT
                    editingIndex = idx
                },
                onAddOrSave = { newPhone, newSchedule ->
                    if (newPhone.isNotBlank()) {
                        if (dialogMode == MemberDialogMode.ADD) phones += newPhone
                        else if (editingIndex in phones.indices) phones[editingIndex] = newPhone
                        showDialog = false
                    }
                    if (newSchedule != null) schedule = newSchedule
                },
                onDelete =
                    if (dialogMode == MemberDialogMode.EDIT && editingIndex in phones.indices) {
                        { phones.removeAt(editingIndex); showDialog = false }
                    } else null,
                onDisbandGroup =
                    if (phones.isNotEmpty() || schedule != null) {
                        { phones.clear(); schedule = null; showDialog = false }
                    } else null
            )
        }
    }
}
