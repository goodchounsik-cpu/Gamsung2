// app/src/main/java/com/gamsung2/auth/model/BizSignUpForm.kt
package com.gamsung2.auth.model
data class BizSignUpForm(
    val email: String,
    val password: String,
    val bizName: String,
    val owner: String,
    val address: String,
    val mobilePhone: String,
    val landlinePhone: String? = null
)
