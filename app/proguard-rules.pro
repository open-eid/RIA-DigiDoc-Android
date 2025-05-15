-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
    public static int wtf(...);
    public static int println(...);
}

# Preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information,
# hide the original source file name.
-renamesourcefileattribute SourceFile

# Android Log
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int e(...);
}
# Ignore Missing classes detected while running R8 warning.
-dontwarn com.gemalto.jp2.JP2Decoder
-dontwarn com.google.j2objc.annotations.RetainedWith
-dontwarn aQute.bnd.annotation.spi.ServiceProvider
-dontwarn javax.security.sasl.SaslClient

-keep class ee.ria.libdigidocpp.* { *; }

-keep public class * extends java.lang.Exception { *; }

-keep public class * extends retrofit2.** { *; }
-keep public class * extends okhttp3.** { *; }
-keep public class * implements retrofit2.** { *; }
-keep public class * implements okhttp3.** { *; }

# Keep data classes
-keep class ee.ria.DigiDoc.network.mid.dto.** { *; }
-keep class ee.ria.DigiDoc.network.sid.dto.** { *; }
-keep class ee.ria.DigiDoc.configuration.** { *; }
-keep class ee.ria.DigiDoc.libdigidoclib.domain.model.** { *; }
-keep class ee.ria.DigiDoc.network.proxy.** { *; }

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions

# OkHTTP
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase { *; }
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Gson
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-dontwarn sun.misc.**
# Retain Gson annotations (if you're using any @SerializedName annotations)
-keepattributes *Annotation*

# Retain the Gson classes
-keep class com.google.gson.** { *; }

-keepclasseswithmembers class * {
    <init>(...);
    @com.google.gson.annotations.SerializedName <fields>;
}
# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.** { *; }
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.** { *; }

# BouncyCastle
-keep class org.bouncycastle.** { *; }

# Woodstox
-keep class com.ctc.wstx.stax.** { *; }

-keep class javax.xml.namespace.** { *; }
-keep class javax.xml.stream.** { *; }