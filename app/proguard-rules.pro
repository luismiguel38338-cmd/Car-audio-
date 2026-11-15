# Add project specific ProGuard rules here.
# Keep rules for Car Audio DSP Pro v6.0 release optimization & reflection safety.

-keepattributes SourceFile,LineNumberTable
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

# Preserve AudioModels & EqualizerModels data classes
-keep class com.example.model.** { *; }
