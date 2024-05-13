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