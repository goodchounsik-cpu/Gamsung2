package com.gamsung2.auth

import com.gamsung2.auth.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private var session: Session? = null
    private var profile: UserProfile? = null

    override suspend fun login(form: LoginForm): Session {
        delay(120)
        session = Session(token = "fake-token:${form.email}", userType = "general")
        if (profile == null) {
            profile = UserProfile(
                name = form.email.substringBefore('@'),
                email = form.email,
                phone = null
            )
        }
        return session!!
    }

    override suspend fun loginByPhone(phone: String, password: String): Session {
        delay(120)
        session = Session(token = "fake-token:$phone", userType = "general")
        if (profile == null) {
            // 데모용 이메일 대체
            profile = UserProfile(
                name = "회원",
                email = "$phone@example.com",
                phone = phone
            )
        }
        return session!!
    }

    override suspend fun signUpGeneral(form: GeneralSignUpForm): Session? {
        delay(150)
        session = Session(token = "fake-token:${form.email}", userType = "general")
        profile = UserProfile(name = form.name, email = form.email, phone = form.phone)
        return session
    }

    override suspend fun signUpBiz(form: BizSignUpForm): Session? {
        delay(150)
        session = Session(token = "fake-token:${form.email}", userType = "biz")
        profile = UserProfile(name = form.owner, email = form.email, phone = form.mobilePhone)
        return session
    }

    override suspend fun loginWithSocial(provider: SocialProvider): Session? {
        delay(120)
        val type = provider.name.lowercase()
        session = Session(token = "fake-social-token:$type", userType = type)
        if (profile == null) {
            profile = UserProfile(name = "소셜회원", email = "$type@example.com", phone = null)
        }
        return session
    }

    override suspend fun getMyProfile(): UserProfile? = profile

    override suspend fun updateMyProfile(profile: UserProfile) {
        delay(80)
        this.profile = profile
    }

    override suspend fun deleteAccount() {
        delay(80)
        session = null
        profile = null
    }
}
