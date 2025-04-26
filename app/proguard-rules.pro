# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/antoine/Android/adt-bundle/sdk/tools/proguard/proguard-android.txt
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
#
# Dont touch WebView Javascript Interface
-keepclassmembers class com.ayuget.redface.ui.view.TopicPageView.JsInterface {
    public *;
}

-keepclassmembers class com.ayuget.redface.ui.view.SmileySelectorView.JsInterface {
    public *;
}

# Only obfuscate
-dontshrink

# Keep source file and line numbers for better crash logs
-keepattributes SourceFile,LineNumberTable

# Avoid throws declarations getting removed from retrofit service definitions
-keepattributes Exceptions

# Retain generated class which implement Unbinder.
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }

-keep class dagger.* { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection

# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinding.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

# Do not touch Otto annotated classes
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Keep retrofit
-keep class retrofit.** { *; }
-keep class com.ayuget.redface.data.api.model.** { *; }
-keepclassmembernames interface * {
    @retrofit.http.* <methods>;
}

# Bouncycastle
-dontwarn org.bouncycastle.**

# OkHttp has some internal stuff not available on Android.
-dontwarn okhttp3.internal.**

# Okio has some stuff not available on Android.
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-dontwarn sun.misc.Unsafe

# Okio has some stuff not available on Android.
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Retrofit has some optional dependencies we don't use.
-dontwarn rx.**
-dontwarn retrofit.appengine.**
-dontwarn retrofit.client.**
-dontwarn com.squareup.picasso.**

# LeakCanary
-dontwarn com.squareup.haha.guava.**
-dontwarn com.squareup.haha.perflib.**
-dontwarn com.squareup.haha.trove.**
-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.haha.** { *; }
-keep class com.squareup.leakcanary.** { *; }

# Marshmallow removed Notification.setLatestEventInfo()
-dontwarn android.app.Notification

# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn afu.org.checkerframework.**
-dontwarn org.checkerframework.**
-dontwarn com.google.errorprone.**
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.ClassValue

# Guava 19.0
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Retrolambda
-dontwarn java.lang.invoke.*

# Unused classes in MaterialProgressBar
-dontwarn me.zhanghai.android.materialprogressbar.**

# Dagger
-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keepclassmembers class rx.internal.util.unsafe.** {
    long producerIndex;
    long consumerIndex;
}

-keep class com.ayuget.redface.data.state.CategoriesStore { *; }
-keep class com.ayuget.redface.image.superhost.** { *; }
-keep class com.ayuget.redface.network.UserCookieStore { *; }
-keep class com.ayuget.redface.network.SerializableHttpCookie { *; }
-keep class com.ayuget.redface.image.diberie.DiberieHostResult { *; }
