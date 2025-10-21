// app/src/main/java/com/gamsung2/di/CoreModule.kt
package com.gamsung2.di

import android.content.Context
import com.gamsung2.data.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext ctx: Context): SessionManager =
        SessionManager(ctx)
}
