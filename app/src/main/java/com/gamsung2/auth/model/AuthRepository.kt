package com.gamsung2.auth.model

interface AuthRepository {
    suspend fun login(form: LoginForm): Session
    suspend fun loginByPhone(phone: String, password: String): Session

    suspend fun signUpGeneral(form: GeneralSignUpForm): Session?
    suspend fun signUpBiz(form: BizSignUpForm): Session?

    suspend fun loginWithSocial(provider: SocialProvider): Session?

    suspend fun getMyProfile(): UserProfile?
    suspend fun updateMyProfile(profile: UserProfile)
    suspend fun deleteAccount()
}
