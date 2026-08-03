# Rules for the RED feature surface merged into the main Signal application.
# Keep generated Hilt/Room/Moshi entry points discoverable in release builds.
-keep class com.red.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep,allowobfuscation,allowshrinking interface com.red.feature.**Api
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
