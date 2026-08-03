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
