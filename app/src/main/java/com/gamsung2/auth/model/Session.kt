// app/src/main/java/com/gamsung2/auth/model/Session.kt
package com.gamsung2.auth.model
data class Session(
    val token: String,
    val userType: String? = null,
    val userId: String? = null
)
