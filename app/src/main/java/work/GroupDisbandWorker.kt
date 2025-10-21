package com.gamsung2.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 예약된 시각에 실행되어 그룹을 해제하는 워커.
 * 실제 해제 로직은 TODO 부분에서 Repository/DB 호출로 연결하세요.
 *
 * enqueue 시 입력 값:
 *  - INPUT_GROUP_ID (String) : 대상 그룹 ID (선택)
 */
class GroupDisbandWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // 입력값 예시 (필요 없으면 제거 가능)
            val groupId: String? = inputData.getString(INPUT_GROUP_ID)

            // TODO: 실제 그룹 해제 로직 수행
            // ex) GroupRepository.get(applicationContext).disband(groupId)
            // 또는 로컬 DB 정리: GroupDao.clearAll()

            Result.success()
        } catch (t: Throwable) {
            // 재시도 필요하면 retry, 아니면 failure
            Result.retry()
        }
    }

    companion object {
        const val INPUT_GROUP_ID = "group_id"

        fun buildInput(groupId: String?): Data =
            Data.Builder().apply {
                if (!groupId.isNullOrBlank()) putString(INPUT_GROUP_ID, groupId)
            }.build()
    }
}
