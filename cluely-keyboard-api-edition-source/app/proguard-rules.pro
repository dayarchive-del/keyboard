# Cluely Keyboard ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.cluely.keyboard.**$$serializer { *; }
-keepclassmembers class dev.cluely.keyboard.** {
    *** Companion;
}
-keepclasseswithmembers class dev.cluely.keyboard.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Keep InputMethodService
-keep class * extends android.inputmethodservice.InputMethodService { *; }
-keep class dev.cluely.keyboard.ui.ime.** { *; }