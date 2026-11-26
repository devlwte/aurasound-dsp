# AuraSound DSP Proguard / R8 Optimization Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Android AudioFX and Media APIs
-keep class android.media.audiofx.** { *; }
-keep class android.media.** { *; }

# Compose Runtime
-keep class androidx.compose.runtime.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.** { *; }
