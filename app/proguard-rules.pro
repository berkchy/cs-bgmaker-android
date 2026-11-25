-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable

-keep class dev.berkchy.bgmaker.** { *; }

-dontwarn kotlinx.coroutines.**
-dontwarn androidx.compose.**
