package com.gamsung2.data.local

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

/**
 * 그룹 생성/참여/위치 업데이트/멤버 구독을 담당하는 저장소.
 *
 * Firebase 세팅 필수:
 * - app/build.gradle: firebase-bom, firebase-auth-ktx, firebase-firestore-ktx,
 *   kotlinx-coroutines-play-services 의존성 추가
 * - google-services 플러그인 적용 및 google-services.json 배치
 * - Firebase Authentication: Anonymous sign-in 활성화
 * - Firestore DB 생성(개발 규칙 허용)
 */
class GroupRepository {

    private val db = Firebase.firestore
    private val auth get() = Firebase.auth

    /** 상위 groups 컬렉션 참조 */
    private fun groups(): CollectionReference = db.collection("groups")

    /** 현재 사용자 익명 로그인 보장 후 uid 리턴 */
    suspend fun ensureAnonSignIn(): String {
        val user = auth.currentUser ?: run {
            val res: AuthResult = auth.signInAnonymously().await()
            res.user ?: error("Anonymous sign-in failed: user == null")
        }
        return user.uid
    }

    /** 6자리 코드로 새 그룹 생성 */
    suspend fun createGroup(): String {
        val code = (100_000 + Random.nextInt(900_000)).toString()
        // 이미 존재하면 충돌할 수 있음 — MVP에선 1회 시도
        groups().document(code).set(GroupMeta(code = code)).await()
        return code
    }

    /** 코드로 그룹 참가 (멤버 문서 초기 생성) */
    suspend fun joinGroup(code: String, name: String): String {
        val uid = ensureAnonSignIn()
        val member = Member(uid = uid, name = name)
        groups().document(code).collection("members").document(uid).set(member).await()
        return uid
    }

    /** 내 현재 위치/배터리 갱신 */
    suspend fun updateLocation(code: String, lat: Double, lng: Double, battery: Int) {
        val uid = ensureAnonSignIn()
        val patch = mapOf(
            "lat" to lat,
            "lng" to lng,
            "lastTs" to System.currentTimeMillis(),
            "battery" to battery
        )
        groups().document(code).collection("members").document(uid).update(patch).await()
    }

    /**
     * 멤버 목록을 실시간 구독 (Flow)
     * 사용 예:
     *   repo.membersFlow(code).collect { list -> ... }
     */
    fun membersFlow(code: String): Flow<List<Member>> = callbackFlow {
        val reg = groups().document(code).collection("members")
            .addSnapshotListener { snap: QuerySnapshot?, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents
                    ?.mapNotNull { it.toObject(Member::class.java) }
                    .orEmpty()
                    .sortedBy { it.name }
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
