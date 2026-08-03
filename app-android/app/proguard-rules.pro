# RED Ultimate ProGuard Rules

# Keep application classes
-keep class com.red.** { *; }

# Keep Moshi adapters
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keep @com.squareup.moshi.JsonClass class *

# Keep Retrofit interfaces
-keep,allowobfuscation,allowshrinking interface com.red.feature.**Api

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep Hilt
-dontwarn dagger.hilt.**

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Coil
-dontwarn coil.**

# General Android
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Kotlin
-dontwarn kotlin.**
-keepclassmembers class kotlin.Metadata {
    public <methods>();
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# CameraX
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# Biometric
-keep class androidx.biometric.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.CoroutineWorker {
    public <methods>;
}

# Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
