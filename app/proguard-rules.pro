# Med Pearls — release shrinker rules (Stage 14)

# Preserve stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# --- Kotlin / coroutines ---
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlinx.atomicfu.**

# --- kotlinx.serialization (Supabase DTOs, capture payloads) ---
-keep @kotlinx.serialization.Serializable class com.knowledgepearls.app.** { *; }
-keepclassmembers class com.knowledgepearls.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.knowledgepearls.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-keepclasseswithmembers class <1> {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Hilt / Dagger ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Supabase / Ktor ---
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# --- Firebase Cloud Messaging ---
-keep class com.google.firebase.** { *; }
-keep class com.knowledgepearls.app.push.MedPearlsFirebaseMessagingService { *; }
-dontwarn com.google.firebase.**

# --- Google Sign-In / Credential Manager ---
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }

# --- Media3 (in-app video) ---
-keep class androidx.media3.** { *; }

# --- WorkManager (scheduled backup) ---
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.knowledgepearls.app.data.backup.ScheduledBackupWorker { *; }

# --- Coil ---
-dontwarn coil3.**
