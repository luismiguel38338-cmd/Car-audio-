# Add project specific ProGuard rules here.
# Keep rules for Audio DSP Pro release optimization & reflection safety.

-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# Preserve entry points, foreground services & broadcast receivers
-keep class com.example.MainActivity { *; }
-keep class com.example.service.CarAudioPlaybackService { *; }
-keep class com.example.service.FloatingEqualizerService { *; }
-keep class com.example.receiver.AudioSessionReceiver { *; }

# Preserve ViewModel constructors invoked by ViewModelProvider
-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(android.app.Application);
}

# Preserve models, Firestore POJOs (@DocumentId / @ServerTimestamp / @IgnoreExtraProperties), auth, online & repositories
-keep class com.example.model.** { *; }
-keep class com.example.auth.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.online.** { *; }
-keep class com.example.downloads.** { *; }
-keepclassmembers class com.example.model.** { *; }

# Preserve Android Media3 & Credential Manager reflection classes
-keep class androidx.media3.** { *; }
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn com.google.errorprone.annotations.**
