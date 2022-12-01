# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\Android\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontwarn com.squareup.okhttp.**
-dontwarn java.lang.invoke.**
-dontwarn org.joda.time.**

-dontwarn com.parse.**
-keep public class com.parse.** { *; }
#-keep class com.parse.** { *; }

-keepclassmembers class ** {
    public void onEvent*(***);
}
-dontwarn de.greenrobot.event.util.*$Support
-dontwarn de.greenrobot.event.util.*$SupportManagerFragment

-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

#-keep public class com.google.android.gms.* { public *; }
#-dontwarn com.google.android.gms.**

-keep public class ir.adad.client.** {
   *;
}

#---------------Tapsell---------------#

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep interface ir.tapsell.tapselldevelopersdk.NoProguard
-keep class * implements ir.tapsell.tapselldevelopersdk.NoProguard { *; }
-keep interface * extends ir.tapsell.tapselldevelopersdk.NoProguard { *; }
#-keep public class ir.tapsell.tapselldevelopersdk.** { *; }
#-dontwarn ir.tapsell.tapselldevelopersdk.**


-keep class com.volley.** { *; }