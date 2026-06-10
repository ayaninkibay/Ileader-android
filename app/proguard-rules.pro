# ─────────────────────────────────────────────────────────────────────
# kotlinx.serialization
# ─────────────────────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Synthetic methods on @Serializable companions are erased by R8 otherwise.
-keepclassmembers @kotlinx.serialization.Serializable class * {
    static **$* *;
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable data classes (DTO + domain).
-keep class com.ileader.app.data.remote.dto.** { *; }
-keep class com.ileader.app.data.models.** { *; }

# ─────────────────────────────────────────────────────────────────────
# Kotlin Coroutines
# Without these the volatile `result` field of CancellableContinuationImpl
# gets renamed and `cancellable` operations break in obscure ways.
# ─────────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ─────────────────────────────────────────────────────────────────────
# Supabase (PostGREST, Auth, Storage, Realtime)
# Heavy reflection internally — keep everything reachable.
# ─────────────────────────────────────────────────────────────────────
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**

# ─────────────────────────────────────────────────────────────────────
# Ktor (HTTP client used by Supabase)
# ─────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# OkHttp engine (we use ktor-client-okhttp)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ─────────────────────────────────────────────────────────────────────
# Firebase (Messaging + Analytics)
# ─────────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# FirebaseMessagingService is referenced from the manifest — must survive
-keep class com.ileader.app.data.notifications.ILeaderMessagingService { *; }

# ─────────────────────────────────────────────────────────────────────
# Coil (image loading)
# ─────────────────────────────────────────────────────────────────────
-dontwarn coil.**

# ─────────────────────────────────────────────────────────────────────
# Room (KSP-generated)
# Room's generated Dao_Impl/Database_Impl need to be findable.
# ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ─────────────────────────────────────────────────────────────────────
# CameraX + ML Kit (QR scanning)
# ─────────────────────────────────────────────────────────────────────
-dontwarn androidx.camera.**
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**

# ─────────────────────────────────────────────────────────────────────
# ZXing (QR generation)
# ─────────────────────────────────────────────────────────────────────
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ─────────────────────────────────────────────────────────────────────
# Media3 / ExoPlayer (video in courses)
# ─────────────────────────────────────────────────────────────────────
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# ─────────────────────────────────────────────────────────────────────
# Compose runtime
# ─────────────────────────────────────────────────────────────────────
-dontwarn androidx.compose.**

# Application-level entry points referenced from manifest
-keep class com.ileader.app.MainActivity { *; }
-keep class com.ileader.app.BuildConfig { *; }
