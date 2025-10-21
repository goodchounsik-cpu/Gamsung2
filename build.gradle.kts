plugins {
    id("com.android.application") version "8.6.1" apply false
    id("com.android.library")     version "8.6.1" apply false

    // Compose 1.5.15 ↔ Kotlin 1.9.25 호환
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false

    // ✅ KSP는 이걸로 (존재하는 버전)
    id("com.google.devtools.ksp") version "1.9.25-1.0.20" apply false

    id("com.google.dagger.hilt.android") version "2.51" apply false
    id("com.google.gms.google-services")  version "4.4.2" apply false
}
