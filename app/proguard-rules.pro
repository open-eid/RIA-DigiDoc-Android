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