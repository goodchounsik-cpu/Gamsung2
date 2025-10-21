// app/src/main/java/com/gamsung2/auth/di/CoreModule.kt
package com.gamsung2.auth.di

import com.gamsung2.auth.FakeAuthRepository
import com.gamsung2.auth.model.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds @Singleton
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository
}
