package com.gamsung2.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamsung2.auth.model.*
import com.gamsung2.data.SessionManager
import com.gamsung2.util.isValidEmail
import com.gamsung2.util.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val sessionStore: SessionManager
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val session: Session? = null,
        val displayName: String? = null,
        val profile: UserProfile? = null,
        val profileSaving: Boolean = false
    )

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    fun clearError() { _ui.value = _ui.value.copy(error = null) }

    // ── 이메일 로그인
    fun login(email: String, pw: String, onSuccess: () -> Unit) = viewModelScope.launch {
        if (!isValidEmail(email)) { _ui.value = _ui.value.copy(error = "이메일 형식이 올바르지 않습니다"); return@launch }
        if (!isValidPassword(pw)) { _ui.value = _ui.value.copy(error = "비밀번호는 6자 이상이어야 합니다"); return@launch }

        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val session = repo.login(LoginForm(email, pw))
            saveSessionSafely(session, "general")
            val fetched = runCatching { repo.getMyProfile() }.getOrNull()
            val profile = fetched ?: UserProfile(email.substringBefore('@'), email, null)
            _ui.value = UiState(
                loading = false,
                session = session,
                displayName = profile.name,
                profile = profile
            )
            onSuccess()
        } catch (e: Exception) {
            _ui.value = UiState(loading = false, error = e.message ?: "로그인 실패")
        }
    }

    // ── 전화번호 로그인
    fun loginByPhone(phone: String, pw: String, onSuccess: () -> Unit) = viewModelScope.launch {
        if (phone.filter { it.isDigit() }.length < 9) { _ui.value = _ui.value.copy(error = "전화번호를 정확히 입력하세요"); return@launch }
        if (!isValidPassword(pw)) { _ui.value = _ui.value.copy(error = "비밀번호는 6자 이상이어야 합니다"); return@launch }

        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val session = repo.loginByPhone(phone, pw)
            saveSessionSafely(session, "general")
            val profile = runCatching { repo.getMyProfile() }.getOrNull()
                ?: UserProfile(name = "회원", email = "$phone@example.com", phone = phone)
            _ui.value = UiState(
                loading = false,
                session = session,
                displayName = profile.name,
                profile = profile
            )
            onSuccess()
        } catch (e: Exception) {
            _ui.value = UiState(loading = false, error = e.message ?: "로그인 실패")
        }
    }

    // ── 소셜 로그인
    fun loginWithSocial(provider: SocialProvider, onSuccess: () -> Unit) = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val session = repo.loginWithSocial(provider)
                ?: run { _ui.value = _ui.value.copy(loading = false, error = "소셜 로그인 실패"); return@launch }
            saveSessionSafely(session, provider.name.lowercase())
            val fetched = runCatching { repo.getMyProfile() }.getOrNull()
            _ui.value = UiState(
                loading = false,
                session = session,
                displayName = fetched?.name,
                profile = fetched
            )
            onSuccess()
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(loading = false, error = e.message ?: "소셜 로그인 실패")
        }
    }

    // ── 회원가입(일반/소상공인)
    fun signUpGeneral(name: String, email: String, pw: String, phone: String, onDone: () -> Unit) = viewModelScope.launch {
        if (name.isBlank()) { _ui.value = _ui.value.copy(error = "이름을 입력하세요"); return@launch }
        if (!isValidEmail(email)) { _ui.value = _ui.value.copy(error = "이메일 형식이 올바르지 않습니다"); return@launch }
        if (!isValidPassword(pw)) { _ui.value = _ui.value.copy(error = "비밀번호는 6자 이상이어야 합니다"); return@launch }
        if (phone.filter { it.isDigit() }.length < 9) { _ui.value = _ui.value.copy(error = "전화번호를 정확히 입력하세요"); return@launch }

        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val session = repo.signUpGeneral(GeneralSignUpForm(email, pw, name, phone))
                ?: run { _ui.value = _ui.value.copy(loading = false, error = "가입 실패"); return@launch }
            saveSessionSafely(session, "general")
            val profile = UserProfile(name = name, email = email, phone = phone)
            _ui.value = _ui.value.copy(loading = false, session = session, displayName = name, profile = profile)
            onDone()
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(loading = false, error = e.message ?: "가입 실패")
        }
    }

    fun signUpBiz(
        bizName: String, owner: String, email: String, pw: String,
        address: String, mobilePhone: String, landlinePhone: String?, onDone: () -> Unit
    ) = viewModelScope.launch {
        if (bizName.isBlank()) { _ui.value = _ui.value.copy(error = "업체명을 입력하세요"); return@launch }
        if (owner.isBlank()) { _ui.value = _ui.value.copy(error = "대표자명을 입력하세요"); return@launch }
        if (address.isBlank()) { _ui.value = _ui.value.copy(error = "주소를 입력하세요"); return@launch }
        if (mobilePhone.filter { it.isDigit() }.length < 9) { _ui.value = _ui.value.copy(error = "휴대전화를 정확히 입력하세요"); return@launch }
        if (!isValidEmail(email)) { _ui.value = _ui.value.copy(error = "이메일 형식이 올바르지 않습니다"); return@launch }
        if (!isValidPassword(pw)) { _ui.value = _ui.value.copy(error = "비밀번호는 6자 이상이어야 합니다"); return@launch }

        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val session = repo.signUpBiz(BizSignUpForm(email, pw, bizName, owner, address, mobilePhone, landlinePhone))
                ?: run { _ui.value = _ui.value.copy(loading = false, error = "가입 실패"); return@launch }
            saveSessionSafely(session, "biz")
            val profile = UserProfile(owner, email, mobilePhone)
            _ui.value = _ui.value.copy(loading = false, session = session, displayName = owner, profile = profile)
            onDone()
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(loading = false, error = e.message ?: "가입 실패")
        }
    }

    // ── 프로필
    fun loadProfile() = viewModelScope.launch {
        if (_ui.value.profile != null) return@launch
        _ui.value = _ui.value.copy(loading = true, error = null)
        try {
            val p = repo.getMyProfile()
            _ui.value = _ui.value.copy(
                loading = false,
                profile = p,
                displayName = p?.name ?: _ui.value.displayName
            )
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(loading = false, error = e.message)
        }
    }

    fun updateProfile(name: String, phone: String?) = viewModelScope.launch {
        _ui.value = _ui.value.copy(profileSaving = true, error = null)
        try {
            val current = _ui.value.profile ?: return@launch
            val newProfile = current.copy(name = name, phone = phone?.ifBlank { null })
            runCatching { repo.updateMyProfile(newProfile) }
            _ui.value = _ui.value.copy(profileSaving = false, profile = newProfile, displayName = newProfile.name)
        } catch (e: Exception) {
            _ui.value = _ui.value.copy(profileSaving = false, error = e.message)
        }
    }

    // ── 세션
    fun logout(onDone: () -> Unit = {}) = viewModelScope.launch {
        _ui.value = UiState()
        onDone()
    }

    fun withdraw(onDone: () -> Unit = {}) = viewModelScope.launch {
        runCatching { repo.deleteAccount() }
        _ui.value = UiState()
        onDone()
    }

    private suspend fun saveSessionSafely(session: Session, fallbackUserType: String) {
        val token = try { session.token } catch (_: Throwable) { null }
        val userType = try { session.userType ?: fallbackUserType } catch (_: Throwable) { fallbackUserType }
        if (!token.isNullOrBlank()) sessionStore.saveSession(token, userType)
    }
}
