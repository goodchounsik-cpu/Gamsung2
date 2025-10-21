// app/src/main/java/com/gamsung2/util/Validators.kt
package com.gamsung2.util

fun isValidEmail(s: String) = s.contains("@") && s.contains(".")
fun isValidPassword(s: String) = s.length >= 6
